package net.psiX.test;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

import net.psiX.connection.PsiXConnector;

public class SharePublish {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String dataSet = "1340/335_1";
		
		int set1 = 80;
		int set2 = 160;
		int set3 = 240;
		
		long publishDelay = 15000;
		
		File file = new File("/home/tk27f/Desktop/Thesis/Thesis datas/"
				+ dataSet);

		if (file.isDirectory()) {

			// Get all files in the directory
			File[] listOfFiles = file.listFiles();


			PsiXConnector psiX = new PsiXConnector();

			// Iterate through all files in the directory
			for (int index = 0; index < listOfFiles.length; index++) {

				file = listOfFiles[index];

				if (file.exists()) {
					try {
						// publish
						if (index < set1) {
							psiX.publishDoc(file.getPath(), null, "http://"
									+ "192.168.1.201:8080" + "/documents/"
									+ dataSet + "/" + file.getName());
							System.out.println("http://" + "192.168.1.201:8080"
									+ "/documents/" + dataSet + "/"
									+ file.getName());
						} else if (index >= set1 && index < set2) {

							psiX.publishDoc(file.getPath(), null, "http://"
									+ "192.168.1.202:8080" + "/documents/"
									+ dataSet + "/" + file.getName());
							System.out.println("http://" + "192.168.1.202:8080"
									+ "/documents/" + dataSet + "/"
									+ file.getName());
						} else if (index >= set2 && index < set3) {

							psiX.publishDoc(file.getPath(), null, "http://"
									+ "192.168.1.203:8080" + "/documents/"
									+ dataSet + "/" + file.getName());
							System.out.println("http://" + "192.168.1.203:8080"
									+ "/documents/" + dataSet + "/"
									+ file.getName());
						} else {

							psiX.publishDoc(file.getPath(), null, "http://"
									+ "192.168.1.206:8080" + "/documents/"
									+ dataSet + "/" + file.getName());
							System.out.println("http://" + "192.168.1.206:8080"
									+ "/documents/" + dataSet + "/"
									+ file.getName());
						}

						System.out.println(index);
						
						Thread.sleep(publishDelay);
						
					} catch (Exception e) {
						// TODO: handle exception
					}

				}

			}

		}
		
		
//		try {
//			Thread.sleep(1800000);
//		} catch (InterruptedException e2) {
//			// TODO Auto-generated catch block
//			e2.printStackTrace();
//		}
		
		
//		dataSet = "1340/670_3_4";
//		file = new File("/home/tk27f/Desktop/Thesis/Thesis datas/"
//				+ dataSet);
//
//		if (file.isDirectory()) {
//
//			// Get all files in the directory
//			File[] listOfFiles = file.listFiles();
//
//			// Get Ip Address
//			try {
//
//				InetAddress inetAdd = InetAddress.getLocalHost();
//				ipAdd = inetAdd.getHostAddress();
//
//			} catch (UnknownHostException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
//
//			PsiXConnector psiX = new PsiXConnector();
//
//			// Iterate through all files in the directory
//			for (int index = 0; index < listOfFiles.length; index++) {
//
//				file = listOfFiles[index];
//
//				if (file.exists()) {
//					try {
//						// publish
//						if (index < 200) {
//							psiX.publishDoc(file.getPath(), null, "http://"
//									+ "192.168.1.201:8080" + "/documents/"
//									+ dataSet + "/" + file.getName());
//							System.out.println("http://" + "192.168.1.201:8080"
//									+ "/documents/" + dataSet + "/"
//									+ file.getName());
//						} else if (index >= 200 && index < 400) {
//
//							psiX.publishDoc(file.getPath(), null, "http://"
//									+ "192.168.1.202:8080" + "/documents/"
//									+ dataSet + "/" + file.getName());
//							System.out.println("http://" + "192.168.1.202:8080"
//									+ "/documents/" + dataSet + "/"
//									+ file.getName());
//						} else if (index >= 400 && index < 550) {
//
//							psiX.publishDoc(file.getPath(), null, "http://"
//									+ "192.168.1.203:8080" + "/documents/"
//									+ dataSet + "/" + file.getName());
//							System.out.println("http://" + "192.168.1.203:8080"
//									+ "/documents/" + dataSet + "/"
//									+ file.getName());
//						} else {
//
//							psiX.publishDoc(file.getPath(), null, "http://"
//									+ "192.168.1.206:8080" + "/documents/"
//									+ dataSet + "/" + file.getName());
//							System.out.println("http://" + "192.168.1.206:8080"
//									+ "/documents/" + dataSet + "/"
//									+ file.getName());
//						}
//
//						Thread.sleep(20000);
//						System.out.println(index);
//					} catch (Exception e) {
//						// TODO: handle exception
//					}
//
//				}
//
//			}
//
//		}
	}

}