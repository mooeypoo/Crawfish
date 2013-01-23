package repastcity3.main;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class CSVFileMaker {

	private String file;
	FileWriter wr;
	PrintWriter pr;
	
	public CSVFileMaker(String fileName) throws IOException{
		this.setFile(fileName);
		this.wr = new FileWriter(fileName);
		this.pr = new PrintWriter(wr);
	}
	 	
	
	public void create_it() throws IOException{
		
		switch(this.file.charAt(0)){
			case 'A':
				pr.print("Day");
				pr.print(",");
				pr.print("Adult Number");
				pr.print(",");
				pr.print("S_Adults");
				pr.print(",");
				pr.print("E_Adults");
				pr.print(",");
				pr.print("I_Adults");
				pr.print(",");
				pr.print("R_Adults");
				pr.println();
				break;
			case 'T':
				pr.print("Day");
				pr.print(",");
				pr.print("Teen Number");
				pr.print(",");
				pr.print("S_Teens");
				pr.print(",");
				pr.print("E_Teens");
				pr.print(",");
				pr.print("I_Teens");
				pr.print(",");
				pr.print("R_Teens");
				pr.println();
				break;
			case 'C':
				pr.print("Day");
				pr.print(",");
				pr.print("Children Number");
				pr.print(",");
				pr.print("S_Children");
				pr.print(",");
				pr.print("E_Children");
				pr.print(",");
				pr.print("I_Children");
				pr.print(",");
				pr.print("R_Children");
				pr.println();
				break;
		}
		
		
		pr.flush();
		

		
	}
	
	public void setFile(String fileName){
		this.file = fileName;
	}

	public String getFile(){
		return this.file;
	}
	
	public void append_it() throws IOException{
		
		switch(this.file.charAt(0)){
		case 'A':
			pr.append('c');
			this.pr.print("1");
			this.pr.print(",");
			this.pr.print("100");
			this.pr.print(",");
			this.pr.print("40");
			this.pr.print(",");
			this.pr.print("34");
			this.pr.print(",");
			this.pr.print("45");
			this.pr.print(",");
			this.pr.print("78");
			this.pr.println();
			break;
		case 'T':
			pr.print("2");
			pr.print(",");
			pr.print("445r");
			pr.print(",");
			pr.print("456");
			pr.print(",");
			pr.print("677");
			pr.print(",");
			pr.print("87");
			pr.print(",");
			pr.print("98");
			pr.println();
			break;
		case 'C':
			pr.print("3");
			pr.print(",");
			pr.print("76");
			pr.print(",");
			pr.print("90");
			pr.print(",");
			pr.print("32");
			pr.print(",");
			pr.print("65");
			pr.print(",");
			pr.print("78");
			pr.println();
			break;
	}
	
	
	this.pr.flush();
	
	this.pr.close();
	this.wr.close();
	}	

}
