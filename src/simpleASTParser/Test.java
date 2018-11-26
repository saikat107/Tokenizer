package simpleASTParser;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;


public class Test {
	public static int testStaticInt = 10;
	public static void main(String args[]) throws IOException {
			if(args.length < 2) {
					System.out.println("java -jar Tokenizer.jat <input file path> <output path> [method]");
					System.exit(0);
			}
		    String infile = args[0];
			String outFileName = args[1];
			boolean methodOnly = false;
			if(args.length == 3) {
				methodOnly = true;
			}
			PrintStream outStream = new PrintStream(new File(outFileName));
				
			JavaASTTokenizer tokenizer = new JavaASTTokenizer(infile, outFileName);
			tokenizer.tokenize();
			List<Map<Integer, List<ASTToken>>> processedTokens = tokenizer.postProcess(methodOnly);
				
			for(Map<Integer, List<ASTToken>> fileTokens : processedTokens){
				writeDataToFile(fileTokens, outStream);
			}			
			outStream.close();
	 }
	
	private static void writeDataToFile(Map<Integer, List<ASTToken>> fileTokens, PrintStream stream) {
		Set<Integer> lineNumbers = fileTokens.keySet();
		for(Integer ln : lineNumbers){
			List<ASTToken> tokenInLine = fileTokens.get(ln);
			for(ASTToken token : tokenInLine){
				stream.print(token.toString() + '\t');
			}
			stream.println();
		}
		stream.flush();
	}
	
	@SuppressWarnings("unused")
	private static void writeTree(File file, Stream<Token> lines) throws IOException {
		  try (BufferedWriter fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
			   lines.forEachOrdered(x -> {
				    try {
					     fw.append(x.toString());
					     fw.append('\n');
				    } catch (IOException e) {
				    	System.err.println(e );
				    }
			   });
		  }
	}
	
	
	@SuppressWarnings("unused")
	private static void parseVarDecl(char[] arr, String infile_name){

		VarDeclarationASTVisitor astv = new VarDeclarationASTVisitor(arr, infile_name);
		astv.parse();
	}
	
	@SuppressWarnings("unused")
	private static void parseMethodInvoc(char[] arr, String infile_name){
		MethodInvocationASTVisitor astv = new MethodInvocationASTVisitor(arr, infile_name);
		astv.parse();
	}
	
	@SuppressWarnings("unused")
	private static void parse(char[] arr, String infile_name){
		SimpleASTVisitor astv = new SimpleASTVisitor(arr, infile_name);
		astv.parse();
	}
	
}