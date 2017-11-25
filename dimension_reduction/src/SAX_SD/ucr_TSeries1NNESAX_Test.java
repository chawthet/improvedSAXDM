package SAX_SD;

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
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;

import SAX_SD.ucr_TSeries1NNSAXTD_Test.DistanceComparator;
import SAX_SD.ucr_TSeries1NNSAXTD_Test.Result;
import SAX_SD.ucr_TSeries1NNSAXTD_Test.Result1;
import net.seninp.jmotif.distance.EuclideanDistance;
import net.seninp.jmotif.sax.SAXException;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.jmotif.sax.alphabet.Alphabet;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;

public class ucr_TSeries1NNESAX_Test {

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
	/**
	   * Finds the maximal value in timeseries.
	   * 
	   * @param series The timeseries.
	   * @return The max value.
	   */
	  public static double max(double[] series) {
	    double max = Double.MIN_VALUE;
	    for (int i = 0; i < series.length; i++) {
	      if (max < series[i]) {
	        max = series[i];
	      }
	    }
	    return max;
	  }

	  /**
	   * Finds the minimal value in timeseries.
	   * 
	   * @param series The timeseries.
	   * @return The min value.
	   */
	  public static double min(double[] series) {
	    double min = Double.MAX_VALUE;
	    for (int i = 0; i < series.length; i++) {
	      if (min > series[i]) {
	        min = series[i];
	      }
	    }
	    return min;
	  }
	  
