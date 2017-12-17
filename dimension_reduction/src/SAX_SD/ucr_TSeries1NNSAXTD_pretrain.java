package SAX_SD;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;

import SAX_SD.ucr_TSeries1NNSAXTD_Exectime.Result;
import SAX_SD.ucr_TSeries1NNSAXTD_Exectime.Result1;
import SAX_SD.ucr_TSeries1NNSAXTD_Exectime.sampleSeries;
import net.seninp.jmotif.sax.SAXException;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.jmotif.sax.alphabet.Alphabet;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;

/**
 * Implement SAXTD scheme 
 * for comparing classification accuracy with our proposed method
 * for UCR time series data sets
 * 
 * The best parameters (segment size and alphabet size)were shown in published paper.
 * 
 * Pre-training and check for execution time 
 * @author chawt
 * 
 */
/*
 * 4-inputs arguments have to be supported
 * args[0]- trainFile 
 * args[1]- testFile
 * args[2]- segment size (Number of segments)
 * args[3]- alphabet size(Number of alphabet represented for each segment)
 * args[0] and args[1] are String data types.
 * args[2] and args[3] are integer data types.  
 * 
 */

public class ucr_TSeries1NNSAXTD_pretrain {

	public static List<sampleSeries>  dataLoad(String filename){
		Path file=Paths.get(filename);
		//java 8: Stream class
		Stream<String> lines;
		int cname=0;
		List<sampleSeries>sSeriesList=new ArrayList<sampleSeries>();
		
		try {
			lines = Files.lines(file, StandardCharsets.UTF_8);
			for(String line: (Iterable<String>) lines::iterator){
				StringTokenizer stk=new StringTokenizer(line, ",");
				cname=Integer.parseInt(stk.nextToken());
				ArrayList<Double> sList=new ArrayList<Double>();				
				while(stk.hasMoreTokens()){					
					sList.add(Double.parseDouble(stk.nextToken()));
				}				
				sSeriesList.add(new sampleSeries(cname,sList));				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sSeriesList;
	}
	
	public static double[][] delta_Distance(double []ts, int paaSize){
		//fix the length
		int len=ts.length;
		double [][]delta_tsdist=new double[paaSize][2];
		//int count=0;
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
			double tdstart_dist;			//trend distance for start for each segment
			double tdend_dist;				//trend distance for end for each segment
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
				//System.out.println();
				
				tdstart_dist= segment[0]-(elementsSum/pointsPerSegment);
				tdend_dist= segment[segment.length-1]-(elementsSum/pointsPerSegment);
				
				delta_tsdist[i][0]=tdstart_dist;
				delta_tsdist[i][1]=tdend_dist;
				//System.out.println(segment[0] +" "+ segment[segment.length-1]);
				//System.out.println(elementsSum/pointsPerSegment+" "+tdstart_dist+" "+tdend_dist);
			}
		}
		//for(int i=0;i< delta_tsdist.length;i++)
			//System.out.println(delta_tsdist[i][0]+", "+delta_tsdist[i][1]);
		return delta_tsdist;
	}	

	//simple class to model instances (class + features)
	static class sampleSeries {	
		List<Double> Attributes=new ArrayList<Double>();
		int cName;
		public sampleSeries(int cName, ArrayList<Double>Attribute ){
			this.cName = cName;
			this.Attributes = Attribute;				
		}
	}
	
	//simple class to model results (distance + class)
	static class Result1 {	
		double distance;
		int cName;
		public Result1(double distance, int cName){
			this.cName = cName;
			this.distance = distance;	    	    
		}
	}
	
	//simple class to model results (distance + class)
	static class Result {	
		double distance;
		int paa_segment;
		int sax_alpha;
		long tms;
		public Result(double distance, int paa_segment, int sax_alpha, long tms){
			this.distance = distance;
			this.paa_segment=paa_segment;
			this.sax_alpha=sax_alpha;
			this.tms=tms;
		}
	}
	
	//simple comparator class used to compare results via distances
	static class DistanceComparator1 implements Comparator<Result1> {
		@Override
		public int compare(Result1 a, Result1 b) {
			return a.distance < b.distance ? -1 : a.distance == b.distance ? 0 : 1;
		}
	}
	//simple comparator class used to compare results via distances
	static class DistanceComparator implements Comparator<Result> {
		@Override
		public int compare(Result a, Result b) {
			return a.distance < b.distance ? -1 : a.distance == b.distance ? 0 : 1;
		}
	}

