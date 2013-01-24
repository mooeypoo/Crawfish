package repastcity3.main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class CSVFileMaker {

	private String file;
	File f;
	FileWriter wr;
	PrintWriter pr;
	
	public CSVFileMaker(String fileName) throws IOException{
		String nameOfFile = "Output";
		nameOfFile +=f.separator;
		nameOfFile += fileName;
		this.setFile(nameOfFile);
		this.wr = new FileWriter(nameOfFile);
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
				pr.print(",");
				pr.print("D_Adults");
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
				pr.print(",");
				pr.print("D_Teens");
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
				pr.print(",");
				pr.print("D_Children");
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
	
	public void append_it(int day, List<Integer> dis_all, List<Integer> pop_all) throws IOException{
		
		switch(this.file.charAt(0)){
		case 'A':
			pr.print(day);
			pr.print(",");
			pr.print(pop_all.get(0));
			pr.print(",");
			pr.print(dis_all.get(0));
			pr.print(",");
			pr.print(dis_all.get(1));
			pr.print(",");
			pr.print(dis_all.get(2));
			pr.print(",");
			pr.print(dis_all.get(3));
			pr.print(",");
			pr.print(dis_all.get(4));
			pr.println();
			break;
		case 'T':
			pr.print(day);
			pr.print(",");
			pr.print(pop_all.get(2));
			pr.print(",");
			pr.print(dis_all.get(5));
			pr.print(",");
			pr.print(dis_all.get(6));
			pr.print(",");
			pr.print(dis_all.get(7));
			pr.print(",");
			pr.print(dis_all.get(8));
			pr.print(",");
			pr.print(dis_all.get(9));
			pr.println();
			break;
		case 'C':
			pr.print(day);
			pr.print(",");
			pr.print(pop_all.get(1));
			pr.print(",");
			pr.print(dis_all.get(10));
			pr.print(",");
			pr.print(dis_all.get(11));
			pr.print(",");
			pr.print(dis_all.get(12));
			pr.print(",");
			pr.print(dis_all.get(13));
			pr.print(",");
			pr.print(dis_all.get(14));
			pr.println();
			break;
	}
	
	
	this.pr.flush();
	
	}	

	
	public void close_it() throws IOException
	{
		pr.close();
		wr.close();
	}
}
