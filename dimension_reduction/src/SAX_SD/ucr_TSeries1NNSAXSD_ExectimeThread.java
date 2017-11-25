package SAX_SD;

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

import net.seninp.jmotif.distance.EuclideanDistance;
import net.seninp.jmotif.sax.SAXException;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.jmotif.sax.alphabet.Alphabet;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;

public class ucr_TSeries1NNSAXSD_ExectimeThread{	
	
	
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
	public  double sd_dist(double []tempArray, double[]tempArray1, int paa_segment, int testsize){
		TSProcessor tsp=new TSProcessor();
		double SDDist=0;
			double []tSD_value=SD(tsp.znorm(tempArray, 0.00001), paa_segment);
			double []qSD_value=SD(tsp.znorm(tempArray1, 0.00001), paa_segment);			
			//calculate distance			
			for(int d=0;d< tSD_value.length;d++)
			{
				SDDist+=((double)paa_segment/(double)testsize)*Math.pow((qSD_value[d]- tSD_value[d]), 2);				
			}			
			return SDDist;
	}
	public  double sax_dist(double []tempArray, double[]tempArray1, int paa_segment, int saxAlpha,int testsize){
		SAXProcessor saxp=new SAXProcessor();
		Alphabet normalA = new NormalAlphabet();
		double saxDist=0;
		
		try {
			char[] tSAX_List = saxp.ts2string(tempArray, paa_segment, normalA.getCuts(saxAlpha), 0.00001);
			char[]qSAX_List = saxp.ts2string(tempArray1, paa_segment, normalA.getCuts(saxAlpha), 0.00001);
			saxDist=saxp.saxMinDist_update(qSAX_List, tSAX_List, normalA.getDistanceMatrix(saxAlpha), testsize, paa_segment);
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return saxDist;
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
			static class DistanceComparator implements Comparator<Result> {
				@Override
				public int compare(Result a, Result b) {					
					return a.distance < b.distance ? -1 : a.distance == b.distance ? 0 : 1;					
				}
			}
	
	
}
