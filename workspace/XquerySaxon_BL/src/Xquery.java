import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import net.cdn.message.CDNMessage;
import net.cdn.message.HashCode;
import net.sf.saxon.Query;

/**
 * Servlet implementation class Xquery
 */
public class Xquery extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Xquery() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		// get the input stream from the applet
		InputStream in1 = request.getInputStream();

		// create an object input stream
		ObjectInputStream ois = new ObjectInputStream(in1);
		// XQueryReturnObject data_in = null;
		CDNMessage cdnMsg = null;

		// read the serialized data object
		try {
			// data_in = (XQueryReturnObject) ois.readObject();
			cdnMsg = (CDNMessage) ois.readObject();
			System.out.println((String) cdnMsg.getMessage());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		in1.close();

		// TODO Auto-generated method stub

		PrintWriter out = null;
		UUID uuid = UUID.randomUUID();

		// Filename using UUID
		String fileName = uuid.toString() + ".xml";

		String xQuery = request.getParameter("xquery");

		System.out.println(xQuery);
		try {

			// String[] args = {
			// "-qs:"
			// + "for $x in doc(\"books.xml\")/catalog/book return $x",
			// "-o:" + fileName };

			String[] args = { "-qs:" + xQuery, "-o:" + fileName };

			Query.main(args);

			response.setContentType("text/html");
			out = response.getWriter();

			// Open the file that is the first
			// command line parameter
			FileInputStream fstream = new FileInputStream(fileName);

			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			// Read File Line By Line
			while ((strLine = br.readLine()) != null) {
				// Print the content on the console
				out.println(strLine);
			}
			// Close the input stream
			in.close();

			File target = new File(fileName);

			// delete the temp file
			target.delete();

		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		// response.setContentType("application/x-java-serialized-object");

		// XQueryReturnObject data_in = null;
		CDNMessage cdnMsg = null;
		CDNMessage cdnMsgOutput = new CDNMessage();
		HashCode hashCode = null;

		String xQuery = null;

		// PrintWriter out = null;
		OutputStream out = null;
		InputStream in1 = null;
		ObjectInputStream ois = null;

		// read the serialized data object
		try {
			// get the input stream
			in1 = request.getInputStream();

			// create an object input stream
			ois = new ObjectInputStream(in1);

			// Object a = ois.readObject();

			// String vettri = (String) ois.readObject();
			// data_in = (XQueryReturnObject) a;
			// System.out.println(data_in.getName());

			cdnMsg = (CDNMessage) ois.readObject();
			// System.out.println("heheheheh---" + (String)cdnMsg.getMessage());
			// System.out.println("hohohohoh---"+ new
			// String((byte[])cdnMsg.getHashCodeFromEncrypted()));

//			long decryptStart = System.currentTimeMillis();
			
			xQuery = (String) cdnMsg.getMessage();
			
//			long decryptEnd = System.currentTimeMillis();
//			
//			System.out.println("decrypt time - "
//					+ ((decryptEnd - decryptStart) / 1000.0) + " secs");

			// String o = new String((byte[])cdnMsg.getHashCodeFromEncrypted());

			// hashCode = new HashCode();
			// byte[] origHashcode = hashCode.generateHash("g");
			// byte[] gotHashCode = (byte[])cdnMsg.getHashCodeFromEncrypted();

			// boolean blnResult = Arrays.equals(origHashcode,gotHashCode);
			// System.out.println("Are two byte arrays equal ? : " + blnResult);

			in1.close();
			// data_in = (XQueryReturnObject) ois.readObject();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// TODO Auto-generated method stub

		UUID uuid = UUID.randomUUID();

		// Filename using UUID
		String fileName = uuid.toString() + ".xml";

		// String xQuery = request.getParameter("xquery");

		// out = response.getWriter();

		out = response.getOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(out);

		System.out.println(xQuery);

		// remove this
		// xQuery =
		// "<ROOT>{ for $x0 in doc(\"psix/XMLFile/allfiles/1_2.xml\")/ClinicalDocument  return <TempRoot> {$x0//Patient}</TempRoot>}</ROOT>";
		// xQuery =
		// "<ROOT>{ for $x0 in doc(\"home/tk27f/Desktop/eclipse/psix/XMLFile/allfiles/1_2.xml\")/ClinicalDocument/RecordTarget/PatientRole[Patient=\"M\"]  return <TempRoot> {$x0//Patient}</TempRoot>}</ROOT>";
		// System.out.println(xQuery);

		try {

			// String[] args = {
			// "-qs:"
			// + "for $x in doc(\"books.xml\")/catalog/book return $x",
			// "-o:" + fileName };

			String[] args = { "-qs:" + xQuery, "-o:" + fileName };

//			long saxonStart = System.currentTimeMillis();

			Query.main(args);

//			long saxonEnd = System.currentTimeMillis();
//
//			System.out.println("saxon execution time - "
//					+ ((saxonEnd - saxonStart) / 1000.0) + " secs");

			response.setContentType("text/html");

			// Open the file that is the first
			// command line parameter
			FileInputStream fstream = new FileInputStream(fileName);

			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			String completeString = "";
			String strLine;

			// Read File Line By Line
			while ((strLine = br.readLine()) != null) {
				// Print the content on the console
				// out.println(strLine);
				completeString = completeString + strLine;
			}

			// For security
//			 long encryptStart = System.currentTimeMillis();
			 
			 cdnMsgOutput.setMessage(completeString);
			 
//			 long encryptEnd = System.currentTimeMillis();
//			 
//			 System.out.println("Encrypt output execution time - "
//						+ ((encryptEnd - encryptStart) / 1000.0) + " secs");
			 
			 Object sendCdnMsg = (Object) cdnMsgOutput;

			// without security
//			Object sendCdnMsg = (Object) completeString;

			// write the serialized data object to the output stream
			oos.writeObject(sendCdnMsg);
			oos.flush();
			oos.close();

			// Close the input stream
			in.close();

			File target = new File(fileName);

			// delete the temp file
			target.delete();

		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

}