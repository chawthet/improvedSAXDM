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
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.stream.Stream;

import net.seninp.jmotif.sax.SAXException;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.jmotif.sax.alphabet.Alphabet;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;

public class ucr_TSeries1NNSAXSD_Execfun{

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
	public static double[] sdDist(List<sampleSeries>train_List, List<Double>test_List, int paaSegment)
	{
		TSProcessor tsp=new TSProcessor();		
		double [] sdDist_a=new double[train_List.size()];
		for(int i=0;i< train_List.size();i++){
			double []tempArray=new double[train_List.get(i).Attributes.size()];
			double []tempArray1=new double[test_List.size()];
			 for(int j=0;j< train_List.get(i).Attributes.size();j++)
			 {
				 tempArray[j]=train_List.get(i).Attributes.get(j);
				 tempArray1[j]=test_List.get(j);
			 }
		
		double[]tSD_value=SD(tsp.znorm(tempArray, 0.00001), paaSegment);
		double[]qSD_value=SD(tsp.znorm(tempArray1, 0.00001), paaSegment);
		double sdDist1=0;
		for(int d=0;d< tSD_value.length;d++)
			sdDist1+=Math.pow((qSD_value[d]- tSD_value[d]), 2);		
			//sdDist1+=((double)paaSegment/(double)test_List.size())*Math.pow((qSD_value[d]- tSD_value[d]), 2);		
			sdDist_a[i]=sdDist1;
	}
		return sdDist_a;
	}
	public static double[] saxDist(List<sampleSeries>train_List, List<Double>test_List, int paaSegment, int saxAlpha)
	{
		SAXProcessor saxp=new SAXProcessor();
		Alphabet normalA = new NormalAlphabet();
		double [] saxDist=new double[train_List.size()];
		for(int i=0;i< train_List.size();i++){
			double []tempArray=new double[train_List.get(i).Attributes.size()];
			double []tempArray1=new double[test_List.size()];
			 for(int j=0;j< train_List.get(i).Attributes.size();j++)
			 {
				 tempArray[j]=train_List.get(i).Attributes.get(j);
				 tempArray1[j]=test_List.get(j);
			 }
		try {
			char[]tSAX_List = saxp.ts2string(tempArray, paaSegment, normalA.getCuts(saxAlpha), 0.00001);
			char[]qSAX_List = saxp.ts2string(tempArray1, paaSegment, normalA.getCuts(saxAlpha), 0.00001);
			saxDist[i]=saxp.saxMinDist_update(qSAX_List, tSAX_List, normalA.getDistanceMatrix(saxAlpha), test_List.size(), paaSegment);
			} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			}
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
	
	public static void main(String[] args) {
		ResourceBundle.clearCache();
		String train_filename="D:\\D1\\UCR_TS_Archive_2015\\22_Datasets_SAX\\Lighting7\\Lighting7_TRAIN";
		String test_filename="D:\\D1\\UCR_TS_Archive_2015\\22_Datasets_SAX\\Lighting7\\Lighting7_TEST";
		int corrected=0;
		int paa_segment=4;
		int saxAlpha=10;
		long totaltime=0;
		double temp_dist=0;
		double[]sdDist;
		double[]saxDist;
		//double best_so_far=Double.POSITIVE_INFINITY;
		//int test_cLabel = -99999;
		//for (int z=0;z< 25; z++){
			corrected=0;
			long startTime=System.currentTimeMillis();
			List<sampleSeries>train_List=dataLoad(train_filename);
			List<sampleSeries>test_List=dataLoad(test_filename);
			
			
				for(int j=0;j< test_List.size();j++){
					sdDist=new double[train_List.size()];
					saxDist=new double[train_List.size()];
					double best_so_far=Double.POSITIVE_INFINITY;
					int test_cLabel = -99999;
					//double totalDist=0;
					sdDist=sdDist(train_List, test_List.get(j).Attributes, paa_segment);
					saxDist=saxDist(train_List, test_List.get(j).Attributes,paa_segment, saxAlpha);
					for(int i=0;i< sdDist.length;i++)
					{	
						//System.out.println(sdDist[i]+" : "+ saxDist[i]);
						double totalDist= Math.sqrt(((double)test_List.size()/(double)paa_segment))*Math.sqrt(saxDist[i]+sdDist[i]);
						if(totalDist < best_so_far){
							test_cLabel=train_List.get(i).cName;
							best_so_far=totalDist;
							}
					}
					//System.out.println("Test cLable"+ test_cLabel);
					if(test_cLabel == test_List.get(j).cName) corrected = corrected + 1;
					
				}
				temp_dist=(double)(test_List.size() - corrected)/(double)test_List.size();
				long endTime = System.currentTimeMillis();
				long elapsedTimeInMillis_1 = endTime - startTime;	
				totaltime=elapsedTimeInMillis_1;
				ResourceBundle.clearCache();
		//}	
		
		System.out.println("*******************************************");
		System.out.println("Corrected Label "+ corrected +"Error Rate: "+ temp_dist);
		System.out.println("Total Execution time for segment size: "+ paa_segment + " : "+ totaltime + "msec");
	
}
}
