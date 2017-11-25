package dim_reduction;

import java.util.Arrays;

public class cumulativesum_tPoint {
	
	/**
	   * Finds the mean value in timeseries.
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
	   * Finds the maximal value in timeseries.
	   * 
	   * @param series The timeseries.
	   * @return The index of max value.
	   */
	  public static int max(double[] series) {
	    double max = Double.MIN_VALUE;
	    int maxi=-1;
	    for (int i = 0; i < series.length; i++) {
	      if (max < series[i]) {
	        max = series[i];
	        //System.out.println("Maximum: "+max);
	        maxi = i;
	      }
	    }
	    //return max;
	    return maxi;
	 }	  
	  
	  /**
	   * Finds the minimal value in timeseries.
	   * 
	   * @param series The timeseries.
	   * @return The min value.
	   */
	  public static int min(double[] series) {
	    double min = Double.MAX_VALUE;
	    int mini=-1;
	    for (int i = 0; i < series.length; i++) {
	      if (min > series[i]) {
	        min = series[i];
	        //System.out.println("Minimum: "+min);
	        mini=i;
	      }
	    }
	    //return min;
	    return mini;
	  }
	  
	  /**
	   * 
	   * @param num given time series
	   * @param k- #of segments
	   * @return array of key_points
	   */
	public static double[] cumulative_sum(double[]num)
	{		
		double mu=mean(num);
		double []csum=new double[num.length+1];		
			csum[0]=0;
			for(int i=1;i<= num.length;i++){
				csum[i]=csum[i-1]+(num[i-1]-mu);
				//System.out.println(csum[i]);
			}
			//int key=min(csum);			
		return csum;
	}
	public static int[] segment_A(double[]num, int k){
		int leastSeg=5;
		int []keyPoint=new int[k];
		double[]tmp=num;
		int stindex=0;		
		keyPoint[0]=max(cumulative_sum(tmp));	//return index of max point
		double[]tmp1=Arrays.copyOfRange(tmp, 0, keyPoint[0]);
		double[]tmp2=Arrays.copyOfRange(tmp, keyPoint[0]+1, tmp.length);
		//double[]tmp1;
		//double[]tmp2;
		
		for(int p=1;p<keyPoint.length;p++)
		{						
			if(keyPoint[p-1] > leastSeg) {
				keyPoint[p]=max(cumulative_sum(tmp1));				
				//int sindex=keyPoint[p];
				if (keyPoint[p] != 0 && keyPoint[p]!= tmp1.length-1 && ((tmp1.length - keyPoint[p])>leastSeg )){
					double[]temp_buf=tmp1;
					tmp1=Arrays.copyOfRange(temp_buf, 0, keyPoint[p]);
					tmp2=Arrays.copyOfRange(temp_buf, keyPoint[p]+1, temp_buf.length);
				}				
				//stindex=keyPoint[p];
			}
			else if(keyPoint[p-1] > leastSeg)
			{				
				int tmpindex=max(cumulative_sum(tmp2));
				keyPoint[p]=tmpindex + keyPoint[p-1];		
				if (tmpindex != 0 && tmpindex != tmp2.length-1 && ((tmp2.length -tmpindex)>leastSeg)){
				tmp1=Arrays.copyOfRange(tmp2,0, tmpindex);
				tmp2=Arrays.copyOfRange(tmp2, tmpindex+1, tmp2.length);
				}				
				//stindex=sindex;
			}					
		}
		return keyPoint;
	}
	
	public static void main(String[] args) {		
		double []num={-0.46428,-0.55505,-0.84284,-0.8659,-0.9364,-0.81727,-0.26361,-1.258,-1.2504,-0.91831,-0.9221,-0.98449,-1.2881,-1.1435,-1.0489,-0.36539,-0.6914,-0.98055,-0.99134,-0.88709,-1.1099,-0.76872,-1.0103,-0.847,-1.3277,-0.71291,-1.2977,1.3315,0.93574,1.236,1.0561,0.91074,1.3713,0.93706,0.91029,0.93109,1.4938,1.3193,1.2191,0.94836,0.16902,0.9774,1.2301,0.57157,0.75134,0.96185,0.78131,1.1044,1.0003,0.55073,0.26887,1.1255,1.0759,1.0717,1.1545,0.7594,0.15119,1.0203,1.2573,1.0071,1.1624,0.83,1.4851,1.1051,1.5393,1.1156,1.8914,1.3901,1.1567,1.5149,1.6336,0.81026,0.67666,0.70939,1.3614,1.3124,0.59161,0.83193,1.0971,0.96496,0.92592,1.2361,1.1217,0.9347,1.2165,-1.6066,-0.59478,-0.75663,-0.56606,-0.6485,-0.65649,-0.53225,-0.11505,-0.57315,-0.54118,-0.70009,-1.1989,-0.9527,-0.99828,-0.82499,-0.38078,-0.4766,-0.60136,-0.36936,-1.299,-0.91839,-1.2648,-0.99194,-0.80558,-0.92095,-0.36623,-0.34636,-1.0469,-0.91467,-0.91157,-0.98678,-1.219,-0.52962,-1.3311,-1.0837,-1.018,-0.91603,-1.1343,-0.92022,-0.78936,-0.63871,-0.96366,-1.2452};
		//double[]num={-0.46428,-0.55505,-0.84284,-0.8659,-0.9364,-0.81727,-0.26361,-1.258};
		int ktimes=5;
		int keyPoint[]=segment_A(num, ktimes);
		System.out.println("lenght of num:"+ num.length);
		for(int i=0;i< keyPoint.length;i++)
			System.out.println(keyPoint[i]);
		}
}
