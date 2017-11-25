package dim_reduction;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Collator;
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

import dim_reduction.ucr_TSeries1NNSAXTD_Test.DistanceComparator;
import dim_reduction.ucr_TSeries1NNSAXTD_Test.Result;
import dim_reduction.ucr_TSeries1NNSAXTD_Test.Result1;
import net.seninp.jmotif.distance.EuclideanDistance;
import net.seninp.jmotif.sax.SAXException;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.jmotif.sax.alphabet.Alphabet;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;

public class ucr_TSeries1NNSAXSD_Test {

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
	  
	 //find Standard Deviation values for each paa segment
	public static double[] SD(double []ts, int paaSize){
		//fix the length
		int len=ts.length;
		double []SD_value;
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
			return Arrays.copyOf(ts, ts.length);
			/*SD_value=new double[0];
			SD_value[0]=stDev(ts);
			return SD_value;*/
		}
		else{		
			SD_value=new double[paaSize];
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
				/*double ex=0.0;
				double ex2=0.0;
				for(double e:segment){
					ex+= e;
					ex2+=e*e;				
				}
				double mean=ex/segment.length;
				double std=ex2/segment.length;
				SD_value[i]=Math.sqrt(std-mean*mean);*/
				SD_value[i]=stDev(segment);			
			}
		}		
		return SD_value;
	}	
	public static int classification_algorithm(List<sampleSeries>train_List, List<Double> test_List, int paa_segment, int saxAlpha)
	{
		List<Result>innerList=new ArrayList<Result>();		
		SAXProcessor saxp=new SAXProcessor();
		TSProcessor tsp=new TSProcessor();
		Alphabet normalA = new NormalAlphabet();
		double best_so_far=Double.POSITIVE_INFINITY;
		int test_cLabel = -99999;
		//Transform train_List to SAX List
		//List<saxSeries>trainSAX_List=new ArrayList<saxSeries>();		
		for(int i=0;i< train_List.size();i++){
			Double []tempArray=new Double[train_List.get(i).Attributes.size()];
			Double []tempArray1=new Double[test_List.size()];
			train_List.get(i).Attributes.toArray(tempArray);
			test_List.toArray(tempArray1);
			char[] tSAX_List;
			char[] qSAX_List;
			double[]tSD_value;
			double[]qSD_value;
			
			//char[][]tSD_char;
			//char[][]qSD_char;
			try {
				//SAX transformation
				tSAX_List = saxp.ts2string(ArrayUtils.toPrimitive(tempArray), paa_segment, normalA.getCuts(saxAlpha), 0.00001);
				qSAX_List = saxp.ts2string(ArrayUtils.toPrimitive(tempArray1), paa_segment, normalA.getCuts(saxAlpha), 0.00001);
				
				
				tSD_value=SD(tsp.znorm(ArrayUtils.toPrimitive(tempArray), 0.00001), paa_segment);
				qSD_value=SD(tsp.znorm(ArrayUtils.toPrimitive(tempArray1), 0.00001), paa_segment);
				
				double SDDist=0;
				for(int d=0;d< tSD_value.length;d++)
				{
					//tSD_char[d][1]=tsp.num2char(tSD_value[d], normalA.getCuts(saxAlpha));
					//tSD_char[d][0]=tSAX_List[d];
					
					//qSD_char[d][1]=tsp.num2char(qSD_value[d], normalA.getCuts(saxAlpha));
					//qSD_char[d][0]=qSAX_List[d];
					
					
					//calculate distance
					// saxDist +=saxp.EsaxMinDist(qSD_char[d], tSD_char[d], normalA.getDistanceMatrix(saxAlpha));	//test_List.size is time series Leng
					//SDDist+=((double)paa_segment/(double)test_List.size())*Math.pow((qSD_value[d]- tSD_value[d]), 2);
					SDDist+=Math.pow((qSD_value[d]- tSD_value[d]), 2);
					
				}
				double saxDist=saxp.saxMinDist_update(qSAX_List, tSAX_List, normalA.getDistanceMatrix(saxAlpha), test_List.size(), paa_segment);
				double fsaxDist=Math.sqrt(((double)test_List.size()/(double)paa_segment))*Math.sqrt(saxDist+SDDist);
				if (fsaxDist < best_so_far) {
					test_cLabel=train_List.get(i).cName;
					best_so_far=fsaxDist;
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
		
		String train_filename="D:\\D1\\UCR_TS_Archive_2015\\22_Datasets_SAX\\OliveOil\\OliveOil_TRAIN";
		String test_filename="D:\\D1\\UCR_TS_Archive_2015\\22_Datasets_SAX\\OliveOil\\OliveOil_TEST";
		long startTime=System.currentTimeMillis();
		List<sampleSeries>train_List=dataLoad(train_filename);
		List<sampleSeries>test_List=dataLoad(test_filename);
		List<Result>errRate_List=new ArrayList<Result>();
		int corrected=0;
		List<Integer> paa_segment=new ArrayList<Integer>();
		paa_segment.add(0, 2);
		int j=0;
		while(paa_segment.get(j) < test_List.get(0).Attributes.size()/2)
		{
			j++;
			paa_segment.add(paa_segment.get(j-1)*2);
		}
		int [] saxAlpha={3, 4, 5, 6, 7, 8, 9, 10};
		//int [] saxAlpha={10};
		/*Set<Integer>tLabel=new HashSet<Integer>();
		for(int i=0;i< train_List.size();i++){
			tLabel.add(train_List.get(i).cName);			
		}*/
		for (int s=0;s< paa_segment.size(); s++){
			for(int a=0;a < saxAlpha.length;a++){
				corrected=0;
				for(int i=0;i< test_List.size();i++){			
					int predicted_cLabel = classification_algorithm(train_List, test_List.get(i).Attributes, paa_segment.get(s), saxAlpha[a]);			
					if(predicted_cLabel == test_List.get(i).cName) corrected = corrected + 1;
				}
				double temp_dist=(double)(test_List.size() - corrected)/(double)test_List.size();
				long endTime = System.currentTimeMillis();
				long elapsedTimeInMillis_1 = endTime - startTime;
				errRate_List.add(new Result (temp_dist, paa_segment.get(s), saxAlpha[a], elapsedTimeInMillis_1));				
		}			
	}
		Collections.sort(errRate_List, new DistanceComparator());
		System.out.println("Result List");
		for(int r=0;r< errRate_List.size();r++)
			System.out.println(errRate_List.get(r).distance +", "+ errRate_List.get(r).paa_segment+", "+ errRate_List.get(r).sax_alpha+", "+errRate_List.get(r).tms);	
	}
}
