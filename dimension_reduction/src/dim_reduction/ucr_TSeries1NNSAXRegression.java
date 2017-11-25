package dim_reduction;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;

import net.seninp.jmotif.distance.EuclideanDistance;
import net.seninp.jmotif.sax.SAXException;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.jmotif.sax.alphabet.Alphabet;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;

public class ucr_TSeries1NNSAXRegression {
	
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
	
	public static double[] regressionLine(double []ts, int paaSize){
		//fix the length
		int len=ts.length;
		double []slopeArray;
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
			slopeArray=new double[1];
			double elementsSum=0;
			for(double e:ts){
				elementsSum=elementsSum + e;				
			}
			slopeArray[0]=(((double)elementsSum/(double)len) - ts[0])/(double)ts[ts.length -1];
			return slopeArray;
		}
		else{
			slopeArray=new double[paaSize];
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
				slopeArray[i]=(((double)elementsSum/(double)pointsPerSegment)- segment[0])/(double)segment[segment.length-1];				
			}
		}
		//for(int i=0;i< delta_tsdist.length;i++)
			//System.out.println(delta_tsdist[i][0]+", "+delta_tsdist[i][1]);
		return slopeArray;
	}	
	
	public static int classification_algorithm(List<sampleSeries>train_List, List<Double> test_List, int paa_segment, int saxAlpha)
	{
		List<Result>innerList=new ArrayList<Result>();
		TSProcessor tsp=new TSProcessor();
		SAXProcessor saxp=new SAXProcessor();
		//EuclideanDistance edDist=new EuclideanDistance();
		Alphabet normalA = new NormalAlphabet();
		
		//Transform train_List to SAX List
		//List<saxSeries>trainSAX_List=new ArrayList<saxSeries>();		
		for(int i=0;i< train_List.size();i++){
			Double []tempArray=new Double[train_List.get(i).Attributes.size()];
			Double []tempArray1=new Double[test_List.size()];
			train_List.get(i).Attributes.toArray(tempArray);
			test_List.toArray(tempArray1);
			char[] tSAX_List;
			char[] qSAX_List;
			double[]t_Slope;
			double[]q_Slope;
			
			try {
				//SAX Transform
				tSAX_List = saxp.ts2string(ArrayUtils.toPrimitive(tempArray), paa_segment, normalA.getCuts(saxAlpha), 0.0001);
				qSAX_List = saxp.ts2string(ArrayUtils.toPrimitive(tempArray1), paa_segment, normalA.getCuts(saxAlpha), 0.0001);
				
				//Delta Distance (Ordinary)
				//tdelta_Distance=delta_Distance(ArrayUtils.toPrimitive(tempArray), paa_segment);
				//qdelta_Distance=delta_Distance(ArrayUtils.toPrimitive(tempArray1), paa_segment);
				
				//Delta Distance (Normalized)
				t_Slope=regressionLine(tsp.znorm(ArrayUtils.toPrimitive(tempArray), 0.0001), paa_segment);
				q_Slope=regressionLine(tsp.znorm(ArrayUtils.toPrimitive(tempArray1), 0.0001), paa_segment);
				
				double sl_dist = 0;
				for(int d=0;d< t_Slope.length;d++){
					sl_dist+=((double)paa_segment / (double)test_List.size())*Math.pow((q_Slope[d] - t_Slope[d]), 2.0);	
				}				
				double saxDist=saxp.saxMinDist_update(qSAX_List, tSAX_List, normalA.getDistanceMatrix(saxAlpha), test_List.size(), paa_segment);
				double saxTD_Dist= (Math.sqrt((double) test_List.size()/(double) paa_segment))*Math.sqrt(sl_dist + saxDist);
				
				innerList.add(new Result(saxTD_Dist, train_List.get(i).cName));
				//trainSAX_List.add(new saxSeries(train_List.get(i).cName, tSAX_List));
			} catch (SAXException e) {				
				e.printStackTrace();
			}
			
		}
		Collections.sort(innerList, new DistanceComparator());
		int test_cLabel=innerList.get(0).cName;
		return test_cLabel;		
	}
	//SAX class to model instances (class + char[] features)
	/*static class saxSeries{
		char[]saxAttributes;
		int cLabel;
		public saxSeries(int cLabel, char[] saxAttributes){
			this.cLabel=cLabel;
			this.saxAttributes=saxAttributes;
		}
	}*/
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
			static class Result {	
				double distance;
				int cName;
				public Result(double distance, int cName){
					this.cName = cName;
					this.distance = distance;	    	    
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
		String train_filename="D:\\D1\\UCR_TS_Archive_2015\\22_Datasets_SAX\\CBF\\CBF_TRAIN";
		String test_filename="D:\\D1\\UCR_TS_Archive_2015\\22_Datasets_SAX\\CBF\\CBF_TEST";
		List<sampleSeries>train_List=dataLoad(train_filename);
		List<sampleSeries>test_List=dataLoad(test_filename);		
		int corrected=0;
		int paa_segment=128;
		int saxAlpha=10;
		Set<Integer>tLabel=new HashSet<Integer>();
		for(int i=0;i< train_List.size();i++){
			tLabel.add(train_List.get(i).cName);			
		}		
		for(int i=0;i< test_List.size();i++){			
			int predicted_cLabel = classification_algorithm(train_List, test_List.get(i).Attributes, paa_segment, saxAlpha);			
			if(predicted_cLabel == test_List.get(i).cName) corrected = corrected + 1;
		}
		System.out.println("The data set you tested has "+ tLabel.size() + " classes");		
		System.out.println("Training set is size of "+ train_List.size()+ " and test set is size of "+ test_List.size());
		System.out.println("Time series Length is "+ train_List.get(0).Attributes.size());
		System.out.println("Corrected Label "+ corrected);
		System.out.println("The error rate is "+ (double)(test_List.size() - corrected)/(double)test_List.size());
		
	}

}
