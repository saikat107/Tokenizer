package simpleASTParser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;


import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;

@SuppressWarnings("unused")
public class JavaASTTokenizer extends ASTVisitor{

	private String filePath;
	private String documentText;
	private String outputFile;
	private CompilationUnit cu;
	private int methodCount = 0;
	private List<List<Token>> sequenceList;
	
	private Map<String, String>methodSigMap = new HashMap<String, String>();
	
	private static final String[] PUNCTUATIONS = {"~", "`", "!", "@", "#", "$", "%", "^", "&", "*", "(", ")", "-", "+", "=",
			"{", "}", "|", "\"", "'", ";", ":", "<", ">", ",", ".", "?", "/"};
	
	private List<List<Token>> methodTokenList;
	
	public JavaASTTokenizer(String inputJavaFilePath, String outputFilePath) throws FileNotFoundException{
		
		this.filePath = inputJavaFilePath;
		Scanner scanner = new Scanner(new File(inputJavaFilePath));
		String fileString = "";
		while (scanner.hasNextLine()) {
			fileString = fileString  + scanner.nextLine() + "\n";
		}
		this.documentText = fileString;
		//System.out.println(this.documentText);
		sequenceList = new ArrayList<>();
		methodTokenList = new ArrayList<>();
		this.outputFile = outputFilePath;
		scanner.close();
	}
	
	
	public boolean visit(MethodDeclaration node){
		//System.out.println(node);
		String text = node.toString();
		List <Token> tokenStream = traverse(documentText , node);
		methodTokenList.add(tokenStream);
		return true;
	}
	
	public void tokenize() {
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		
		Map<String, String> options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
		parser.setCompilerOptions(options);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(this.documentText.toCharArray());
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true);
		String[] sources = { "" };
		String[] classpath = { System.getProperty("java.home") + "/lib/rt.jar" };
		parser.setUnitName(this.filePath);
		parser.setEnvironment(classpath, sources, new String[] { "UTF-8" }, true);
		
		cu = (CompilationUnit) parser.createAST(null);
		cu.accept(this);
		
