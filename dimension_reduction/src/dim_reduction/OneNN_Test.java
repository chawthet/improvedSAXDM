package dim_reduction;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Stream;

public class OneNN_Test {

	//for Trained Data
	public static List<sampleSeries>  dataMap(String filename){
		Path file=Paths.get(filename);
		//java 8: Stream class
		Stream<String> lines;
		Float cname=0f;
		List<sampleSeries>sSeriesList=new ArrayList<sampleSeries>();
		
		try {
			lines = Files.lines(file, StandardCharsets.UTF_8);
			for(String line: (Iterable<String>) lines::iterator){
				StringTokenizer stk=new StringTokenizer(line, " ");
				cname=Float.parseFloat(stk.nextToken());
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
	//for Test Data
	public static List<ArrayList<Double>>dataLoad(String filename){
		Path file=Paths.get(filename);
		//java 8: Stream class
		Stream<String> lines;
		Float cname=0f;
		List<ArrayList<Double>>qSeriesList=new ArrayList<ArrayList<Double>>();
		
		try {
			lines = Files.lines(file, StandardCharsets.UTF_8);
			for(String line: (Iterable<String>) lines::iterator){
				StringTokenizer stk=new StringTokenizer(line, " ");
				cname=Float.parseFloat(stk.nextToken());					
				ArrayList<Double>qList=new ArrayList<Double>();
				while(stk.hasMoreTokens()){					
					qList.add(Double.parseDouble(stk.nextToken()));
				}	
				qSeriesList.add(qList);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return qSeriesList;
	}
	
	public static void main(String[] args) {
		int k=1;		//# of neighbors
		//String dfilename="D:\\eclipse_luna_workspace\\dimension_reduction\\resources\\dataset\\CBF\\CBF_TRAIN";
		//String qfilename="D:\\eclipse_luna_workspace\\dimension_reduction\\resources\\dataset\\CBF\\CBF_TEST";
		
		String dfilename="/home/chawtzan/eclipse_luna_ws/dimension_reduction/resources/dataset/CBF/CBF_TRAIN";
		String qfilename="/home/chawtzan/eclipse_luna_ws/dimension_reduction/resources/dataset/CBF/CBF_TEST";
		//list for trained data series
		List<sampleSeries>sSeriesList=dataMap(dfilename);
		//List for query data series
		List<ArrayList<Double>>qSeriesList=dataLoad(qfilename);
		//list to save distance result
		List<List<Result>> resultList = new ArrayList<List<Result>>();
		/*double[]query={-1.5172029e+000, -3.3271582e-001, -1.2521944e-001,
				-3.1039752e-001, -1.3724254e-001,  2.8504921e-001, -1.1167064e-001,
				-4.6538397e-001, -2.7380431e-001,  1.3651345e-001,  2.5486315e-001,
				-8.6224219e-001, -8.3357425e-002, -4.6313565e-001, -2.2711098e-001, 
				-1.3033858e+000, -8.5209281e-001,  5.4910805e-002,  3.0538331e-001, 
				-6.0270031e-001, -5.2178262e-001, -7.8222255e-001, -2.2229987e-001,  
				4.6833929e-002, -3.2184387e-001, -6.2854879e-001, -1.0538965e+000,
				-7.3234732e-001,  6.7703454e-001, -3.2840063e-001,  7.1780709e-002,
				-6.8855546e-001, -7.5447809e-002,  6.1031846e-001, -1.3595744e-001,
				-6.4325515e-001,  2.5181063e-001,  8.2140078e-001,  2.5438079e-001,
				5.3025888e-001,  1.3176644e+000,  5.7967163e-001, -1.6434296e-001,
				9.8127925e-001,  1.4554383e+000,  1.4295710e+000,  1.1765190e+000,
				7.2959592e-001,  1.4243019e+000,  1.7795581e+000,  1.2024350e+000,
				1.9604186e+000,  1.6170078e+000,  1.2329466e+000,  1.3621172e+000,
				2.6735676e+000,  1.8309385e+000,  1.0941541e+000,  2.2501651e+000,
				2.2233088e+000,  2.6356121e+000,  2.1655242e+000,  2.4820461e+000,
				2.8700245e+000,  3.0771456e+000,  1.0700153e-002, -7.8338195e-001,
				1.4067663e-001, 1.5358670e-001, -9.8875527e-001, -1.0022488e+000, 
				-6.7754278e-001, -1.7874740e+000, -1.0949665e+000, -7.1647677e-001,
				7.3080152e-002, -9.3185591e-001, -1.0799353e+000, -9.9759230e-002,
				-7.7860715e-001, -2.7506308e-001,  1.2639293e-001, -1.2419729e+000,
				1.0890070e-001,  1.3347904e-001, -6.0697642e-001, -4.3463807e-001,
				-5.9908448e-001, -4.1299825e-001, -1.1651371e+000, -1.0596061e+000,
				1.0793437e-001, -8.8706554e-002, -9.4974426e-003, -1.6824750e+000,
				-9.1131115e-001, -3.0536050e-001, -5.3197119e-001, -2.6168157e-001,
				-1.6454567e-001, -7.7671767e-001,  1.0404477e-001,  2.5661950e-001,
				-1.7580822e-001, -6.7316357e-001, -9.1099302e-001, -5.9982294e-001,
				8.4301754e-002, -1.0656627e-001, -5.7008832e-001, -1.1042220e-001,
				-6.8912076e-001, -1.2997821e+000, -9.1324287e-001, -1.1426435e+000,
				-4.4291917e-001, -6.2034121e-001, -1.2458491e-001,  6.0588933e-002,
				-6.5981889e-001, -8.4775194e-001, -1.1150918e+000, -7.8318288e-001,
				-3.0291353e-001, -1.6948994e-001,  2.3132049e-001, -1.3145390e-001,
				-6.1866420e-001};*/
		//find distance
		for(int i=0;i< qSeriesList.size();i++){
			List<Result>innerList=new ArrayList<Result>();
			for(sampleSeries ss: sSeriesList){				
				double dist=0.0;
				for(int j=0; j< ss.Attributes.size(); j++){					
					dist+=Math.pow(ss.Attributes.get(j)-qSeriesList.get(i).get(j), 2);				
				}
				double edDist=Math.sqrt(dist);
				innerList.add(new Result(edDist, ss.cName));		
			}
			resultList.add(innerList);
		}
		//Display the resultList
		for(int i=0;i< resultList.size();i++){
			Collections.sort(resultList.get(i), new DistanceComparator());
			//System.out.println("Result List Size: "+ resultList.size());
			float[]class_Label=new float[k];
			for(int j=0;j< k;j++){
				//System.out.println(resultList.get(i).get(j).cName + " :  "+ resultList.get(i).get(j).distance);
				//class_Label[i]=resultList.get(i).cName;
				System.out.println(resultList.get(i).get(j).cName);
			}
		}
		
		//Display the dataSeries
		//for(int i=0;i<sSeriesList.size();i++)
		//System.out.println(sSeriesList.get(i).cName +"," + sSeriesList.get(i).Attributes);
	}

	//simple class to model instances (features + class)
		static class sampleSeries {	
			List<Double> Attributes=new ArrayList<Double>();
			float cName;
			public sampleSeries(float cName, ArrayList<Double>Attribute ){
				this.cName = cName;
				this.Attributes = Attribute;				
			}
		}
		//simple class to model results (distance + class)
		static class Result {	
			double distance;
			float cName;
			public Result(double distance, float cName){
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

}
