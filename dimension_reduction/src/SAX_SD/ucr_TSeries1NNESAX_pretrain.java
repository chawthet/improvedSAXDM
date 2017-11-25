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

import net.seninp.jmotif.distance.EuclideanDistance;
import net.seninp.jmotif.sax.SAXException;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.jmotif.sax.alphabet.Alphabet;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;

public class ucr_TSeries1NNESAX_pretrain {

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
	/*public static int classification_algorithm(List<sampleSeries>train_List, List<Double> test_List, int paa_segment, int saxAlpha)
	{
		List<Result>innerList=new ArrayList<Result>();		
		SAXProcessor saxp=new SAXProcessor();
		TSProcessor tsp=new TSProcessor();
		Alphabet normalA = new NormalAlphabet();
		
		//Transform train_List to SAX List
		//List<saxSeries>trainSAX_List=new ArrayList<saxSeries>();		
		for(int i=0;i< train_List.size();i++){
			Double []tempArray=new Double[train_List.get(i).Attributes.size()];
			Double []tempArray1=new Double[test_List.size()];
			train_List.get(i).Attributes.toArray(tempArray);
			test_List.toArray(tempArray1);
			//char[] tSAX_List;
			//char[] qSAX_List;
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
				tmaxmin_value=max_min(tsp.znorm(ArrayUtils.toPrimitive(tempArray), 0.0001), paa_segment);
				qmaxmin_value=max_min(tsp.znorm(ArrayUtils.toPrimitive(tempArray1), 0.0001), paa_segment);
				
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
				
				innerList.add(new Result(fsaxDist, train_List.get(i).cName));
				//trainSAX_List.add(new saxSeries(train_List.get(i).cName, tSAX_List));
			} catch (SAXException e) {				
				e.printStackTrace();
			}
			
		}
		Collections.sort(innerList, new DistanceComparator());
		int test_cLabel=innerList.get(0).cName;
		return test_cLabel;		
	}	*/
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
		String train_filename="D:\\D1\\UCR_TS_Archive_2015\\22_Datasets_SAX\\wafer\\wafer_TRAIN";
		String test_filename="D:\\D1\\UCR_TS_Archive_2015\\22_Datasets_SAX\\wafer\\wafer_TEST";
		SAXProcessor saxp=new SAXProcessor();
		TSProcessor tsp=new TSProcessor();
		Alphabet normalA = new NormalAlphabet();
		
		double[][]tmaxmin_value;
		double[][]qmaxmin_value;
		char[][]tmaxmeanmin_char;
		char[][]qmaxmeanmin_char;
		long totaltime=0;
		int corrected=0;
		int paa_segment=64;
		int saxAlpha=10;
			
		for(int s=0;s< 25; s++){			
		ArrayList<double[][]>train_arr=new ArrayList<double[][]>();			
		List<sampleSeries>train_List=dataLoad(train_filename);		
		for (int i=0;i< train_List.size();i++){
			Double []tempArray=new Double[train_List.get(i).Attributes.size()];
			train_List.get(i).Attributes.toArray(tempArray);
			tmaxmin_value=max_min(tsp.znorm(ArrayUtils.toPrimitive(tempArray), 0.00001), paa_segment);
			train_arr.add(tmaxmin_value);
		}
		long startTime=System.currentTimeMillis();	
		List<sampleSeries>test_List=dataLoad(test_filename);
		corrected=0;
		for(int i=0;i< test_List.size();i++){			
			Double []tempArray1=new Double[test_List.get(i).Attributes.size()];
			test_List.get(i).Attributes.toArray(tempArray1);			
			//double best_so_far=Double.POSITIVE_INFINITY;
			int test_cLabel=-99999;
			qmaxmin_value=max_min(tsp.znorm(ArrayUtils.toPrimitive(tempArray1), 0.00001), paa_segment);
			tmaxmeanmin_char = new char[paa_segment][3];
			qmaxmeanmin_char = new char[paa_segment][3];
			List<Result>innerList=new ArrayList<Result>();
			for (int j=0;j < train_List.size();j++){
				double saxDist=0;
				
				for(int d=0;d< train_arr.get(j).length;d++)
				{
					try {
						tmaxmeanmin_char[d][0]=tsp.num2char(train_arr.get(j)[d][0], normalA.getCuts(saxAlpha));
						tmaxmeanmin_char[d][1]=tsp.num2char(train_arr.get(j)[d][1], normalA.getCuts(saxAlpha));
						tmaxmeanmin_char[d][2]=tsp.num2char(train_arr.get(j)[d][2], normalA.getCuts(saxAlpha));
						qmaxmeanmin_char[d][0]=tsp.num2char(qmaxmin_value[d][0], normalA.getCuts(saxAlpha));
						qmaxmeanmin_char[d][1]=tsp.num2char(qmaxmin_value[d][1], normalA.getCuts(saxAlpha));
						qmaxmeanmin_char[d][2]=tsp.num2char(qmaxmin_value[d][2], normalA.getCuts(saxAlpha));
						//calculate distance
						 saxDist+=saxp.EsaxMinDist(qmaxmeanmin_char[d], tmaxmeanmin_char[d], normalA.getDistanceMatrix(saxAlpha));	//test_List.size is time series Length
					} catch (SAXException e) {
						e.printStackTrace();
					}					
				}
				double fsaxDist=((double)test_List.size()/(double)paa_segment)*saxDist;
				innerList.add(new Result(fsaxDist,train_List.get(j).cName));				
				/*if(fsaxDist < best_so_far){
					test_cLabel=train_List.get(j).cName;
					best_so_far=fsaxDist;				
				}*/
				
		}
			//int predicted_cLabel = classification_algorithm(train_List, test_List.get(i).Attributes, paa_segment[f], saxAlpha[f]);			
			Collections.sort(innerList,new DistanceComparator());
			test_cLabel=innerList.get(0).cName;
			if(test_cLabel == test_List.get(i).cName) corrected = corrected + 1;
		}
			System.out.println("Corrected Label "+ corrected);
			System.out.println("The error rate is "+ (double)(test_List.size() - corrected)/(double)test_List.size());
			long endTime = System.currentTimeMillis();
			long elapsedTimeInMillis_1 =endTime - startTime;
			totaltime+=elapsedTimeInMillis_1;
			//System.out.println("Time for calculation: "+ elapsedTimeInMillis_1 + " ms");
		}
			System.out.println("Time for calculation: "+ (totaltime/25.0)/(1000.0) + " ms");			
		}		
	}

