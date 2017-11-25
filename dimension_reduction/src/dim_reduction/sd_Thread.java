package dim_reduction;

import java.util.ArrayList;
import java.util.List;

import dim_reduction.ucr_TSeries1NNSAXSD_ExectimeThread.sampleSeries;

public class sd_Thread extends ucr_TSeries1NNSAXSD_ExectimeThread implements Runnable {

	
	private List<sampleSeries>train_List;
	private List<Double>test_List;	
	private String threadname;
	private int paa_segment;
	private Thread t;
	private volatile double[]sdDist;
	public sd_Thread(String name)
	{
		threadname=name;
	}
	public sd_Thread(List<sampleSeries>train_List, List<Double>test_List, int paa_segment,String tname)
	{
		threadname=tname;
		this.paa_segment=paa_segment;
		this.train_List=train_List;
		this.test_List=test_List;		
	}
	@Override
	public void run() {	
		sdDist=new double[train_List.size()];
		for(int i=0;i< train_List.size();i++){
			double []tempArray=new double[train_List.get(i).Attributes.size()];
			double []tempArray1=new double[test_List.size()];
			 for(int j=0;j< train_List.get(i).Attributes.size();j++)
			 {
				 tempArray[j]=train_List.get(i).Attributes.get(j);
				 tempArray1[j]=test_List.get(j);
			 }
			//train_List.get(i).Attributes.toArray(tempArray);
			//test_List.toArray(tempArray1);
			sdDist[i]=sd_dist(tempArray, tempArray1, paa_segment, test_List.size());
		}
	}
	
		public void start ()

		{
			if(t==null)
			{
				t=new Thread(this, threadname);
				t.start();
			}
		}
		public double[] getsdList()
		{
			return sdDist;
		}
}
