package pff;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class PffVsWs 
{
	//PFF Algorithm Comment
		/*If PFFrequency is 1/F. Time interval between current and last page fault time is compared with F whenever page fault occurs.  
		 * As we increase the value of F, the number of page faults decrease and reach a constant value. As F increases, the resident
		 * set size increases slowly and over time it reaches process size.
		 * For F=2, page faults=14
		 * For F=4, page faults=11
		 * For F=6, page faults=10
		 * For F=8, page faults=10
		 * For F=10, page faults=10
		 * 
		 * So we can see that after F=6, page faults reach their max value for a large sized input.
		 */
		
		//VSWS Algorithm Comment
		/*
		 * If fault count reaches Q, in the sampling interval (M<=refcount<=L), scan again 
		 * and sampling should be done in this interval.
		 * If L time passes since last sampling, scan again.
		 * With an increase in Q, Number of PFs decreases for the interval M->L.
		 * If Q closes near to L, PFs saturates to a constant value
		 * Fix L = 6, M = 2
		 * Q = 3, faults = 4116
		 * Q = 5, faults = 3739
		 * Q = 7,10 faults = 3721
		 *
		 *The number of page faults become constant after a particular value of Q keeping L and M constant.
		 * 
		 * Fix M = 2, Q = 5
		 * L = 5 faults = 4323
		 * L= 10, faults = 2036
		 * L = 25, faults = 231	
		 * L = 40, faults = 49
		 * L = 65, faults= 11
		 * L = 72,80,90,100  faults=10
		 * The number of page faults becomes constant after a particular value of L keeping M and Q constant.
		 * 
		 * ----------Performance Comparison---------------------
		 * 
		  VSWS manages memory more efficiently because usually locality shifts happen in the process. At that
		  time VSWS changes its sampling rate and removes the frames which are not being used much.
		  VSWS also keeps less frames in memory than the PFF as seen from the output.
		  PFF is better than VSWS when there are not many locality shifts in the process.
		 */

	//hpff is the hashmap for pff algorithm
	//hvsws is the hashmap for vsws algorithm
	static ConcurrentHashMap<Integer, Integer> hpff=new ConcurrentHashMap<Integer, Integer>();
	static ConcurrentHashMap<Integer, Integer> hvsws=new ConcurrentHashMap<Integer, Integer>();
	static int page_fault;          //The total number of faults for pff algorithm
	static int faults;              //The total number of faults for vsws algorithm
	
	public void pff(String file) throws IOException
	{
		//declarations
		int page, numFrames=0;
		int lastPageFault=0;
	    int count=0;
	    int thresh=10; 
	    
	    System.out.println("--------------Running PFF algorithm using F="+thresh+" ---------------------");
	    FileReader fr=new FileReader(file);
	    BufferedReader br=new BufferedReader(fr);
	    
	    String a=br.readLine();          // reads the content to the array
	    while((a=br.readLine())!=null)
	    {
	    	page=Integer.parseInt(a.trim()+"");
	        
	        //checks if hash map already contains the page number
	        if(hpff.containsKey(page))
	        {
	        	hpff.put(page, 1);
	        	count++;
	        }
	        // if hash map doesn't contain the page number
	        else
	        {
	        	page_fault++;    ///increment the number of page faults
	        	if(hpff.size()<10)
	        		numFrames++;
	        	if(count - lastPageFault >= thresh)
                { 
	        		Iterator<Entry<Integer, Integer>> iter = hpff.entrySet().iterator();
	        		while (iter.hasNext()) 
	        		{
	        			Entry item = iter.next();
	        		    if(item.getValue().equals(0))
	        		    {
	        		        iter.remove();          //remove the keys having value 0
	        		    }
	        		    else
	                    {
	                        hpff.put((Integer) item.getKey(), 0);    //replace all the keys having value 1 to 0
	                    }
	        		}
	        		hpff.put(page, 1);
	        		lastPageFault=count;
	        		++count;
                }
                else
                {  
                	hpff.put(page, 1);
                	lastPageFault=count;
                	++count;
                }
           }
	    }
	    br.close();
	    fr.close();      //closing the file
	    System.out.println("There are less than 10 frames in memory "+numFrames+" times during PFF execution");
	}
	public void vsws(String file) throws IOException
	{
		//declarations
		int Q=4,M=2, L=30, numFrames=0;
		int refcount=0, fault_count=0;     //refcount is the time  and  fault_count is the number of faults
		
		System.out.println("----------------------Running vsws algorithm using Q="+Q+" M="+M+" L="+L+" ---------------------------------");
		FileReader fr=new FileReader(file);
		BufferedReader br=new BufferedReader(fr);		
		String str=br.readLine();    //initialize the number of pages		
		while((str=br.readLine())!=null)   // reading the lines one by one
		{
			refcount++;
			int page=Integer.parseInt(str);
			//checks if refcount or time is less than M
			if(refcount<M)
			{
				if(hvsws.containsKey(page)==false)
				{
					fault_count++;
					faults++;
					hvsws.put(page, 1);
				}
				else if(hvsws.get(page).equals(0))
				{
					hvsws.put(page, 1);
				}
			}
			//checks if refcount or time is between M and L
			else if(refcount>=M && refcount<L)
			{
				if(hvsws.containsKey(page)==false)
				{
					fault_count++;
					faults++;
				}
				if(fault_count>=Q)
				{
					Iterator<Entry<Integer, Integer>> iter = hvsws.entrySet().iterator();
	        		while (iter.hasNext()) 
	        		{
	        			Entry item = iter.next();
	        		    if(item.getValue().equals(0))
	        		    {
	        		        iter.remove();
	        		    }
	        		    else
	                    {
	                        hvsws.put((Integer) item.getKey(), 0);
	                    }
	        		}
	        		if(hvsws.containsKey(page)==false||hvsws.get(page).equals(0))
	        			hvsws.put(page, 1);
	        		fault_count=0;
					refcount=0;
				}
				else
				{
					hvsws.put(page, 1);
				}
			}
			//checks if refcount or time is greater than L
			else if(refcount==L)
			{
				if(hvsws.containsKey(page)==false)
				{
					fault_count++;
					faults++;
				}
				fault_count=0;
				refcount=0;
				Iterator<Entry<Integer, Integer>> iter = hvsws.entrySet().iterator();
        		while (iter.hasNext()) 
        		{
        			Entry item = iter.next();
        		    if(item.getValue().equals(0))
        		    {
        		        iter.remove();
        		    }
        		    else
                    {
                        hvsws.put((Integer) item.getKey(), 0);
                    }
        		}
        		if(hvsws.containsKey(page)==false||(hvsws.get(page).equals(0)))
				{
					hvsws.put(page, 1);
				}
			}
			if(hvsws.size()<10)
				numFrames++;
		}
		br.close();
		fr.close();
		System.out.println("There are less than 10 frames in memory "+numFrames+" times during vsws execution");
	}
	public static void main(String[] args) throws IOException 
	{
		String str=args[0];
		//String str="testExample.txt";
		PffVsWs obj=new PffVsWs();    //create object of class
		obj.pff(str);
		System.out.println("Page fault count for pff algorithm: "+page_fault);
		System.out.println("Size of the largest amount of memory used by PFF algorithm is "+hpff.size());
		System.out.println("Hash map for pff algorithm: "+hpff);
		
		obj.vsws(str);
		System.out.println("Page fault count for vsws algorithm: "+faults);
		System.out.println("Size of the largest amount of memory used by vsws algorithm is "+hvsws.size());
		System.out.println("Hash map for vsws algorithm: "+hvsws);
	}
}