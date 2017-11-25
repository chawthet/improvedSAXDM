package SAX_SD;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

import SAX_SD.ucr_TSeries1NNSAXSD_Test.DistanceComparator;
import SAX_SD.ucr_TSeries1NNSAXSD_Test.Result;
import net.seninp.jmotif.distance.EuclideanDistance;
import net.seninp.jmotif.sax.SAXException;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.jmotif.sax.alphabet.Alphabet;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;

public class ucr_TSeries1NNSAXTD_pretrain {

	public static List<sampleSeries> dataLoad(String filename) {
		Path file = Paths.get(filename);
		// java 8: Stream class
		Stream<String> lines;
		int cname = 0;
		List<sampleSeries> sSeriesList = new ArrayList<sampleSeries>();

		try {
			lines = Files.lines(file, StandardCharsets.UTF_8);
			for (String line : (Iterable<String>) lines::iterator) {
				StringTokenizer stk = new StringTokenizer(line, ",");
				cname = Integer.parseInt(stk.nextToken());
				ArrayList<Double> sList = new ArrayList<Double>();
				while (stk.hasMoreTokens()) {
					sList.add(Double.parseDouble(stk.nextToken()));
				}
				sSeriesList.add(new sampleSeries(cname, sList));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sSeriesList;
	}

	public static double[][] delta_Distance(double[] ts, int paaSize) {
		// fix the length
		int len = ts.length;
		double[][] delta_tsdist = new double[paaSize][2];
		if (len < paaSize) {
			try {
				throw new SAXException(
						"PAA size can't be greater than timeseries size.");
			} catch (SAXException e) {
				e.printStackTrace();
			}
		}
		// check for the trivial case
		if (len == paaSize) {
			// return Arrays.copyOf(ts, ts.length);
			delta_tsdist[paaSize][0] = ts[0];
			delta_tsdist[paaSize][1] = ts[ts.length - 1];
			return delta_tsdist;
		} else {
			double tdstart_dist; // trend distance for start for each segment
			double tdend_dist; // trend distance for end for each segment
			double pointsPerSegment = (double) len / (double) paaSize;
			double[] breaks = new double[paaSize + 1];
			for (int i = 0; i < paaSize + 1; i++) {
				breaks[i] = i * pointsPerSegment;
			}
			for (int i = 0; i < paaSize; i++) {
				double segStart = breaks[i];
				double segEnd = breaks[i + 1];

				double fractionStart = Math.ceil(segStart) - segStart;
				double fractionEnd = segEnd - Math.floor(segEnd);

				int fullStart = Double.valueOf(Math.floor(segStart)).intValue();
				int fullEnd = Double.valueOf(Math.ceil(segEnd)).intValue();

				double[] segment = Arrays.copyOfRange(ts, fullStart, fullEnd);

				if (fractionStart > 0) {
					segment[segment.length - 1] = segment[segment.length - 1]
							* fractionEnd;
				}

				double elementsSum = 0.0;
				for (double e : segment) {
					elementsSum = elementsSum + e;
				}
				tdstart_dist = segment[0] - (elementsSum / pointsPerSegment);
				tdend_dist = segment[segment.length - 1]
						- (elementsSum / pointsPerSegment);

				delta_tsdist[i][0] = tdstart_dist;
				delta_tsdist[i][1] = tdend_dist;
			}
		}
		return delta_tsdist;
	}

	/*public static int classification_algorithm(List<sampleSeries> train_List,List<Double> test_List, int paa_segment, int saxAlpha) {
		//List<Result1> innerList = new ArrayList<Result1>();
		TSProcessor tsp = new TSProcessor();
		SAXProcessor saxp = new SAXProcessor();

		Alphabet normalA = new NormalAlphabet();
		double bestsofar = Double.POSITIVE_INFINITY;
		int test_cLabel = -99999;
		// Transform train_List to SAX List
		for (int i = 0; i < train_List.size(); i++) {
			Double[] tempArray = new Double[train_List.get(i).Attributes.size()];
			Double[] tempArray1 = new Double[test_List.size()];
			train_List.get(i).Attributes.toArray(tempArray);
			test_List.toArray(tempArray1);
			char[] tSAX_List;
			char[] qSAX_List;
			double[][] tdelta_Distance;
			double[][] qdelta_Distance;

			try {
				// SAX Transform
				tSAX_List = saxp.ts2string(ArrayUtils.toPrimitive(tempArray),paa_segment, normalA.getCuts(saxAlpha), 0.0001);
				qSAX_List = saxp.ts2string(ArrayUtils.toPrimitive(tempArray1),paa_segment, normalA.getCuts(saxAlpha), 0.0001);

				// Delta Distance (Normalized)
				tdelta_Distance = delta_Distance(tsp.znorm(ArrayUtils.toPrimitive(tempArray), 0.0001),paa_segment);
				qdelta_Distance = delta_Distance(tsp.znorm(ArrayUtils.toPrimitive(tempArray1), 0.0001),paa_segment);

				double td_dist = 0;
				for (int d = 0; d < tdelta_Distance.length; d++) {
					double delta_Start = ((qdelta_Distance[d][0] - tdelta_Distance[d][0]) * (qdelta_Distance[d][0] - tdelta_Distance[d][0]));
					double delta_End = ((qdelta_Distance[d][1] - tdelta_Distance[d][1]) * (qdelta_Distance[d][1] - tdelta_Distance[d][1]));
					td_dist += ((((double) paa_segment / (double) test_List.size())) * Math.pow(Math.sqrt((delta_Start + delta_End)), 2.0));
				}
				double saxDist = saxp.saxMinDist_update(qSAX_List, tSAX_List,normalA.getDistanceMatrix(saxAlpha), test_List.size(),paa_segment);
				double saxTD_Dist = (Math.sqrt((double) test_List.size()/ (double) paa_segment))* Math.sqrt(td_dist + saxDist);

				if (saxTD_Dist < bestsofar) {
					test_cLabel = train_List.get(i).cName;
					bestsofar = saxTD_Dist;
				}
			} catch (SAXException e) {
				e.printStackTrace();
			}
		}
		return test_cLabel;
	}*/

	// simple class to model instances (class + features)
	static class sampleSeries {
		List<Double> Attributes = new ArrayList<Double>();
		int cName;
		public sampleSeries(int cName, ArrayList<Double> Attribute) {
			this.cName = cName;
			this.Attributes = Attribute;
		}
	}

	// simple class to model results (distance + class)
	static class Result1 {
		double distance;
		int cName;

		public Result1(double distance, int cName) {
			this.cName = cName;
			this.distance = distance;
		}
	}

	// simple class to model results (distance + class)
	static class Result {
		double distance;
		int paa_segment;
		int sax_alpha;
		long tms;

		public Result(double distance, int paa_segment, int sax_alpha, long tms) {
			this.distance = distance;
			this.paa_segment = paa_segment;
			this.sax_alpha = sax_alpha;
			this.tms = tms;
		}
	}

	// simple comparator class used to compare results via distances
	static class DistanceComparator1 implements Comparator<Result1> {
		@Override
		public int compare(Result1 a, Result1 b) {
			return a.distance < b.distance ? -1 : a.distance == b.distance ? 0 : 1;
		}
	}

	// simple comparator class used to compare results via distances
	static class DistanceComparator implements Comparator<Result> {
		@Override
		public int compare(Result a, Result b) {
			return a.distance < b.distance ? -1 : a.distance == b.distance ? 0 : 1;
		}
	}

	public static void main(String[] args) {
		String train_filename = "D:\\D1\\UCR_TS_Archive_2015\\22_Datasets_SAX\\wafer\\wafer_TRAIN";
		String test_filename = "D:\\D1\\UCR_TS_Archive_2015\\22_Datasets_SAX\\wafer\\wafer_TEST";
		//int corrected = 0;
		int paa_segment = 64;
		int saxAlpha = 10;
		TSProcessor tsp = new TSProcessor();
		SAXProcessor saxp = new SAXProcessor();
		Alphabet normalA = new NormalAlphabet();
		long totaltime=0;
		char[] tSAX_List;
		char[] qSAX_List = null;
		double[][] tdelta_Distance;
		double[][] qdelta_Distance = null;
		
		ArrayList<char[]>pre_List=new ArrayList<char[]>();
		ArrayList<double[][]>pre_List1=new ArrayList<double[][]>();
		List<sampleSeries> train_List = dataLoad(train_filename);
		for(int t=0;t< 25;t++){
			int corrected=0;
		
		for(int i=0;i< train_List.size();i++)
		{
			Double[] tempArray = new Double[train_List.get(i).Attributes.size()];
			train_List.get(i).Attributes.toArray(tempArray);
			try {
				tSAX_List = saxp.ts2string(ArrayUtils.toPrimitive(tempArray),paa_segment, normalA.getCuts(saxAlpha), 0.00001);
				tdelta_Distance = delta_Distance(tsp.znorm(ArrayUtils.toPrimitive(tempArray), 0.00001),paa_segment);
				pre_List.add(tSAX_List);
				pre_List1.add(tdelta_Distance);
			} catch (SAXException e) {			
				e.printStackTrace();
			}			
		}	

		long startTime = System.currentTimeMillis();		
			corrected = 0;
			List<sampleSeries> test_List = dataLoad(test_filename);
				for (int i = 0; i < test_List.size(); i++) {
					double bestsofar=Double.POSITIVE_INFINITY;
					int test_cLabel = -99999;
					Double[] tempArray1 = new Double[test_List.get(i).Attributes.size()];
					test_List.get(i).Attributes.toArray(tempArray1);
					try {
						qSAX_List = saxp.ts2string(ArrayUtils.toPrimitive(tempArray1),paa_segment, normalA.getCuts(saxAlpha), 0.00001);
						qdelta_Distance = delta_Distance(tsp.znorm(ArrayUtils.toPrimitive(tempArray1), 0.00001),paa_segment);
					} catch (SAXException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
					for(int j=0;j< train_List.size();j++){						
						try {							
							double td_dist = 0;
							for (int d = 0; d < qdelta_Distance.length; d++) {
								double delta_Start = ((qdelta_Distance[d][0] - pre_List1.get(j)[d][0]) * (qdelta_Distance[d][0] - pre_List1.get(j)[d][0]));
								double delta_End = ((qdelta_Distance[d][1] - pre_List1.get(j)[d][1]) * (qdelta_Distance[d][1] - pre_List1.get(j)[d][1]));
								td_dist += ((((double) paa_segment / (double) test_List.size())) * Math.pow(Math.sqrt((delta_Start + delta_End)), 2.0));
							}
							double saxDist = saxp.saxMinDist_update(qSAX_List, pre_List.get(j),normalA.getDistanceMatrix(saxAlpha), test_List.size(),paa_segment);
							double saxTD_Dist = (Math.sqrt((double) test_List.size()/ (double) paa_segment))* Math.sqrt(td_dist + saxDist);

							if (saxTD_Dist < bestsofar) {
								test_cLabel = train_List.get(j).cName;
								bestsofar = saxTD_Dist;
							}
						} catch (SAXException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}	
					}										
					if (test_cLabel == test_List.get(i).cName)
							corrected = corrected + 1;
					}
					double temp_dist = (double) (test_List.size() - corrected)/ (double) test_List.size();
					long endTime = System.currentTimeMillis();
					long elapsedTimeInMillis_1 = endTime - startTime;
					totaltime+=elapsedTimeInMillis_1;
					System.out.println("ErrorRate: " + temp_dist + "\nCorrected Label "+ corrected + "\nwith paa_segment & alpha size"+ paa_segment + " : " + saxAlpha);
					}
					System.out.println("\nExecutionTime:"	+ (totaltime/25.0)/(1000.0));
				}
}