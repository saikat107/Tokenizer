package simpleASTParser;

import simpleASTParser.Token;

public class ASTToken extends Token {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2997206275657174543L;

	private final int depth;
	private  String type;
	
	private final int startLine;
	private final int endLine;
	private int lineNumber;
	private String methodName;

	public ASTToken(String text, int depth) {
		this(text, depth, "");
	}

	public ASTToken(String text, int depth, String type) {
		this(text, depth, type, 0, 0, 0, null);
	}

	public ASTToken(String text, int depth, String type, int startLine, int endLine, int lineNumber, String methodName) {
		super(text);
		this.depth = depth;
		this.type = type;
		if(type == "" || type == null){
			this.type = text;
		}
		this.startLine = startLine;
		this.endLine = endLine;
		this.lineNumber = lineNumber;
		if(methodName == null || methodName == ""){
			this.methodName = "NULL";
		}
		else{
			this.methodName = methodName;
		}
	}
	
	public String toString(){
		return this.text + " " + this.type + " " + this.lineNumber + " " + this.methodName;
	}
	
	private final char delim = '\t';
	@Override
	public String text() {
		StringBuilder sb = new StringBuilder();
		//sb.append("Text : " + this.text + "\t\tType : " + this.type);
		//sb.append("<\"" + this.text + "\"");
		if(this.type != null && this.type.length()!=0){
			sb.append(this.type );
		}
		else{
			sb.append(this.text);
		}
		//sb.append(">");
		return sb.toString();
	}

	public String getContent() {
		return this.text;
	}
	
	public String getType(){
		return this.type;
	}
	
	public void setCOntent(String content){
		super.text = content;
		this.text = content;
	}
	
	public void setType(String type){
		this.type = type;
	}

	public int getDepth() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getLineNumber() {
		// TODO Auto-generated method stub
		return lineNumber;
	}

	public String getMethodName() {
		// TODO Auto-generated method stub
		return methodName;
	}
}