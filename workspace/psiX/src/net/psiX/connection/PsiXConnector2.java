package net.psiX.connection;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.MultipartPostMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;

import net.psiX.http.ClientHttpRequest;
import net.psiX.objects.PsiXObject;
import java.util.*;
public class PsiXConnector2 {

	/**
	 * @author Tivakar
	 *         This method publishDoc is used for publishing the document along
	 *         with document id
	 * @throws FileNotFoundException 
	 */
	
	public String publishDoc(String xmlfile, String xmldtd, String docid, int count) throws FileNotFoundException {

		HttpClient client = new HttpClient();
		client.getParams().setParameter("http.useragent", "Test Client");
		BufferedReader br = null;
		
		//Random generator = new Random();
		//int choice = generator.nextInt(server.length);

		String instance="";
		String[] server ={			
				"ec2-23-20-45-36.compute-1.amazonaws.com",
				"ec2-54-224-69-36.compute-1.amazonaws.com",
				"ec2-54-224-80-216.compute-1.amazonaws.com",
				"ec2-184-72-145-136.compute-1.amazonaws.com"
	};
		int choice=count%(server.length);		
		instance=server[choice];
		
		PostMethod method = new PostMethod("http://"+instance+"/cgi-bin/publishcluster.cgi");
		Part[] parts = {
			      new FilePart("xmlfile", new File(xmlfile)),
			      new StringPart("docid", docid)
			  };
		method.setRequestEntity(
			      new MultipartRequestEntity(parts, method.getParams())
			      );
		try {
			int returnCode = client.executeMethod(method);

			if (returnCode == HttpStatus.SC_NOT_IMPLEMENTED) {
				System.err
						.println("The Post method is not implemented by this URI");
				// still consume the response body
				method.getResponseBodyAsString();
			} else {
				br = new BufferedReader(new InputStreamReader(
						method.getResponseBodyAsStream()));
				String readLine;
				while (((readLine = br.readLine()) != null)) {
					System.err.println(readLine);
				}
			}
		} catch (Exception e) {
			System.err.println(e);
		} finally {
			method.releaseConnection();
			if (br != null)
				try {
					br.close();
				} catch (Exception fe) {
				}
		}

		return null;

	}
    public String[] readConfig(String loc){    	
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
        	      if(strLine.startsWith("server")){
        	    	  StringTokenizer st = new StringTokenizer(strLine,"=");
        	    	  while(st.hasMoreTokens()) {
        	    		  serverList.add(st.nextToken());
        	    	  } 
        	      }
        	  }
        	  in.close();
        }
        catch (Exception e){
        	  System.err.println("Error: " + e.getMessage());
        }
        String server[]=(String[]) serverList.toArray(new String[serverList.size()]); 
        return server;
    }
	/**
	 *         locateDoc method returns the results from the psiX
	 */

	public String locateDoc(String xmlpath, String xmldtd, String replica) {
		// TODO Auto-generated method stub
		ClientHttpRequest clientReq = null;
		String output = "";
		try {
			/*
			 * Httprequest to psiX website
			 */
			clientReq = new ClientHttpRequest(
					"http://ec2-54-234-205-37.compute-1.amazonaws.com/cgi-bin/query.cgi");

			clientReq.setParameter("xmlpath", xmlpath);
			clientReq.setParameter("xmldtd", new File(xmldtd));
			clientReq.setParameter("replica", replica);

			InputStream input = clientReq.post();

			int data = input.read();
			while (data != -1) {
				output = output + (char) data;
				data = input.read();
			}
			input.close();
			// System.out.println(output);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return output;

	}

	/**
	 *         retrieveURLnDocID method accepts htmlText string and seperates
	 *         url and docid , adds it to PsiXObject and then to ArrayList
	 */
	public ArrayList<PsiXObject> retrieveURLnDocID(String htmlText) {
		PsiXObject psiXObject = null;
		String token1;
		String urlTrimmed;
		StringTokenizer stToken1 = new StringTokenizer(htmlText);
		StringTokenizer stToken2 = null;

		String urlString;
		ArrayList<PsiXObject> psiXObjects = new ArrayList<PsiXObject>();

		while (stToken1.hasMoreElements()) {
			token1 = stToken1.nextElement().toString();
			if (token1.equals("/>[Docid:")) {
				urlString = stToken1.nextElement().toString();
				urlTrimmed = urlString.substring(0, urlString.length() - 3);

				System.out.println(urlTrimmed);

				stToken2 = new StringTokenizer(urlTrimmed, ":");

				psiXObject = new PsiXObject();

				if (stToken2.hasMoreElements() && stToken2.countTokens() == 2) {
					psiXObject.setUrl(stToken2.nextElement().toString());
					psiXObject.setDocId(stToken2.nextElement().toString());
					psiXObjects.add(psiXObject);
				}
			}
		}

		return psiXObjects;
	}
	public String getFileAsString(File file) {
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		DataInputStream dis = null;
		StringBuffer sb = new StringBuffer();
		try {
			fis = new FileInputStream(file);
			bis = new BufferedInputStream(fis);
			dis = new DataInputStream(bis);

			while (dis.available() != 0) {
				sb.append(dis.readLine() + "\n");
			}
			fis.close();
			bis.close();
			dis.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

}
