package dim_reduction;

import java.io.IOException;
import java.util.Arrays;

import net.seninp.jmotif.distance.EuclideanDistance;
import net.seninp.jmotif.sax.SAXException;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.jmotif.sax.alphabet.Alphabet;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;

public class SAXTD_Test {

	public static double[][] delta_Distance(double []ts, int paaSize){
		//fix the length
		int len=ts.length;
		double [][]delta_tsdist=new double[paaSize][2];
		int count=0;
		if (len < paaSize){
			try {
				throw new SAXException("PAA size can't be greater than timeseries size.");
			} catch (SAXException e) {				
				e.printStackTrace();
			}
		}
		//check for the trivial case
		if(len==paaSize){
			//return Arrays.copyOf(ts, ts.length);
			delta_tsdist[paaSize][0]=ts[0];
			delta_tsdist[paaSize][1]=ts[ts.length-1];
			return delta_tsdist;
		}
		else{			
			double tdstart_dist;	//trend distance for start for each segment
			double tdend_dist;		//trend distance for end for each segment
			double pointsPerSegment=(double)len/(double)paaSize;
			double []breaks=new double[paaSize+1];
			for(int i=0;i< paaSize+1; i++){
				breaks[i]=i*pointsPerSegment;
			}
			for(int i=0;i< paaSize;i++){
				double segStart=breaks[i];
				double segEnd=breaks[i+1];
				
				double fractionStart=Math.ceil(segStart) - segStart;
				double fractionEnd=segEnd - Math.floor(segEnd);
				
				int fullStart= Double.valueOf(Math.floor(segStart)).intValue();
				int fullEnd=Double.valueOf(Math.ceil(segEnd)).intValue();
				
				double[]segment=Arrays.copyOfRange(ts, fullStart, fullEnd);
				
				if(fractionStart > 0){
					segment[segment.length -1]=segment[segment.length -1 ] * fractionEnd;
				}
						
				double elementsSum=0.0;
				for(double e:segment){
					elementsSum=elementsSum + e;
					//System.out.print(e +" ");
				}
				System.out.println();
				
				tdstart_dist= segment[0]-(elementsSum/pointsPerSegment);
				tdend_dist= segment[segment.length-1]-(elementsSum/pointsPerSegment);
				
				delta_tsdist[i][0]=tdstart_dist;
				delta_tsdist[i][1]=tdend_dist;
				System.out.println(segment[0] +" "+ segment[segment.length-1]);
				System.out.println(elementsSum/pointsPerSegment+" "+tdstart_dist+" "+tdend_dist);
			}
		}
		for(int i=0;i< delta_tsdist.length;i++)
			System.out.println(delta_tsdist[i][0]+", "+delta_tsdist[i][1]);
		return delta_tsdist;
	}

	
	
	public static void main(String[] args) {
		String ts1File = "D:\\myspace\\dimension_reduction\\test-data\\ECG_Train.csv";
		String ts2File = "D:\\myspace\\dimension_reduction\\test-data\\ECG_Test.csv";
		
		//String ts1File = "/home/chawtzan/eclipse_luna_ws/dimension_reduction/resources/test-data/cbf_train_sample.csv";
		//String ts2File = "/home/chawtzan/eclipse_luna_ws/dimension_reduction/resources/test-data/cbf_test_sample.csv";
		TSProcessor tsp=new TSProcessor();
		SAXProcessor saxp=new SAXProcessor();
		//EuclideanDistance edDist=new EuclideanDistance();
		Alphabet normalA = new NormalAlphabet();
		double []ts1;
		double []ts2 = null;
		double[][] delta_ts1;
		double[][] delta_ts2;
		char []sax_ts1;
		char []sax_ts2;
		double td_dist=0;
		double sax_dist=0;
		int loadLimit=Integer.MAX_VALUE;
		int paaSize=32;						//no of PAA segments
		//Read time series
		try {
			ts1=tsp.readTS(ts1File, loadLimit);
			ts2=tsp.readTS(ts2File, loadLimit);
			delta_ts1=delta_Distance(tsp.znorm(ts1, 0.0001), paaSize);
			delta_ts2=delta_Distance(tsp.znorm(ts2, 0.0001), paaSize);
			sax_ts1=saxp.ts2string(ts1, paaSize, normalA.getCuts(10), 0.0001);
			sax_ts2=saxp.ts2string(ts2, paaSize, normalA.getCuts(10), 0.0001);
				sax_dist=saxp.saxMinDist_update(sax_ts1, sax_ts2, normalA.getDistanceMatrix(10), 128, paaSize);
			for(int i=0;i< delta_ts1.length;i++){
				double delta_Start=((delta_ts2[i][0]-delta_ts1[i][0])*(delta_ts2[i][0]-delta_ts1[i][0]));
				double delta_End=((delta_ts2[i][1]-delta_ts1[i][1])*(delta_ts2[i][1]-delta_ts1[i][1]));
				td_dist+=((((double)paaSize/(double)ts2.length))*(delta_Start + delta_End));	
				//td_dist+=((delta_Start + delta_End));
			}
		} catch (SAXException e) {			
			e.printStackTrace();
		} catch (IOException e) {			
			e.printStackTrace();
		}
		//System.out.println("Trend Distance: "+ td_dist+", "+ Math.sqrt(td_dist));
		//System.out.println("Trend Distance: "+Math.sqrt(td_dist)+" "+Math.sqrt((double) 128 / (double) paaSize)*Math.sqrt(td_dist));
		System.out.println("SAX_TD Distance:"+ Math.sqrt((double) ts2.length / (double) paaSize) * Math.sqrt(td_dist + sax_dist));
	}

}
