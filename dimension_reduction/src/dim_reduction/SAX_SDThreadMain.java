package dim_reduction;

import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

import dim_reduction.ucr_TSeries1NNSAXSD_ExectimeThread.sampleSeries;

public class SAX_SDThreadMain {
	
	public static void main(String[] args) {
		ResourceBundle.clearCache();
		ucr_TSeries1NNSAXSD_ExectimeThread exthread=new ucr_TSeries1NNSAXSD_ExectimeThread();
		String train_filename="D:\\D1\\UCR_TS_Archive_2015\\22_Datasets_SAX\\ECG200\\ECG200_TRAIN";
		String test_filename="D:\\D1\\UCR_TS_Archive_2015\\22_Datasets_SAX\\ECG200\\ECG200_TEST";
		int corrected=0;
		int paa_segment=2;
		int saxAlpha=10;
		long totaltime=0;
		double temp_dist=0;
		//double best_so_far=Double.POSITIVE_INFINITY;
		//int test_cLabel = -99999;		
		for (int z=0;z<25; z++){
			corrected=0;
			long startTime=System.currentTimeMillis();
			List<sampleSeries>train_List=exthread.dataLoad(train_filename);
			List<sampleSeries>test_List=exthread.dataLoad(test_filename);
					
				
				for(int i=0;i< test_List.size();i++){
				double best_so_far=Double.POSITIVE_INFINITY;
				int test_cLabel = -99999;
				sd_Thread sdthread=new sd_Thread(train_List, test_List.get(i).Attributes, paa_segment, "sdThread");
				saxThread saxthread=new saxThread(train_List, test_List.get(i).Attributes, paa_segment, saxAlpha, "saxThread");
			
				 Thread t1=new Thread(sdthread);
				 Thread t2=new Thread(saxthread);
				 t1.start();
				 t2.start();
				 try {				 					 
					t1.join();
					t2.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}		
				 double totalDist=0;
				 double[]sdDist=sdthread.getsdList();
				 double[]saxDist=saxthread.getsaxList();
				 for(int j=0;j<sdDist.length;j++){
					 totalDist=Math.sqrt(((double)test_List.size()/(double)paa_segment))*Math.sqrt(saxDist[j]+sdDist[j]);
				 if(totalDist < best_so_far){
					 test_cLabel=train_List.get(j).cName;
					 best_so_far=totalDist;
				 }
				}
				 if(test_cLabel == test_List.get(i).cName) corrected = corrected + 1;
			}
			//corrected=0;
			/*for(int s=0;s< test_List.size();s++){				 			
				if(test_cLabel == test_List.get(s).cName) corrected = corrected + 1;
			}*/
			temp_dist=(double)(test_List.size() - corrected)/(double)test_List.size();
			long endTime = System.currentTimeMillis();
			long elapsedTimeInMillis_1 = endTime - startTime;	
			totaltime+=elapsedTimeInMillis_1;
			ResourceBundle.clearCache();
		}	 
		
		System.out.println("*******************************************");
		System.out.println("Corrected Label "+ corrected +"Error Rate: "+ temp_dist);
		System.out.println("Total Execution time for segment size: "+ paa_segment + " : "+ totaltime/25.0 + "msec");
	}

}
