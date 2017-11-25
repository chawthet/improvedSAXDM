package dim_reduction;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Stream;

import net.seninp.jmotif.distance.EuclideanDistance;
import net.seninp.jmotif.sax.SAXException;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.jmotif.sax.alphabet.Alphabet;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;

import org.apache.commons.lang3.ArrayUtils;

public class weightmeanCompare {

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
	   * Computes the mean value of timeseries.
	   * 
	   * @param series The timeseries.
	   * @return The mean value.
	   */
	  public static double mean(double[] series) {
	    double res = 0D;
	    int count = 0;
	    for (double tp : series) {
	      res += tp;
	      count += 1;

	    }
	    if (count > 0) {
	      return res / ((Integer) count).doubleValue();
	    }
	    return Double.NaN;
	  }

	
	/**
	   * Compute the variance of timeseries.
	   * 
	   * @param series The timeseries.
	   * @return The variance.
	   */
	  public static double var(double[] series) {
	    double res = 0D;
	    double mean = mean(series);
	    int count = 0;
	    for (double tp : series) {
	      res += (tp - mean) * (tp - mean);
	      count += 1;
	    }
	    if (count > 0) {
	      return res / ((Integer) (count - 1)).doubleValue();
	    }
	    return Double.NaN;
	  }
	/**
	   * Speed-optimized implementation.
	   * 
	   * @param series The timeseries.
	   * @return the standard deviation.
	   */
	  public static double stDev(double[] series) {
	    double num0 = 0D;
	    double sum = 0D;
	    int count = 0;
	    for (double tp : series) {
	      num0 = num0 + tp * tp;
	      sum = sum + tp;
	      count += 1;
	    }
	    double len = ((Integer) count).doubleValue();
	    return Math.sqrt((len * num0 - sum * sum) / (len * (len - 1)));
	  }	
	  
	  
	  public static int classification_algorithm(List<sampleSeries>train_List, List<Double> test_List, int paa_segment)
		{
			List<Result>innerList=new ArrayList<Result>();
			//TSProcessor tsp=new TSProcessor();
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
				
			}
			Collections.sort(innerList, new DistanceComparator());
			int test_cLabel=innerList.get(0).cName;
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
		String train_filename="/home/chawtzan/Documents/DM tools/UCR_TS_Archive_2015/22_Data_Set_for_testing/Lighting2/Lighting2_TRAIN";
		String test_filename="/home/chawtzan/Documents/DM tools/UCR_TS_Archive_2015/22_Data_Set_for_testing/Lighting2/Lighting2_TEST";
		List<sampleSeries>train_List=dataLoad(train_filename);
		List<sampleSeries>test_List=dataLoad(test_filename);
		int corrected=0;
		int paa_segment=128;
		Set<Integer>tLabel=new HashSet<Integer>();
		for(int i=0;i< train_List.size();i++){
			tLabel.add(train_List.get(i).cName);			
		}		
		for(int i=0;i< test_List.size();i++){			
			int predicted_cLabel = classification_algorithm(train_List, test_List.get(i).Attributes, paa_segment);			
			if(predicted_cLabel == test_List.get(i).cName) corrected = corrected + 1;
		}
		System.out.println("The data set you tested has "+ tLabel.size() + " classes");		
		System.out.println("Training set is size of "+ train_List.size()+ " and test set is size of "+ test_List.size());
		System.out.println("Time series Length is "+ train_List.get(0).Attributes.size());
		System.out.println("Corrected Label "+ corrected);
		System.out.println("The error rate is "+ (double)(test_List.size() - corrected)/(double)test_List.size());
	}

}
