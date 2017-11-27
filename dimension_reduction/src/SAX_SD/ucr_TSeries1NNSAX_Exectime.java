package SAX_SD;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;

import net.seninp.jmotif.sax.SAXException;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.alphabet.Alphabet;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;

/**
 * implement SAX representation 
 * check 1NNED classification accuracy for UCR time series data sets
 * parameters for segment size and symbol(alphabet size=10) can be check on paper
 * 
 * @author chawt
 *
 */
public class ucr_TSeries1NNSAX_Exectime {

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
		if(args.length ==0){
			System.exit(-1);
		}
		String train_filename= args[0];
		String test_filename=args[1];
		//fixed parameter for each dataset
		int paa_segment=32;	
		//int saxAlpha=10;
		long totaltime=0;
		int corrected=0;
		double temp_dist=0;
		for (int y=0;y<25;y++){		
			long startTime=System.currentTimeMillis();	
			List<sampleSeries>train_List=dataLoad(train_filename);
			List<sampleSeries>test_List=dataLoad(test_filename);		
			
			/*Set<Integer>tLabel=new HashSet<Integer>();
			for(int i=0;i< train_List.size();i++){
				tLabel.add(train_List.get(i).cName);			
			}	*/				
			corrected=0;
					for(int i=0;i< test_List.size();i++){			
						int predicted_cLabel = classification_algorithm(train_List, test_List.get(i).Attributes, paa_segment);			
						if(predicted_cLabel == test_List.get(i).cName) corrected = corrected + 1;
			}
					temp_dist=(double)(test_List.size() - corrected)/(double)test_List.size();
					long endTime = System.currentTimeMillis();
					long elapsedTimeInMillis_1 = endTime - startTime;
					totaltime+=elapsedTimeInMillis_1;					
				}
			System.out.println("Corrected Label "+ corrected + "with paa_segment & alpha size"+ paa_segment);
			System.out.println("The error rate is "+ temp_dist);
			System.out.println("Total execution time: "+ totaltime/25.0);		
			}	
}
