package net.psiX.test;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import net.psiX.connection.PsiXConnector;

public class SharePublishCloud2 {

	
	
	   public static ArrayList readConfig(String loc, int id){    	
	    	File f = new File(loc);
	        ArrayList serverList = new ArrayList();
	        if(!f.exists()){
	        	System.out.println("Unable to locate the configuration file, program terminated.");
	        	System.exit(0);
	        }
	        try{
	         	  FileInputStream fstream = new FileInputStream(f);
	        	  DataInputStream in = new DataInputStream(fstream);        	  
	        	  BufferedReader br = new BufferedReader(new InputStreamReader(in));
	        	  String strLine;
	        	  while ((strLine = br.readLine()) != null){
	        	      if(strLine.startsWith("peer")){
	        	    	  StringTokenizer st = new StringTokenizer(strLine," ");
	        	    	  while(st.hasMoreTokens()) {
	        	    		  if(id==2)
	        	    			  st.nextToken();
	        	    		  serverList.add(st.nextToken());
	        	    	  } 
	        	      }
	        	  }
	        	  in.close();
	        }
	        catch (Exception e){
	        	  System.err.println("Error: " + e.getMessage());
	        }
	        
	        return serverList;
	  }
	 public static int[] convertIntegers(ArrayList list)
	   {
	       int[] ret = new int[list.size()];
	       for (int i=0; i < ret.length; i++)
	       {
	           ret[i] = (Integer)list.get(i);
	       }
	       return ret;
	   }

	   
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub


		String dataSet = "1340";
		File file = new File("/usr/share/tomcat6/documents/"
				+ dataSet); 
		ArrayList serverList = readConfig("config",1);
		String HOSTLIST[] = (String[])serverList.toArray(new String[serverList.size()]); 

		
		serverList = readConfig("config",2);
		int docs2Publish[] = convertIntegers(serverList);
				
				//1,1,2,6,12,22,37,54,71,84,89,84,71,54,37,22,12,6,2,1};
//				12,2,5,12,25,46,77,113,149,176,186,176,149,113,77,46,25,12,5,2};

		int index_count = 0;
		int currentDocs2Publish = docs2Publish[index_count];
		int currentDocPublished = 0;
		//int waitTime=15000;
		int waitTime=5000;
		
		if (file.isDirectory()) {

			// Get all files in the directory
			File[] listOfFiles = file.listFiles();

			PsiXConnector psiX = new PsiXConnector();

			// Iterate through all files in the directory
			for (int index = 0; index < listOfFiles.length; index++) {

				file = listOfFiles[index];

				if (file.exists()) {
					try {
						// Publish
						

						psiX.publishDoc(
								file.getPath(),
								null,
								"http://" + HOSTLIST[index_count] + ":8080"
										+ "/documents/" + dataSet + "/"
										+ file.getName());

						// Increase current published document number
						currentDocPublished++;
						
						System.out.println("Hostindex: " + (index_count + 1)
								+ " # To publish: " + currentDocs2Publish
								+ " # CurrentPublished: " + currentDocPublished
								+ " # TotalPublished: " + index);

						System.out.println("http://" + HOSTLIST[index_count]
								+ ":8080" + "/documents/" + dataSet + "/"
								+ file.getName());
					
						// Increase index_count and Reset currentDoc published
						// no.
						if (currentDocPublished == currentDocs2Publish) {

							index_count++;
							currentDocs2Publish = docs2Publish[index_count];
							currentDocPublished = 0;

						}
						
						// Wait
						if(index>=500) {
							//waitTime=30000;
							break;
						}
						Thread.sleep(waitTime);

					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
					}

				}

			}

		}
	}

}
