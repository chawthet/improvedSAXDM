package SAX_SD;

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
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;

import net.seninp.jmotif.distance.EuclideanDistance;
import net.seninp.jmotif.sax.SAXException;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.jmotif.sax.alphabet.Alphabet;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;

public class ucr_TSeries1NNSAX_pretrain {

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
	
	/*public static int classification_algorithm(List<sampleSeries>train_List, List<Double> test_List, int paa_segment)
	{
		SAXProcessor saxp=new SAXProcessor();		
		Alphabet normalA = new NormalAlphabet();
		double bestsofar=Double.POSITIVE_INFINITY;
		int test_cLabel=-99999;		
		for(int i=0;i< train_List.size();i++){
			Double []tempArray=new Double[train_List.get(i).Attributes.size()];
			Double []tempArray1=new Double[test_List.size()];
			train_List.get(i).Attributes.toArray(tempArray);
			test_List.toArray(tempArray1);
			char[] tSAX_List;
			char[] qSAX_List;
			try {
				//segment-wise normalization
				//tSAX_List = saxp.ts2string(ArrayUtils.toPrimitive(tempArray), paa_segment, normalA.getCuts(10), 0.0001);
				//qSAX_List = saxp.ts2string(ArrayUtils.toPrimitive(tempArray1), paa_segment, normalA.getCuts(10), 0.0001);
				
				//normalization first for the whole series
				tSAX_List=saxp.ts2saxNormalizefirst(ArrayUtils.toPrimitive(tempArray), paa_segment, normalA.getCuts(10), 0.00001);
				qSAX_List=saxp.ts2saxNormalizefirst(ArrayUtils.toPrimitive(tempArray1), paa_segment, normalA.getCuts(10), 0.00001);
				
				double saxDist=saxp.saxMinDist(qSAX_List, tSAX_List, normalA.getDistanceMatrix(10), test_List.size(), paa_segment);		//test_List.size is time series Length
				if(saxDist < bestsofar)
				{
					test_cLabel=train_List.get(i).cName;
					bestsofar=saxDist;
				}
				
			} catch (SAXException e) {				
				e.printStackTrace();
			}			
		}		
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
		int paa_segment=64;			
		SAXProcessor saxp=new SAXProcessor();		
		Alphabet normalA = new NormalAlphabet();		
		char[] tSAX_List;
		char[] qSAX_List = null;
		long totaltime=0;
		
		for(int s=0;s< 25;s++){	
			int corrected=0;
		ArrayList<char[]>trainpre=new ArrayList<char[]>();
		List<sampleSeries>train_List=dataLoad(train_filename);
		for(int i=0;i< train_List.size();i++){
			Double []tempArray=new Double[train_List.get(i).Attributes.size()];
			train_List.get(i).Attributes.toArray(tempArray);
			try {
				tSAX_List = saxp.ts2string(ArrayUtils.toPrimitive(tempArray), paa_segment, normalA.getCuts(10), 0.00001);
				trainpre.add(tSAX_List);
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		long startTime=System.currentTimeMillis();	
			List<sampleSeries>test_List=dataLoad(test_filename);		
			for(int j=0;j< test_List.size();j++)
			{
				double bestsofar=Double.POSITIVE_INFINITY;
				int test_cLabel=-99999;		
				Double []tempArray1=new Double[test_List.get(j).Attributes.size()];
				test_List.get(j).Attributes.toArray(tempArray1);
				try {
					qSAX_List = saxp.ts2string(ArrayUtils.toPrimitive(tempArray1), paa_segment, normalA.getCuts(10), 0.00001);
				} catch (SAXException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				List<Result>innerList=new ArrayList<Result>();
				for(int i=0;i<train_List.size();i++)
				{
					try {					
						
						double saxDist=saxp.saxMinDist(qSAX_List, trainpre.get(i), normalA.getDistanceMatrix(10), test_List.size(), paa_segment);
						//test_List.size is time series Length
						innerList.add(new Result(saxDist,train_List.get(i).cName));
						
						/*if(saxDist < bestsofar)
						{
							test_cLabel=train_List.get(i).cName;
							bestsofar=saxDist;
						}*/
						
					} catch (SAXException e) {				
						e.printStackTrace();
					}
				}
				Collections.sort(innerList,new DistanceComparator());
				test_cLabel=innerList.get(0).cName;
				if(test_cLabel==test_List.get(j).cName)corrected=corrected+1;	
											
			}
			long endTime = System.currentTimeMillis();
			long elapsedTimeInMillis_1 = endTime - startTime;	
			System.out.println("Corrected Label "+ corrected);
			System.out.println("The error rate is "+ (double)(test_List.size() - corrected)/(double)test_List.size());				
			totaltime+=elapsedTimeInMillis_1;			
		}
		System.out.println("Total Time for calculation of "+ "w size: "+ paa_segment+" = "+ (totaltime/25.0)/(1000.0) + " s");
	}
}

