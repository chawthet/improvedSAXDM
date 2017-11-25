package dim_reduction;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.stream.Stream;

import net.seninp.jmotif.distance.EuclideanDistance;
import net.seninp.jmotif.sax.SAXException;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.jmotif.sax.alphabet.Alphabet;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;

import org.apache.commons.lang3.ArrayUtils;

public class SAX_Test {

public static ArrayList<Vector<Double>> tsRead(String filename){
		
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
	public static void main(String[] args) {
		//String tsFile = "D:\\eclipse_luna_workspace\\dimension_reduction\\resources\\dataset\\CBF\\CBF_TRAIN_NoClassify";
		//String qsFile = "D:\\eclipse_luna_workspace\\dimension_reduction\\resources\\dataset\\CBF\\CBF_TEST_NoClassify";
		
		String tsFile = "D:\\myspace\\dimension_reduction\\test-data\\cbf_train_sample.csv";
		String qsFile = "D:\\myspace\\dimension_reduction\\test-data\\cbf_test_sample.csv";
		ArrayList<Vector<Double>>tsList=tsRead(tsFile);
		ArrayList<Vector<Double>>qsList=tsRead(qsFile);
		int segment_Size=64;
		int ts_length=tsList.get(0).size();
		TSProcessor tsp=new TSProcessor();
		SAXProcessor saxp=new SAXProcessor();
		EuclideanDistance edDist=new EuclideanDistance();
		Alphabet normalA = new NormalAlphabet();
		Double[]ts_array = null;
		Double[]qs_array = null; 
		ArrayList<char[]>sax_tsList=new ArrayList<char[]>();
		ArrayList<char[]>sax_qsList=new ArrayList<char[]>();
		char []sax_ts;
		char []sax_qs;
		double [][]ts_PAA=new double[tsList.size()][];
		double [][]qs_PAA=new double[qsList.size()][];
		for(int i=0;i< tsList.size();i++){			
		//PAA transform	
				ts_array=new Double[tsList.get(i).size()];
				qs_array=new Double[qsList.get(i).size()];
				tsList.get(i).toArray(ts_array);
				qsList.get(i).toArray(qs_array);
				try {
					ts_PAA[i]=tsp.paa(ArrayUtils.toPrimitive(ts_array), segment_Size);
					qs_PAA[i]=tsp.paa(ArrayUtils.toPrimitive(qs_array), segment_Size);					
					//SAX transform
					//sax_ts=tsp.ts2String(tsp.normOne(ts_PAA[i]), normalA.getCuts(10));
					sax_ts=saxp.ts2string(ArrayUtils.toPrimitive(ts_array), segment_Size, normalA.getCuts(10), 0.0001);		//directly use time series (saxp.ts2string  transform PAA and SAX using parameter)
					sax_tsList.add(sax_ts);
					//sax_qs=tsp.ts2String(tsp.normOne(qs_PAA[i]), normalA.getCuts(10));
					sax_qs=saxp.ts2string(ArrayUtils.toPrimitive(qs_array), segment_Size, normalA.getCuts(10), 0.0001);
					sax_qsList.add(sax_qs);
				} catch (SAXException e) {		
					e.printStackTrace();
				}
			}
		/*for(int i=0;i< ts_PAA.length;i++){
			for(int j=0;j< segment_Size;j++)
				System.out.print(ts_PAA[i][j]+" ");
			System.out.println();
		}	*/		
		for(int i=0;i< sax_tsList.size();i++){			
				//System.out.print(sax_tsList.get(i)[j]+" | "+ sax_qsList.get(i)[j] +" | ");
				//System.out.println("Real Distance: "+ edDist.distance2(ts_array[i], qs_array[i]));				
				try {
					System.out.println("PAA Distance: "+ edDist.distance2(ts_PAA[i], qs_PAA[i]));
					System.out.println("SAX Distance:"+ saxp.strDistance(sax_tsList.get(i), sax_qsList.get(i)));
					System.out.println("SAX MinDistance:"+ saxp.saxMinDist(sax_tsList.get(i), sax_qsList.get(i), normalA.getDistanceMatrix(10), ts_length, segment_Size));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			}			
	}
	
}
