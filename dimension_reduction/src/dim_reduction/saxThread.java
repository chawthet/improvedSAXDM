package dim_reduction;

import java.util.ArrayList;
import java.util.List;

import dim_reduction.ucr_TSeries1NNSAXSD_ExectimeThread.sampleSeries;

public class saxThread extends ucr_TSeries1NNSAXSD_ExectimeThread implements Runnable {

	
	private String threadname;
	private int paa_segment;
	private int saxAlpha;
	private List<sampleSeries>train_List;
	private List<Double>test_List;
	private volatile double[] saxDist;
	private Thread t;
	public saxThread(String name)
	{
		threadname=name;
	}
	public saxThread(List<sampleSeries>train_List, List<Double>test_List, int paa_segment, int saxAlpha,String tname)
	{
		threadname=tname;
		this.paa_segment=paa_segment;
		this.saxAlpha=saxAlpha;
		this.train_List=train_List;
		this.test_List=test_List;
	}
	@Override
	public void run() {		
		saxDist=new double[train_List.size()];
		for(int i=0;i< train_List.size();i++){
			double []tempArray=new double[train_List.get(i).Attributes.size()];
			double []tempArray1=new double[test_List.size()];
			 for(int j=0;j< train_List.get(i).Attributes.size();j++)
			 {
				 tempArray[j]=train_List.get(i).Attributes.get(j);
				 tempArray1[j]=test_List.get(j);
			 }
			saxDist[i]=sax_dist(tempArray, tempArray1, paa_segment, saxAlpha, test_List.size());		
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
		public double[] getsaxList()
		{
			return saxDist;
			//System.out.println("Size:"+ saxDistList.size());
		}
}
