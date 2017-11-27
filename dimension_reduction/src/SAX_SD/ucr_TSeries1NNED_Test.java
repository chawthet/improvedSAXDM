package SAX_SD;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Stream;

/**
 * Check classification accuracy for UCR time series data sets.
 * Distance measures: 1NNED (1Nearest Neighbor Euclidean Distance)  
 * @author chawt
 *
 */
public class ucr_TSeries1NNED_Test {

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
	
	public static int classification_algorithm(List<sampleSeries>train_List, List<Double> test_List)
	{
		int test_cLabel=-9999999;
		double best_so_far=Double.POSITIVE_INFINITY;
		for(int i=0;i< train_List.size();i++){
			double distance=0.0;
			for(int j=0;j< train_List.get(i).Attributes.size();j++){
				distance+=Math.pow(test_List.get(j)-train_List.get(i).Attributes.get(j),2.0);
			}
			double edDist=Math.sqrt(distance);
			if(edDist < best_so_far){
				test_cLabel=train_List.get(i).cName;
			best_so_far=edDist;
			}
			//innerList.add(new Result(edDist, train_List.get(i).cName));
		}
		//Collections.sort(innerList, new DistanceComparator());
		//int test_cLabel=innerList.get(0).cName;
		return test_cLabel;		
	}
	
	//simple class to model instances (features + class)
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
		if(args.length ==0){
			System.exit(-1);
		}
		String train_filename= args[0];
		String test_filename=args[1];
		long startTime=System.currentTimeMillis();
		List<sampleSeries>train_List=dataLoad(train_filename);
		List<sampleSeries>test_List=dataLoad(test_filename);		
		int corrected=0;
		Set<Integer>tLabel=new HashSet<Integer>();
		for(int i=0;i< train_List.size();i++){
			tLabel.add(train_List.get(i).cName);			
		}
		
		for(int i=0;i< test_List.size();i++){			
			int predicted_cLabel = classification_algorithm(train_List, test_List.get(i).Attributes);			
			if(predicted_cLabel == test_List.get(i).cName) corrected = corrected + 1;
		}
		System.out.println("The data set you tested has "+ tLabel.size() + " classes");		
		System.out.println("Training set is size of "+ train_List.size()+ " and test set is size of "+ test_List.size());
		System.out.println("Time series Length is "+ train_List.get(0).Attributes.size());
		System.out.println("Corrected Label "+ corrected);
		System.out.println("The error rate is "+ (double)(test_List.size() - corrected)/(double)test_List.size());
		System.out.println("Total time taken: "+ (System.currentTimeMillis() - startTime)+"ms");
	}
}