	public static void main(String[] args) {
		if(args.length < 4){
			System.out.println("Invalid numbers of arguments OR type of arguments");
			System.exit(-1);
		}
		String train_filename= args[0];
		String test_filename=args[1];
		int paa_segment=Integer.parseInt(args[2]);	
		int saxAlpha=Integer.parseInt(args[3]);
		
		TSProcessor tsp = new TSProcessor();
		SAXProcessor saxp = new SAXProcessor();
		Alphabet normalA = new NormalAlphabet();
		long totaltime=0;
		char[] tSAX_List;
		char[] qSAX_List = null;
		double[][] tdelta_Distance;
		double[][] qdelta_Distance = null;
		
		ArrayList<char[]>pre_List=new ArrayList<char[]>();
		ArrayList<double[][]>pre_List1=new ArrayList<double[][]>();
		
		long startTime = System.currentTimeMillis();
		List<sampleSeries> train_List = dataLoad(train_filename);
			
		for(int i=0;i< train_List.size();i++)
		{
			Double[] tempArray = new Double[train_List.get(i).Attributes.size()];
			train_List.get(i).Attributes.toArray(tempArray);
			try {
				tSAX_List = saxp.ts2string(ArrayUtils.toPrimitive(tempArray), paa_segment, normalA.getCuts(saxAlpha), 0.0001);
				tdelta_Distance=delta_Distance(tsp.znorm(ArrayUtils.toPrimitive(tempArray), 0.0001), paa_segment);
				pre_List.add(tSAX_List);
				pre_List1.add(tdelta_Distance);
			} catch (SAXException e) {			
				e.printStackTrace();
			}			
		}					
		int corrected=0;
		List<sampleSeries> test_List = dataLoad(test_filename);
			for (int i = 0; i < test_List.size(); i++) {
				double bestsofar=Double.POSITIVE_INFINITY;
				int test_cLabel = -99999;
				Double[] tempArray1 = new Double[test_List.get(i).Attributes.size()];
				test_List.get(i).Attributes.toArray(tempArray1);				
				try {
					qSAX_List = saxp.ts2string(ArrayUtils.toPrimitive(tempArray1),paa_segment, normalA.getCuts(saxAlpha), 0.0001);
					qdelta_Distance = delta_Distance(tsp.znorm(ArrayUtils.toPrimitive(tempArray1), 0.0001),paa_segment);
					} catch (SAXException e1) {
						e1.printStackTrace();
					}
					for(int j=0;j< train_List.size();j++){						
						try {							
							double td_dist = 0;
							for (int d = 0; d < qdelta_Distance.length; d++) {
								double delta_Start = ((qdelta_Distance[d][0] - pre_List1.get(j)[d][0]) * (qdelta_Distance[d][0] - pre_List1.get(j)[d][0]));
								double delta_End = ((qdelta_Distance[d][1] - pre_List1.get(j)[d][1]) * (qdelta_Distance[d][1] - pre_List1.get(j)[d][1]));
								td_dist += ((((double) paa_segment / (double) test_List.size())) * Math.pow(Math.sqrt((delta_Start + delta_End)), 2.0));
							}
							
							double saxDist = saxp.saxMinDist_update(qSAX_List, pre_List.get(j),normalA.getDistanceMatrix(saxAlpha), test_List.size(),paa_segment);
							double saxTD_Dist = (Math.sqrt((double) test_List.size()/(double) paa_segment))*Math.sqrt(td_dist + saxDist);
							if (saxTD_Dist < bestsofar) {
								test_cLabel = train_List.get(j).cName;
								bestsofar = saxTD_Dist;
							}
						} catch (SAXException e) {
							e.printStackTrace();
						}	
					}										
					if (test_cLabel == test_List.get(i).cName)
						corrected = corrected + 1;					
				}
			double temp_dist = (double) (test_List.size() - corrected)/ (double) test_List.size();
			long endTime = System.currentTimeMillis();
			totaltime = endTime - startTime;
			System.out.println("ErrorRate: " + temp_dist + "\nCorrected Label "+ corrected + "\nwith paa_segment & alpha size"+ paa_segment + " : " + saxAlpha);
			System.out.println("\nExecutionTime:"	+ totaltime+" ms");
		}
}