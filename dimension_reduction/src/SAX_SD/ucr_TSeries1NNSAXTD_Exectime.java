package SAX_SD;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;

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
public class ucr_TSeries1NNSAXTD_Exectime {
	
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
	
	public static int classification_algorithm(List<sampleSeries>train_List, List<Double> test_List, int paa_segment, int saxAlpha)
	{
		List<Result1>innerList=new ArrayList<Result1>();
		TSProcessor tsp=new TSProcessor();
		SAXProcessor saxp=new SAXProcessor();
		//EuclideanDistance edDist=new EuclideanDistance();
		Alphabet normalA = new NormalAlphabet();
		double bestsofar=Double.POSITIVE_INFINITY;
		int test_cLabel=-99999;
		//Transform train_List to SAX List
		//List<saxSeries>trainSAX_List=new ArrayList<saxSeries>();		
		for(int i=0;i< train_List.size();i++){
			Double []tempArray=new Double[train_List.get(i).Attributes.size()];
			Double []tempArray1=new Double[test_List.size()];
			train_List.get(i).Attributes.toArray(tempArray);
			test_List.toArray(tempArray1);
			char[] tSAX_List;
			char[] qSAX_List;
			double[][] tdelta_Distance;
			double[][] qdelta_Distance;
			
			try {
				//SAX Transform
				tSAX_List = saxp.ts2string(ArrayUtils.toPrimitive(tempArray), paa_segment, normalA.getCuts(saxAlpha), 0.0001);
				qSAX_List = saxp.ts2string(ArrayUtils.toPrimitive(tempArray1), paa_segment, normalA.getCuts(saxAlpha), 0.0001);
				
				//Delta Distance (Ordinary)
				//tdelta_Distance=delta_Distance(ArrayUtils.toPrimitive(tempArray), paa_segment);
				//qdelta_Distance=delta_Distance(ArrayUtils.toPrimitive(tempArray1), paa_segment);
				
				//Delta Distance (Normalized)
				tdelta_Distance=delta_Distance(tsp.znorm(ArrayUtils.toPrimitive(tempArray), 0.0001), paa_segment);
				qdelta_Distance=delta_Distance(tsp.znorm(ArrayUtils.toPrimitive(tempArray1), 0.0001), paa_segment);
				
				double td_dist = 0;
				for(int d=0;d< tdelta_Distance.length;d++){
					double delta_Start=((qdelta_Distance[d][0]-tdelta_Distance[d][0])*(qdelta_Distance[d][0]-tdelta_Distance[d][0]));
					double delta_End=((qdelta_Distance[d][1]-tdelta_Distance[d][1])*(qdelta_Distance[d][1]-tdelta_Distance[d][1]));
					td_dist+=((((double)paa_segment/(double)test_List.size()))*Math.pow(Math.sqrt((delta_Start + delta_End)),2.0));													
				}		
				double saxDist=saxp.saxMinDist_update(qSAX_List, tSAX_List, normalA.getDistanceMatrix(saxAlpha), test_List.size(), paa_segment);
				double saxTD_Dist= (Math.sqrt((double) test_List.size()/(double) paa_segment))*Math.sqrt(td_dist + saxDist);
				//innerList.add(new Result1(saxTD_Dist, train_List.get(i).cName));
				//trainSAX_List.add(new saxSeries(train_List.get(i).cName, tSAX_List));
				if(saxTD_Dist < bestsofar){
					test_cLabel=train_List.get(i).cName;
					bestsofar=saxTD_Dist;
				}
			} catch (SAXException e) {				
				e.printStackTrace();
			}
			
		}
		//Collections.sort(innerList, new DistanceComparator1());
		//int test_cLabel=innerList.get(0).cName;
		return test_cLabel;		
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
			System.out.println("Invalid number of arguemnts OR types of arguments");
			System.exit(-1);
		}
		String train_filename= args[0];
		String test_filename=args[1];
		int paa_segment=Integer.parseInt(args[2]);	
		int saxAlpha=Integer.parseInt(args[3]);
		
		int corrected=0;
		long totaltime=0;
		double temp_dist=0;
		for (int y=0;y< 25;y++){		
		
			long startTime=System.currentTimeMillis();
			List<sampleSeries>train_List=dataLoad(train_filename);
			List<sampleSeries>test_List=dataLoad(test_filename);
						
			Set<Integer>tLabel=new HashSet<Integer>();
			for(int i=0;i< train_List.size();i++){
				tLabel.add(train_List.get(i).cName);			
			}
			corrected=0;
					for(int i=0;i< test_List.size();i++){			
						int predicted_cLabel = classification_algorithm(train_List, test_List.get(i).Attributes, paa_segment, saxAlpha);			
						if(predicted_cLabel == test_List.get(i).cName) corrected = corrected + 1;
			}
					temp_dist=(double)(test_List.size() - corrected)/(double)test_List.size();
					long endTime = System.currentTimeMillis();
					long elapsedTimeInMillis_1 = endTime - startTime;
					totaltime+=elapsedTimeInMillis_1;					
				}
		
		System.out.println("*******************************************");
		System.out.println("Corrected Label "+ corrected +"\nError Rate: "+ temp_dist);
		System.out.println("Total Execution time: "+ totaltime/25.0+ "msec");
	}	
}


