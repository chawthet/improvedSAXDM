package dim_reduction;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class ED_Test {
		
	public ArrayList<Vector<Double>> tsRead(String filename){
		
		Path file=Paths.get(filename);
		//java 8: Stream class
		Stream<String> lines;
		ArrayList<Vector<Double>>ts_array=new ArrayList<Vector<Double>>();
		try {
			lines = Files.lines(file, StandardCharsets.UTF_8);
			for(String line: (Iterable<String>) lines::iterator){
				StringTokenizer stk=new StringTokenizer(line, " ");
				Vector<Double> tmpV=new Vector<Double>();
				while(stk.hasMoreTokens()){
					tmpV.add(Double.parseDouble(stk.nextToken()));
				}
				ts_array.add(tmpV);				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ts_array;
	}	
	
	public double distance2(Vector<Double> point1, Vector<Double> point2)
			throws Exception {
		if (point1.size() == point2.size()) {
			Double sum = 0D;
			for (int i = 0; i < point1.size(); i++) {
				//System.out.println(point1.get(i)+","+point2.get(i));
				double tmp = point2.get(i) - point1.get(i);
				sum = sum + tmp * tmp;
			}
			return Math.sqrt(sum);
		} else {
			throw new Exception(
					"Exception in Euclidean distance: array lengths are not equal");
		}
	}
	
	public static void main(String[] args) {
		
		long startTime=System.nanoTime();
		String tsFile = "D:\\D1\\UCR_TS_Archive_2015\\22_Datasets_SAX\\synthetic_control\\synthetic_control_TRAIN";
		String qsFile = "D:\\D1\\UCR_TS_Archive_2015\\22_Datasets_SAX\\synthetic_control\\synthetic_control_TEST";
		ED_Test ed_test=new ED_Test();
		
		ArrayList<Vector<Double>>tsList=ed_test.tsRead(tsFile);
		ArrayList<Vector<Double>>qsList=ed_test.tsRead(qsFile);
		double []rst_dist=new double[tsList.size()];
		for(int i=0;i< tsList.size();i++){			
				try {
					//rst_dist[i]=ed_test.distance2(tsList.get(i), qsList.get(i));
					System.out.println("Distance: "+ ed_test.distance2(tsList.get(i), qsList.get(i)));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
			long endTime = System.nanoTime();
			long elapsedTimeInMillis_1 = TimeUnit.MILLISECONDS.convert((endTime - startTime), TimeUnit.NANOSECONDS);
			System.out.println("Time for calculation: "+ elapsedTimeInMillis_1/tsList.size() + " ms");
	}
}
