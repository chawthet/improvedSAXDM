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

import net.seninp.jmotif.sax.SAXException;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.jmotif.sax.alphabet.Alphabet;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;

/**
 * Implement proposed method SAXSD scheme 
 * check classification accuracy using 1NN ED distance
 * for UCR time series data sets
 * check execution time for each data set
 * The best parameters (segment size and alphabet size)were shown in published paper.
 * @author chawt
 *
 * Pre-training 
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
public class ucr_TSeries1NNSAXSD_pretrain{

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
	
	public static void main(String[] args) {
		if(args.length < 4){
			System.out.println("Invalid number of arguments OR types of parameters");
			System.exit(-1);
		}
		String train_filename= args[0];
		String test_filename=args[1];
		int paa_segment=Integer.parseInt(args[2]);
		int saxAlpha=Integer.parseInt(args[3]);
		SAXProcessor saxp=new SAXProcessor();
		TSProcessor tsp=new TSProcessor();
		Alphabet normalA = new NormalAlphabet();
					
		long totaltime=0;
		char[] tSAX_List;
		char[] qSAX_List = null;
		double[]tSD_value;
		double[]qSD_value;	
		
		ArrayList<char[]>tranf_List=new ArrayList<char[]>();
		ArrayList<double[]>tranf_List1=new ArrayList<double[]>();
		int corrected=0;
		long startTime=System.currentTimeMillis();
		
		List<sampleSeries>train_List=dataLoad(train_filename);
		for(int i=0;i< train_List.size();i++){
			Double []tempArray=new Double[train_List.get(i).Attributes.size()];
			train_List.get(i).Attributes.toArray(tempArray);
			try {
				tSAX_List = saxp.ts2string(ArrayUtils.toPrimitive(tempArray), paa_segment, normalA.getCuts(saxAlpha), 0.00001);
				tSD_value=SD(tsp.znorm(ArrayUtils.toPrimitive(tempArray), 0.00001), paa_segment);
				tranf_List.add(tSAX_List);
				tranf_List1.add(tSD_value);
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
			List<sampleSeries>test_List=dataLoad(test_filename);			
			for(int i=0;i< test_List.size();i++){
				double best_so_far=Double.POSITIVE_INFINITY;
				int test_cLabel = -99999;
				Double []tempArray1=new Double[test_List.get(i).Attributes.size()];
				test_List.get(i).Attributes.toArray(tempArray1);
				try {
					qSAX_List = saxp.ts2string(ArrayUtils.toPrimitive(tempArray1), paa_segment, normalA.getCuts(saxAlpha), 0.00001);
				} catch (SAXException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			 	qSD_value=SD(tsp.znorm(ArrayUtils.toPrimitive(tempArray1), 0.00001), paa_segment);
				for(int j=0;j< train_List.size();j++){					
					try {						
					double SDDist=0;
					for(int s=0;s< qSD_value.length;s++){
						//SDDist+=((double)paa_segment/(double)test_List.size())*Math.pow((qSD_value[s]- tranf_List1.get(j)[s]), 2);
						SDDist+=Math.pow((qSD_value[s]- tranf_List1.get(j)[s]), 2);	
					}
					double saxDist = saxp.saxMinDist_update(qSAX_List, tranf_List.get(j), normalA.getDistanceMatrix(saxAlpha), test_List.size(), paa_segment);
					double fsaxDist=Math.sqrt(((double)test_List.size()/(double)paa_segment))*Math.sqrt(saxDist+SDDist);
					if (fsaxDist < best_so_far) {
						test_cLabel=train_List.get(j).cName;
						best_so_far=fsaxDist;
					}
					}
					catch (SAXException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if(test_cLabel==test_List.get(i).cName)corrected=corrected+1;				
			}
				double temp_dist=(double)(test_List.size() - corrected)/(double)test_List.size();
				long endTime = System.currentTimeMillis();
				long elapsedTimeInMillis_1 = endTime - startTime;	
				totaltime+=elapsedTimeInMillis_1;
				
		System.out.println("*******************************************");
		System.out.println("Corrected Label "+ corrected +"\nError Rate: "+ temp_dist);
		
		System.out.println("\nExecution Time: "+totaltime+" ms");
	}
}
