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
		int count = 0;
		//try (BufferedReader br = new BufferedReader(new FileReader("tests/in.txt"))) {
		   // String line;
		   // while ((line = br.readLine()) != null) {
				if(args.length < 2) {
						System.out.println("2 argument needed, 1. input java file, 2. output file");
						System.exit(0);
				}
		    		String infile = args[0];
				String outFileName = args[1];
				
				PrintStream outStream = new PrintStream(new File(outFileName));
				
				JavaASTTokenizer tokenizer = new JavaASTTokenizer(infile, outFileName);
				tokenizer.tokenize();
				List<Map<Integer, List<ASTToken>>> processedTokens = tokenizer.postProcess();
				
				for(Map<Integer, List<ASTToken>> fileTokens : processedTokens){
					writeDataToFile(fileTokens, outStream);
				}
				
				outStream.close();
		    //}
		//} 
		
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