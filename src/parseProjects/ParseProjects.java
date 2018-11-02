package parseProjects;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import simpleASTParser.VarDeclarationASTVisitor;
import simpleASTParser.SimpleASTVisitor;

public class ParseProjects {
	
// public static void main(String args[]) throws IOException {
//		
//		String mainDir  = "C:\\Users\\br8jr\\Box Sync\\exception_handling\\java_projects";
//		int dirDepth = 1;
//		
//		if(args.length > 0) 
//			mainDir = args[0];
//		
//		if(args.length > 1) 
//			dirDepth = Integer.parseInt(args[1]);
//		
//		
//	  /* System.out.println("Parsing directorty " + mainDir 
//			   + " with depth " + dirDepth); */
//	   
//	   if(dirDepth == 1) {
//		   parseProjects(mainDir);
//	   }
//	   else {
//		   walk(mainDir);
//	   }
//  }
 

 
 public static void walk( String path ) {

     File root = new File( path );
     File[] list = root.listFiles();

     if (list == null) return;

     for ( File f : list ) {
         if ( f.isDirectory() ) {
             walk( f.getAbsolutePath() );
             //System.out.println( "Dir:" + f.getAbsoluteFile() );
         }
         else {
        	 String fileName = f.getName();
        	 if(fileName.toLowerCase().endsWith(".java"))    {	 
        		// System.out.println( "File:" + f.getAbsoluteFile() );
        		 try {
					printMethod(f.getAbsolutePath());
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	 }
        	 
             
         }
     }
 }
 
 private static void printMethod(String infile) throws FileNotFoundException {
	 
	 File file = new File(infile);
	 Scanner scanner = new Scanner(file);

	   String fileString = scanner.nextLine();
	   while (scanner.hasNextLine()) {
		   fileString = fileString + "\n" + scanner.nextLine();
	   }

	   char[] charArray = fileString.toCharArray();
	   
	   parseMethodInvoc(charArray,infile);
	   parse(charArray,infile);
 }
 
 private static void parseProjects(String dirPath) {
	 
	 File folder = new File(dirPath);
	 File[] listOfProjects = folder.listFiles(); 
	   
	 for (int i = 0; i < listOfProjects.length; i++) {
		 if (listOfProjects[i].isDirectory()) {
		        //System.out.println("Project ==== " + listOfProjects[i].getName());
		        walk(listOfProjects[i].getAbsolutePath());
		        
		 }
		 
	 }
	 
 }

	private static void parseMethodInvoc(char[] arr, String infile_name){
		VarDeclarationASTVisitor astv = new VarDeclarationASTVisitor(arr, infile_name);
		astv.parse();
	}
	
	private static void parse(char[] arr, String infile_name){
		SimpleASTVisitor astv = new SimpleASTVisitor(arr, infile_name);
		astv.parse();
	}

}