		List<Token> tokenStream = traverse(documentText, cu.getRoot());
		sequenceList.add(tokenStream);
		//System.out.println(this.methodCount);
	}

	public  List<Token> traverse(String document, ASTNode node) throws IllegalArgumentException {
		return traverse(document, node, 1, 0, null);
	}

	private static final int[] ignore = { ASTNode.JAVADOC};
	@SuppressWarnings("rawtypes")
	private List<Token> traverse(String document, ASTNode node, int depth, int offset, String methodName) 
			throws IllegalArgumentException {
		String name = null;
		if(node.getNodeType() == ASTNode.METHOD_DECLARATION){
			MethodDeclaration mdNode = (MethodDeclaration) node;
			String bodyString = "";
			if(mdNode.getBody() != null){
				bodyString = mdNode.getBody().toString();
			}
			String wholeString = node.toString();
			String methodDeclaration = wholeString.substring(0, wholeString.indexOf(bodyString));
			//System.out.println(methodDeclaration);
			name = methodDeclaration;
			this.methodCount ++;
			methodSigMap.put(name, "m" + this.methodCount);
		}
		else{
			name = methodName;
		}
		List<Token> tokens = new ArrayList<Token>();
		if (contains(ignore, node.getNodeType())) {
			return tokens;
		}
		int start = node.getStartPosition();
		int end = start + node.getLength();
		if (node.getLength() > 1000000000) {
			throw new IllegalArgumentException("Syntax error in current file near node at " + start + ", content: " + node.toString());
		}
		String type = "";
		if (node.getNodeType() == ASTNode.SIMPLE_NAME) {
			String nodeBody = node.toString().trim();
			ITypeBinding binding = ((SimpleName) node).resolveTypeBinding();
			if (binding != null)
				type = binding.getQualifiedName();
			if(nodeBody.trim().compareTo(type) != 0){
				if(! type.trim().endsWith(nodeBody))
					if(type.trim() == ""){
						type = "UNIDENTIFIED_TYPE";
					}
					type = type + "_VAR";
			}
		}
		else if(node.getNodeType() == ASTNode.NUMBER_LITERAL){
			type = "NUMBER";
		}
		else if(node.getNodeType() == ASTNode.STRING_LITERAL){
			type = "STRING";
		}
		else{
			type = null;
		}
		
		List list = node.structuralPropertiesForType();
		int pos = start;
		for (int i = 0; i < list.size(); i++) {
			StructuralPropertyDescriptor curr = (StructuralPropertyDescriptor) list.get(i);
			Object child = node.getStructuralProperty(curr);
			if (child instanceof ASTNode) {
				pos = addChild(document, node, depth, offset, tokens, type, pos, child, name);
			}
			else if (child instanceof List) {
				List children = (List) child;
				for (Object el : children) {
					if (el instanceof ASTNode) {
						pos = addChild(document, node, depth, offset, tokens, type, pos, el, name);
					}
				}
			}
		}
		if (pos < end) {
			String intermediate = document.substring(pos, end);
			if (intermediate.startsWith("[")) {
				String rep = intermediate.trim().replaceAll("[\n\r]+", "");
				if (rep.length() > 2) intermediate = "[";
			}
			checkComment(intermediate, tokens, type, pos+1, depth, name);
		}
		return tokens;
	}

	
	

	private int checkBlockComment(int depth, List<Token> tokens, String type, int pos, String intermediate, String name) {
		if(intermediate.contains("/*")){
			int start_comment = intermediate.indexOf("/*");
			String beforeComment = intermediate.substring(0 , start_comment);
			checkComment(beforeComment, tokens, type, pos, depth, name);
			String comment = intermediate.substring(start_comment);
			int end_comment = comment.indexOf("*/");
			if(end_comment != -1){
				String afterComment = comment.substring(end_comment + 2);
				comment = comment.substring(0, end_comment + 2);
				//appendTerminals(comment,  depth + 1, tokens, "BLOCK_COMMENT", pos + start_comment);
				checkComment(afterComment, tokens, type, pos + start_comment + end_comment, depth, name);
			}
			return 1;
		}
		return 0;
	}


	private int checkLineComment(int depth, List<Token> tokens, String type, int pos, String intermediate, String name) {
		if(intermediate.contains("//")){
			int start_comment = intermediate.indexOf("//");
			String beforeComment = intermediate.substring(0 , start_comment);
			String comment = intermediate.substring(start_comment);
			int end_comment = comment.indexOf("\n");
			checkComment(beforeComment, tokens, type, pos, depth, name);
			if(end_comment != -1){
				String afterComment = comment.substring(end_comment);
				comment = comment.substring(0, end_comment);
				checkComment(afterComment, tokens, type, pos + start_comment + end_comment, depth, name);
			}
			return 1;
		}
		return 0;
	}
	
	
	

	private  void checkComment(String text, List<Token> tokens, String type, int pos, int depth, String name) {
		int success = checkBlockComment(depth, tokens, type, pos, text, name);
		if(success == 0 ){
			success = checkLineComment(depth, tokens, type, pos, text, name);
		}
		if(success == 0){
			if(text.trim().startsWith("()")){
				appendTerminals("(", depth, tokens, type, pos, name);
				appendTerminals(")", depth, tokens, type, pos+1, name);
			}
			else{
				appendTerminals(text, depth, tokens, type, pos, name);
			}
		}
	}


	private int addChild(String document, ASTNode node, int depth, int offset, 
			List<Token> tokens, String type, int pos, Object child, String name) {
		ASTNode childNode = (ASTNode) child;
		int childPos = childNode.getStartPosition();
		if (childPos > pos) {
			String intermediate = document.substring(pos, childPos);
			checkComment(intermediate, tokens, type, childPos, depth, name);
			pos += intermediate.length();
		}
		tokens.addAll(traverse(document, childNode, depth + 1, offset + pos, name));
		pos += childNode.getLength();
		if (pos != childNode.getLength() + childPos) {
			pos = childPos + childNode.getLength();
		}
		return pos;
	}

	private  void appendTerminals(String intermediate, int depth, List<Token> tokens, String type, int pos, String name) {
		appendTerminals(intermediate, depth, tokens, type, pos, pos + intermediate.length(), name);
	}

	private  void appendTerminals(String intermediate, int depth, List<Token> tokens, String type, int pos, int end, String name) {
		if (!intermediate.trim().isEmpty()) {
			//System.out.println(intermediate.trim().replaceAll("[\n\r]+", "") + '\t' + cu.getLineNumber(pos) + '\t' + name);
			String m = "";
			if(name!= null  && methodSigMap.containsKey(name)){
				m = methodSigMap.get(name);
			}
			String text = intermediate.trim().replaceAll("[\n\r]+", "");
			int ln = cu.getLineNumber(pos);
			if(type!= null && type.compareTo("STRING") == 0){
				
			}
			else{
				for(String punc:PUNCTUATIONS){
					if(text.contains(punc)){
						//System.out.println(text);
						int pi = text.indexOf(punc);
						String beforePunc = text.substring(0,pi).trim();
						String afterPunc = text.substring(pi+1).trim();
						if(beforePunc.length() > 0){
							//appendTerminals(beforePunc, depth, tokens, type, pos, end, name);
							ASTToken token1 = new ASTToken(beforePunc, depth, type, pos, end - 1, ln, m);
							tokens.add(token1);
						}
						// appendTerminals(punc, depth, tokens, type, pos, end, name);
						ASTToken token2 = new ASTToken(punc, depth, type, pos, end - 1, ln, m);
						tokens.add(token2);
						if(afterPunc.length() > 0){
							// appendTerminals(afterPunc, depth, tokens, type, pos, end, name);
							ASTToken token3 = new ASTToken(afterPunc, depth, type, pos, end - 1, ln, m);
							tokens.add(token3);
						}
						return;
					}
				}
			}
			ASTToken token = new ASTToken(text, depth, type, pos, end - 1, ln, m);
			tokens.add(token);
		}
	}

	private  boolean contains(int[] ignore, int nodeType) {
		for (int i : ignore) if (i == nodeType) return true;
		return false;
	}
	
