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
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;

import dim_reduction.ucr_TSeries1NNESAX_Test.DistanceComparator;
import dim_reduction.ucr_TSeries1NNESAX_Test.Result;
import dim_reduction.ucr_TSeries1NNESAX_Test.Result1;
import net.seninp.jmotif.distance.EuclideanDistance;
import net.seninp.jmotif.sax.SAXException;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.jmotif.sax.alphabet.Alphabet;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;

public class ucr_TSeries1NNSAX_Test {

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
	
	public static int classification_algorithm(List<sampleSeries>train_List, List<Double> test_List, int paa_segment)
	{
		List<Result>innerList=new ArrayList<Result>();
		//TSProcessor tsp=new TSProcessor();
		//EuclideanDistance edDist=new EuclideanDistance();
		SAXProcessor saxp=new SAXProcessor();		
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
			try {
				//segment-wise normalization
				tSAX_List = saxp.ts2string(ArrayUtils.toPrimitive(tempArray), paa_segment, normalA.getCuts(10), 0.00001);
			    qSAX_List = saxp.ts2string(ArrayUtils.toPrimitive(tempArray1), paa_segment, normalA.getCuts(10), 0.00001);
				
				//normalization first for the whole series
				//tSAX_List=saxp.ts2saxNormalizefirst(ArrayUtils.toPrimitive(tempArray), paa_segment, normalA.getCuts(10), 0.00001);
				//qSAX_List=saxp.ts2saxNormalizefirst(ArrayUtils.toPrimitive(tempArray1), paa_segment, normalA.getCuts(10), 0.00001);
				
				double saxDist=saxp.saxMinDist(qSAX_List, tSAX_List, normalA.getDistanceMatrix(10), test_List.size(), paa_segment);		//test_List.size is time series Length
				if(saxDist < bestsofar)
				{
					test_cLabel=train_List.get(i).cName;
					bestsofar=saxDist;
				}
				//innerList.add(new Result(saxDist, train_List.get(i).cName));
				//trainSAX_List.add(new saxSeries(train_List.get(i).cName, tSAX_List));
			} catch (SAXException e) {				
				e.printStackTrace();
			}
			
		}
		//Collections.sort(innerList, new DistanceComparator());
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
		for (int y=0;y<25;y++){
		String []train_filename={
				//"/home/chawtzan/Documents/DM tools/UCR_TS_Archive_2015/22_Data_Set_for_testing/synthetic_control/synthetic_control_TRAIN", 
				//"/home/chawtzan/Documents/DM tools/UCR_TS_Archive_2015/22_Data_Set_for_testing/Gun_Point/Gun_Point_TRAIN",
				//"/home/chawtzan/Documents/DM tools/UCR_TS_Archive_2015/22_Data_Set_for_testing/CBF/CBF_TRAIN",
				//"/home/chawtzan/Documents/DM tools/UCR_TS_Archive_2015/22_Data_Set_for_testing/FaceAll/FaceAll_TRAIN",
				//"/home/chawtzan/Documents/DM tools/UCR_TS_Archive_2015/22_Data_Set_for_testing/OSULeaf/OSULeaf_TRAIN",
				//"/home/chawtzan/Documents/DM tools/UCR_TS_Archive_2015/22_Data_Set_for_testing/SwedishLeaf/SwedishLeaf_TRAIN",
				//"/home/chawtzan/Documents/DM tools/UCR_TS_Archive_2015/22_Data_Set_for_testing/50words/50words_TRAIN",
				//"/home/chawtzan/Documents/DM tools/UCR_TS_Archive_2015/22_Data_Set_for_testing/Trace/Trace_TRAIN",
				//"/home/chawtzan/Documents/DM tools/UCR_TS_Archive_2015/22_Data_Set_for_testing/Two_Patterns/Two_Patterns_TRAIN",
				//"/home/chawtzan/Documents/DM tools/UCR_TS_Archive_2015/22_Data_Set_for_testing/wafer/wafer_TRAIN",
				//"/home/chawtzan/Documents/DM tools/UCR_TS_Archive_2015/22_Data_Set_for_testing/FaceFour/FaceFour_TRAIN",
				//"/home/chawtzan/Documents/DM tools/UCR_TS_Archive_2015/22_Data_Set_for_testing/Lighting2/Lighting2_TRAIN",
				//"/home/chawtzan/Documents/DM tools/UCR_TS_Archive_2015/22_Data_Set_for_testing/Lighting7/Lighting7_TRAIN",
				"D:\\D1\\UCR_TS_Archive_2015\\22_Datasets_SAX\\yoga\\yoga_TRAIN",
				//"/home/chawtzan/Documents/DM tools/UCR_TS_Archive_2015/22_Data_Set_for_testing/Adiac/Adiac_TRAIN",
				//"/home/chawtzan/Documents/DM tools/UCR_TS_Archive_2015/22_Data_Set_for_testing/yoga/yoga_TRAIN",
				//"/home/chawtzan/Documents/DM tools/UCR_TS_Archive_2015/22_Data_Set_for_testing/FISH/FISH_TRAIN",
				//"/home/chawtzan/Documents/DM tools/UCR_TS_Archive_2015/22_Data_Set_for_testing/Beef/Beef_TRAIN",
				//"/home/chawtzan/Documents/DM tools/UCR_TS_Archive_2015/22_Data_Set_for_testing/Coffee/Coffee_TRAIN",
				//"/home/chawtzan/Documents/DM tools/UCR_TS_Archive_2015/22_Data_Set_for_testing/OliveOil/OliveOil_TRAIN" 
				};
		String []test_filename={
				//"/home/chawtzan/Documents/DM tools/UCR_TS_Archive_2015/22_Data_Set_for_testing/synthetic_control/synthetic_control_TEST", 
				//"/home/chawtzan/Documents/DM tools/UCR_TS_Archive_2015/22_Data_Set_for_testing/Gun_Point/Gun_Point_TEST",
				//"/home/chawtzan/Documents/DM tools/UCR_TS_Archive_2015/22_Data_Set_for_testing/CBF/CBF_TEST",
				//"/home/chawtzan/Documents/DM tools/UCR_TS_Archive_2015/22_Data_Set_for_testing/FaceAll/FaceAll_TEST",
				//"/home/chawtzan/Documents/DM tools/UCR_TS_Archive_2015/22_Data_Set_for_testing/OSULeaf/OSULeaf_TEST",
				//"/home/chawtzan/Documents/DM tools/UCR_TS_Archive_2015/22_Data_Set_for_testing/SwedishLeaf/SwedishLeaf_TEST",
				//"/home/chawtzan/Documents/DM tools/UCR_TS_Archive_2015/22_Data_Set_for_testing/50words/50words_TEST",
				//"/home/chawtzan/Documents/DM tools/UCR_TS_Archive_2015/22_Data_Set_for_testing/Trace/Trace_TEST",
				//"/home/chawtzan/Documents/DM tools/UCR_TS_Archive_2015/22_Data_Set_for_testing/Two_Patterns/Two_Patterns_TEST",
				//"/home/chawtzan/Documents/DM tools/UCR_TS_Archive_2015/22_Data_Set_for_testing/wafer/wafer_TEST",
				//"/home/chawtzan/Documents/DM tools/UCR_TS_Archive_2015/22_Data_Set_for_testing/FaceFour/FaceFour_TEST",
				//"/home/chawtzan/Documents/DM tools/UCR_TS_Archive_2015/22_Data_Set_for_testing/Lighting2/Lighting2_TEST",
				//"/home/chawtzan/Documents/DM tools/UCR_TS_Archive_2015/22_Data_Set_for_testing/Lighting7/Lighting7_TEST",
				"D:\\D1\\UCR_TS_Archive_2015\\22_Datasets_SAX\\yoga\\yoga_TEST",
				//"/home/chawtzan/Documents/DM tools/UCR_TS_Archive_2015/22_Data_Set_for_testing/Adiac/Adiac_TEST",
				//"/home/chawtzan/Documents/DM tools/UCR_TS_Archive_2015/22_Data_Set_for_testing/yoga/yoga_TEST",
				//"/home/chawtzan/Documents/DM tools/UCR_TS_Archive_2015/22_Data_Set_for_testing/FISH/FISH_TEST",
				//"/home/chawtzan/Documents/DM tools/UCR_TS_Archive_2015/22_Data_Set_for_testing/Beef/Beef_TEST",
				//"/home/chawtzan/Documents/DM tools/UCR_TS_Archive_2015/22_Data_Set_for_testing/Coffee/Coffee_TEST",
				//"/home/chawtzan/Documents/DM tools/UCR_TS_Archive_2015/22_Data_Set_for_testing/OliveOil/OliveOil_TEST" 
				};
		
		//int []paa_segment={2,4,8,16,32,64};	
		int [] saxAlpha={10};
		for(int f=0;f< train_filename.length;f++){
			List<Result>errRate_List=new ArrayList<Result>();
			long startTime=System.nanoTime();	
			List<sampleSeries>train_List=dataLoad(train_filename[f]);
			List<sampleSeries>test_List=dataLoad(test_filename[f]);		
			int corrected=0;
			List<Integer> paa_segment=new ArrayList<Integer>();
			paa_segment.add(0, 2);
			int j=0;
			while(paa_segment.get(j) < test_List.get(0).Attributes.size()/2)
			{
				j++;
				paa_segment.add(paa_segment.get(j-1)*2);
			}			
			Set<Integer>tLabel=new HashSet<Integer>();
			for(int i=0;i< train_List.size();i++){
				tLabel.add(train_List.get(i).cName);			
			}	
			
				
			for (int s=0;s< paa_segment.size(); s++){
				for(int a=0;a < saxAlpha.length;a++){
					corrected=0;
					for(int i=0;i< test_List.size();i++){			
						int predicted_cLabel = classification_algorithm(train_List, test_List.get(i).Attributes, paa_segment.get(s));			
						if(predicted_cLabel == test_List.get(i).cName) corrected = corrected + 1;
			}
					double temp_dist=(double)(test_List.size() - corrected)/(double)test_List.size();
					long endTime = System.nanoTime();
					long elapsedTimeInMillis_1 = TimeUnit.MILLISECONDS.convert((endTime - startTime), TimeUnit.NANOSECONDS);
					errRate_List.add(new Result (temp_dist, paa_segment.get(s), saxAlpha[a], elapsedTimeInMillis_1));
					//System.out.println("Corrected Label "+ corrected + "with paa_segment & alpha size"+ paa_segment.get(s) + " : " +saxAlpha[a]);
					//System.out.println("The error rate is "+ (double)(test_List.size() - corrected)/(double)test_List.size());
			//System.out.println("Corrected Label "+ corrected);
			//System.out.println("The error rate is "+ (double)(test_List.size() - corrected)/(double)test_List.size());
				}
			}
			Collections.sort(errRate_List, new DistanceComparator());
			System.out.println("Result List");
			for(int r=0;r< errRate_List.size();r++)
				System.out.println(y+" , "+errRate_List.get(r).distance +", "+ errRate_List.get(r).paa_segment+", "+ errRate_List.get(r).sax_alpha+", "+errRate_List.get(r).tms);

			
			
			
			
			/*for(int i=0;i< test_List.size();i++){			
				int predicted_cLabel = classification_algorithm(train_List, test_List.get(i).Attributes, paa_segment[f]);			
				if(predicted_cLabel == test_List.get(i).cName) corrected = corrected + 1;
			}
			//System.out.println("*****************************************");
			//System.out.println("The data set you tested has "+ tLabel.size() + " classes");		
			//System.out.println("Training set is size of "+ train_List.size()+ " and test set is size of "+ test_List.size());
			//System.out.println("Time series Length is "+ train_List.get(0).Attributes.size());
			//System.out.println("Corrected Label "+ corrected);
			//System.out.println("The error rate is "+ (double)(test_List.size() - corrected)/(double)test_List.size());	
			long endTime = System.nanoTime();
			long elapsedTimeInMillis_1 = TimeUnit.MILLISECONDS.convert((endTime - startTime), TimeUnit.NANOSECONDS);
			System.out.println(y+", "+"Time for calculation: "+ elapsedTimeInMillis_1 + " ms");*/
		}
	}
	}
}
