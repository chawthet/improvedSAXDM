package dim_reduction;

import java.io.IOException;

import net.seninp.jmotif.distance.EuclideanDistance;
import net.seninp.jmotif.sax.SAXException;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.jmotif.sax.alphabet.Alphabet;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;

public class PAA_SAX_ordinaryTest {

	public static void main(String[] args) {
		String ts1File = "D:\\myspace\\dimension_reduction\\test-data\\ECG_Train.csv";
		String ts2File = "D:\\myspace\\dimension_reduction\\test-data\\ECG_Test.csv";
		//String ts1File = "D:\\eclipse_luna_workspace\\dimension_reduction\\resources\\test-data\\cbf_train_sample.csv";
		//String ts2File = "D:\\eclipse_luna_workspace\\dimension_reduction\\resources\\test-data\\cbf_test_sample.csv";
		int loadLimit=Integer.MAX_VALUE;
		int paaSize=32;						//no of PAA segments
		double []ts1;
		double []ts2;
		double []paa_ts1;
		double []paa_ts2;
		double []norm_paats1;
		double []norm_paats2;
		char []sax_ts1;
		char []sax_ts2;
		
		TSProcessor tsp=new TSProcessor();
		SAXProcessor saxp=new SAXProcessor();
		EuclideanDistance edDist=new EuclideanDistance();
		Alphabet normalA = new NormalAlphabet();
		try {
			//Read time series
			ts1=tsp.readTS(ts1File, loadLimit);
			ts2=tsp.readTS(ts2File, loadLimit);
			int wsize=ts1.length/paaSize;
			/*System.out.println("Time Series Data:");
			for(int i=0;i< ts1.length;i++){
				System.out.println(ts1[i]+ ","+ ts2[i]);
			}*/
			//transform time series to PAA with segment-size
			paa_ts1=tsp.paa(ts1, paaSize);
			paa_ts2=tsp.paa(ts2, paaSize);
			//System.out.println("PAA transform Data:");
			/*for(int i=0;i< paa_ts1.length;i++){
			System.out.println(paa_ts1[i]+ ","+ paa_ts2[i]);			
			}*/
			//normalize PAA
			norm_paats1=tsp.normOne(paa_ts1);
			norm_paats2=tsp.normOne(paa_ts2);
			//System.out.println("PAA Normalized transform Data:");
			/*for(int i=0;i< norm_paats1.length;i++){
			System.out.println(norm_paats1[i]+ ","+ norm_paats2[i]);			
			}*/
			//transform PAA to symbol with alphabet size
			//sax_ts1=tsp.ts2String(norm_paats1, normalA.getCuts(10));
			sax_ts1=saxp.ts2string(ts1, paaSize, normalA.getCuts(10), 0.0001);
			//sax_ts2=tsp.ts2String(norm_paats2, normalA.getCuts(10));
			sax_ts2=saxp.ts2string(ts2, paaSize, normalA.getCuts(10), 0.0001);
			//System.out.println("SAX transform Data:");
			//for(int i=0;i< sax_ts1.length;i++)
			//	System.out.println(sax_ts1[i]+", "+sax_ts2[i]);
			
			//calculate distance
			try {
				//System.out.println("Real Distance: "+ edDist.distance2(ts1, ts2));
				System.out.println("PAA Distance: "+ edDist.distance2(paa_ts1, paa_ts2));
				//System.out.println("PAA Normalized Distance: "+ edDist.distance2(norm_paats1, norm_paats2));
				System.out.println("SAX String Distance:"+ saxp.strDistance(sax_ts1, sax_ts2));
				System.out.println("SAX MinDistance:"+ saxp.saxMinDist(sax_ts1, sax_ts2, normalA.getDistanceMatrix(10), ts1.length, paaSize));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