//	public 

	public List<Map<Integer, List<ASTToken>>> postProcess(boolean methodOnly){
		List<Map<Integer, List<ASTToken>>> returns = new ArrayList<Map<Integer,List<ASTToken>>>();
		int count = 0;
		List<List<Token>> intendedList = null;
		if(methodOnly) {
			intendedList = methodTokenList;
		}
		else {
			intendedList = sequenceList;
		}
		for(List<Token> sequence : intendedList){
			count++;
			int tokenLength = sequence.size();
			Token []tokenList = new ASTToken[tokenLength];
			tokenList = sequence.toArray(tokenList);
			for (int i = 0; i < tokenLength; i++){
				String word = ((ASTToken)tokenList[i]).getContent();
				if(JavaKeywords.isKeyWord(word)){
					//((ASTToken)tokenList[i]).setType("");
				}
				else{
					if(i < tokenLength -1){
						String nextWord = ((ASTToken)tokenList[i + 1]).getContent();
						if (nextWord.trim().compareTo("(")==0 || nextWord.trim().compareTo("()")==0){
							if(! contains(PUNCTUATIONS, word))
								((ASTToken)tokenList[i]).setType("METHOD_NAME");
						}
					}
				}
				
				if(((ASTToken)tokenList[i]).getType().compareTo("STRING") == 0){
					((ASTToken)tokenList[i]).setCOntent("STRING");
				}
				else if(((ASTToken)tokenList[i]).getType().compareTo("NUMBER") == 0){
					((ASTToken)tokenList[i]).setCOntent("NUMBER");
				}
			}
			int lastLine = ((ASTToken)tokenList[tokenList.length - 1]).getLineNumber();
			
			if(methodOnly) {
				Map<Integer, List<ASTToken>> map = new TreeMap<Integer, List<ASTToken>>();
				List<ASTToken> list = new ArrayList<>();
				for(Token tok : sequence) {
					list.add((ASTToken)tok);
				}
				map.put(count, list);
				returns.add(map);
			}
			else {
				Map<Integer, List<ASTToken>> map = new TreeMap<Integer, List<ASTToken>>();
				for(Token token : tokenList){
					ASTToken tok = (ASTToken) token;
					int ln = tok.getLineNumber();
					List<ASTToken> tokens = null; 
					if(map.containsKey(ln)){
						tokens = map.get(ln);
					}
					if(tokens == null){
						tokens = new ArrayList<ASTToken>();
					}
					tokens.add(tok);
					map.put(ln, tokens);
				}
				returns.add(map);
			}
		}
		return returns;
	}
	
	public void printOutputToFile() throws IOException {
		
		PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(outputFile, true)));
		for(List<Token> sequence : sequenceList){
			int tokenLength = sequence.size();
			Token []tokenList = new ASTToken[tokenLength];
			tokenList = sequence.toArray(tokenList);
			
		}
		writer.close();
	}


	private boolean contains(String[] punctuations, String word) {
		for(String punc : punctuations){
			if(punc.compareTo(word.trim()) == 0) return true;
		}
		return false;
	}
}