	  	//find max_min values for each paa segment
	  public static double[][] max_min(double []ts, int paaSize){
		//fix the length
		int len=ts.length;
		double [][]maxmin_value=new double[paaSize][3];
		//int count=0;
		if (len < paaSize || len==paaSize){
			try {
				throw new SAXException("PAA size can't be greater than timeseries size.");
			} catch (SAXException e) {				
				e.printStackTrace();
			}
		}		
		else{		
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
				double midpoint=(segment[0]+segment[segment.length-1])/(double)2.0;
				double mx=max(segment);
				double mi=min(segment);
				double me=elementsSum/(double)segment.length;
				if(mx < midpoint && midpoint < mi)	{
					maxmin_value[i][0]=mx;
					maxmin_value[i][1]=me;
					maxmin_value[i][2]=mi;					
				}
				else if(mi < midpoint && midpoint < mx)	{
					maxmin_value[i][0]=mi;
					maxmin_value[i][1]=me;
					maxmin_value[i][2]=mx;					
				}
				else if(mi < mx &&  mx < midpoint)	{
					maxmin_value[i][0]=mi;
					maxmin_value[i][1]=mx;
					maxmin_value[i][2]=me;					
				}
				else if(mx < mi &&  mi < midpoint)	{
					maxmin_value[i][0]=mx;
					maxmin_value[i][1]=mi;
					maxmin_value[i][2]=me;					
				}
				else if(midpoint < mx &&  mx < mi )	{
					maxmin_value[i][0]=me;
					maxmin_value[i][1]=mx;
					maxmin_value[i][2]=mi;					
				}
				else{
					maxmin_value[i][0]=me;
					maxmin_value[i][1]=mi;
					maxmin_value[i][2]=mx;
				}
								
			}
		}
		//for(int i=0;i< delta_tsdist.length;i++)
			//System.out.println(delta_tsdist[i][0]+", "+delta_tsdist[i][1]);
		return maxmin_value;
	}	
	public static int classification_algorithm(List<sampleSeries>train_List, List<Double> test_List, int paa_segment, int saxAlpha)
	{
		List<Result>innerList=new ArrayList<Result>();		
		SAXProcessor saxp=new SAXProcessor();
		TSProcessor tsp=new TSProcessor();
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
			double[][]tmaxmin_value;
			double[][]qmaxmin_value;
			char[][]tmaxmeanmin_char;
			char[][]qmaxmeanmin_char;
			double saxDist=0;
			try {
				//SAX transformation
				//tSAX_List = saxp.ts2string(ArrayUtils.toPrimitive(tempArray), paa_segment, normalA.getCuts(saxAlpha), 0.0001);
				//qSAX_List = saxp.ts2string(ArrayUtils.toPrimitive(tempArray1), paa_segment, normalA.getCuts(saxAlpha), 0.0001);
				
				//max_min array transform to SAX
				tmaxmin_value=max_min(tsp.znorm(ArrayUtils.toPrimitive(tempArray), 0.00001), paa_segment);
				qmaxmin_value=max_min(tsp.znorm(ArrayUtils.toPrimitive(tempArray1), 0.00001), paa_segment);
				
				tmaxmeanmin_char = new char[paa_segment][3];
				qmaxmeanmin_char = new char[paa_segment][3];
				
				for(int d=0;d< tmaxmin_value.length;d++)
				{
					tmaxmeanmin_char[d][0]=tsp.num2char(tmaxmin_value[d][0], normalA.getCuts(saxAlpha));
					tmaxmeanmin_char[d][1]=tsp.num2char(tmaxmin_value[d][1], normalA.getCuts(saxAlpha));
					tmaxmeanmin_char[d][2]=tsp.num2char(tmaxmin_value[d][2], normalA.getCuts(saxAlpha));
					qmaxmeanmin_char[d][0]=tsp.num2char(qmaxmin_value[d][0], normalA.getCuts(saxAlpha));
					qmaxmeanmin_char[d][1]=tsp.num2char(qmaxmin_value[d][1], normalA.getCuts(saxAlpha));
					qmaxmeanmin_char[d][2]=tsp.num2char(qmaxmin_value[d][2], normalA.getCuts(saxAlpha));
					
					//calculate distance
					 saxDist +=saxp.EsaxMinDist(qmaxmeanmin_char[d], tmaxmeanmin_char[d], normalA.getDistanceMatrix(saxAlpha));	//test_List.size is time series Leng
				}
				double fsaxDist=((double)test_List.size()/(double)paa_segment)*saxDist;
				if(fsaxDist < bestsofar){
					test_cLabel=train_List.get(i).cName;
					bestsofar=fsaxDist;
				}
				//innerList.add(new Result(fsaxDist, train_List.get(i).cName));
				//trainSAX_List.add(new saxSeries(train_List.get(i).cName, tSAX_List));
			} catch (SAXException e) {				
				e.printStackTrace();
			}
			
		}
		//Collections.sort(innerList, new DistanceComparator());
		//int test_cLabel=innerList.get(0).cName;
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
			String []train_filename={
					"D:\\D1\\UCR_TS_Archive_2015\\22_Datasets_SAX\\synthetic_control\\synthetic_control_TRAIN", 
					"D:\\D1\\UCR_TS_Archive_2015\\22_Datasets_SAX\\Gun_Point\\Gun_Point_TRAIN",
					"D:\\D1\\UCR_TS_Archive_2015\\22_Datasets_SAX\\CBF\\CBF_TRAIN",
					"D:\\D1\\UCR_TS_Archive_2015\\22_Datasets_SAX\\FaceAll\\FaceAll_TRAIN",
					"D:\\D1\\UCR_TS_Archive_2015\\22_Datasets_SAX\\OSULeaf\\OSULeaf_TRAIN",
					"D:\\D1\\UCR_TS_Archive_2015\\22_Datasets_SAX\\SwedishLeaf\\SwedishLeaf_TRAIN",
					"D:\\D1\\UCR_TS_Archive_2015\\22_Datasets_SAX\\50words\\50words_TRAIN",
					"D:\\D1\\UCR_TS_Archive_2015\\22_Datasets_SAX\\Trace\\Trace_TRAIN",
					"D:\\D1\\UCR_TS_Archive_2015\\22_Datasets_SAX\\Two_Patterns\\Two_Patterns_TRAIN",
					"D:\\D1\\UCR_TS_Archive_2015\\22_Datasets_SAX\\wafer\\wafer_TRAIN",
					"D:\\D1\\UCR_TS_Archive_2015\\22_Datasets_SAX\\FaceFour\\FaceFour_TRAIN",
					"D:\\D1\\UCR_TS_Archive_2015\\22_Datasets_SAX\\Lighting2\\Lighting2_TRAIN",
					"D:\\D1\\UCR_TS_Archive_2015\\22_Datasets_SAX\\Lighting7\\Lighting7_TRAIN",
					"D:\\D1\\UCR_TS_Archive_2015\\22_Datasets_SAX\\yoga\\yoga_TRAIN",
					"D:\\D1\\UCR_TS_Archive_2015\\22_Datasets_SAX\\Adiac\\Adiac_TRAIN",
					"D:\\D1\\UCR_TS_Archive_2015\\22_Datasets_SAX\\yoga\\yoga_TRAIN",
					"D:\\D1\\UCR_TS_Archive_2015\\22_Datasets_SAX\\FISH\\FISH_TRAIN",
					"D:\\D1\\UCR_TS_Archive_2015\\22_Datasets_SAX\\Beef\\Beef_TRAIN",
					"D:\\D1\\UCR_TS_Archive_2015\\22_Datasets_SAX\\Coffee\\Coffee_TRAIN",
					"D:\\D1\\UCR_TS_Archive_2015\\22_Datasets_SAX\\OliveOil\\OliveOil_TRAIN" 
					};
			String []test_filename={
					"D:\\D1\\UCR_TS_Archive_2015\\22_Datasets_SAX\\synthetic_control\\synthetic_control_TEST", 
					"D:\\D1\\UCR_TS_Archive_2015\\22_Datasets_SAX\\Gun_Point\\Gun_Point_TEST",
					"D:\\D1\\UCR_TS_Archive_2015\\22_Datasets_SAX\\CBF\\CBF_TEST",
					"D:\\D1\\UCR_TS_Archive_2015\\22_Datasets_SAX\\FaceAll\\FaceAll_TEST",
					"D:\\D1\\UCR_TS_Archive_2015\\22_Datasets_SAX\\OSULeaf\\OSULeaf_TEST",
					"D:\\D1\\UCR_TS_Archive_2015\\22_Datasets_SAX\\SwedishLeaf\\SwedishLeaf_TEST",
					"D:\\D1\\UCR_TS_Archive_2015\\22_Datasets_SAX\\50words\\50words_TEST",
					"D:\\D1\\UCR_TS_Archive_2015\\22_Datasets_SAX\\Trace\\Trace_TEST",
					"D:\\D1\\UCR_TS_Archive_2015\\22_Datasets_SAX\\Two_Patterns\\Two_Patterns_TEST",
					"D:\\D1\\UCR_TS_Archive_2015\\22_Datasets_SAX\\wafer\\wafer_TEST",
					"D:\\D1\\UCR_TS_Archive_2015\\22_Datasets_SAX\\FaceFour\\FaceFour_TEST",
					"D:\\D1\\UCR_TS_Archive_2015\\22_Datasets_SAX\\Lighting2\\Lighting2_TEST",
					"D:\\D1\\UCR_TS_Archive_2015\\22_Datasets_SAX\\Lighting7\\Lighting7_TEST",
					"D:\\D1\\UCR_TS_Archive_2015\\22_Datasets_SAX\\yoga\\yoga_TEST",
					"D:\\D1\\UCR_TS_Archive_2015\\22_Datasets_SAX\\Adiac\\Adiac_TEST",
					"D:\\D1\\UCR_TS_Archive_2015\\22_Datasets_SAX\\yoga\\yoga_TEST",
					"D:\\D1\\UCR_TS_Archive_2015\\22_Datasets_SAX\\FISH\\FISH_TEST",
					"D:\\D1\\UCR_TS_Archive_2015\\22_Datasets_SAX\\Beef\\Beef_TEST",
					"D:\\D1\\UCR_TS_Archive_2015\\22_Datasets_SAX\\Coffee\\Coffee_TEST",
					"D:\\D1\\UCR_TS_Archive_2015\\22_Datasets_SAX\\OliveOil\\OliveOil_TEST" 
					};
		int []paa_segment={16, 64, 64, 64, 16, 64, 32, 4, 64, 64, 128, 32, 128, 32, 64, 128, 128, 32, 4, 2};
		int []saxAlpha={10, 10, 10, 9, 9, 10, 10, 10, 10, 9, 7, 7, 8, 10, 10, 10, 10, 9, 5, 3};
		
		int corrected=0;
		for(int f=0; f< train_filename.length; f++){			
			List<sampleSeries>train_List=dataLoad(train_filename[f]);
			List<sampleSeries>test_List=dataLoad(test_filename[f]);
			
			/*List<Integer> paa_segment=new ArrayList<Integer>();
			paa_segment.add(0, 2);
			int j=0;
			while(paa_segment.get(j) < test_List.get(0).Attributes.size()/2)
			{
				j++;
				paa_segment.add(paa_segment.get(j-1)*2);
			}*/			
			
			Set<Integer>tLabel=new HashSet<Integer>();
			for(int i=0;i< train_List.size();i++){
				tLabel.add(train_List.get(i).cName);			
			}		
			corrected=0;
					for(int i=0;i< test_List.size();i++){			
						int predicted_cLabel = classification_algorithm(train_List, test_List.get(i).Attributes, paa_segment[f], saxAlpha[f]);			
						if(predicted_cLabel == test_List.get(i).cName) corrected = corrected + 1;
					}					
					System.out.println("*******************************************");
					System.out.println("The data set you tested has "+ tLabel.size() + " classes");		
					System.out.println("Training set is size of "+ train_List.size()+ " and test set is size of "+ test_List.size());
					System.out.println("Time series Length is "+ train_List.get(0).Attributes.size());
					System.out.println("Corrected Label "+ corrected);
					System.out.println("The error rate is "+ (double)(test_List.size() - corrected)/(double)test_List.size());	
			}			
		}
}

