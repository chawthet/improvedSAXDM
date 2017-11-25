package dim_reduction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.StringTokenizer;

public class Transform_csv {

	public static void read_data(String filename)
	{
		String out_fname=null;
		int pos = filename.lastIndexOf(".");
		if (pos > 0) {
		    out_fname = filename.substring(0, pos);
		}
		else{
			out_fname=filename;
		}
		String nfilename=out_fname+".csv";
		File wfile=new File(nfilename);
		if(wfile.exists() && wfile.length()!=0){
			wfile.delete();
		}
		
		float tmp=0f;
		int id=0;
		String st = null;
		try {
			BufferedReader br = new BufferedReader(new FileReader(filename));
			PrintWriter pwriter = new PrintWriter(new FileWriter(wfile.getName(),true));
			while ((st = br.readLine()) != null) {
				StringTokenizer stk = new StringTokenizer(st, ",");
				int class_label=Integer.parseInt(stk.nextToken());
				//System.out.println(class_label);
				while (stk.hasMoreElements()){					
					 tmp=Float.valueOf(stk.nextToken());
					 pwriter.print(tmp+",");					
					}					
					//pwriter.println(class_label);
				pwriter.println();
		}
			pwriter.close();
	}catch (NumberFormatException | IOException e) {
			System.out.println(e.getMessage());
	}		
	}
	public static void main(String[] args) {
		String filename="/home/chawtzan/Documents/DM tools/UCR_TS_Archive_2015/20_Data_Set_for_testing/wafer/wafer_TEST";
		read_data(filename);

	}

}
