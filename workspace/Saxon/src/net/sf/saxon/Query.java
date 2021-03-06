package net.sf.saxon;

import net.psiX.connection.PsiXConnector;
import net.psiX.objects.PsiXObject;
import net.sf.saxon.event.*;
import net.sf.saxon.expr.PathMap;
import net.sf.saxon.instruct.TerminationException;
import net.sf.saxon.om.*;
import net.sf.saxon.query.*;
import net.sf.saxon.sxpath.XPathDynamicContext;
import net.sf.saxon.sxpath.XPathEvaluator;
import net.sf.saxon.sxpath.XPathExpression;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trace.XQueryTraceListener;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.SchemaException;
import net.sf.saxon.value.SequenceExtent;
import net.sf.saxon.value.UntypedAtomicValue;
import net.sf.saxon.value.Whitespace;
import org.xml.sax.InputSource;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.*;

/**
 * This <B>Query</B> class provides a command-line interface to the Saxon XQuery
 * processor.
 * <p>
 * <p/>
 * The XQuery syntax supported conforms to the W3C XQuery 1.0 drafts.
 * 
 * @author Michael H. Kay
 */

public class Query {

	protected Configuration config;
	protected Properties outputProperties = new Properties();
	protected boolean showTime = false;
	protected int repeat = 1;
	protected String sourceFileName = null;
	protected String queryFileName = null;
	protected boolean useURLs = false;
	protected String outputFileName = null;
	protected String moduleURIResolverClass = null;
	protected String uriResolverClass = null;
	protected boolean explain = false;
	protected boolean wrap = false;
	protected boolean pullMode = false;
	protected boolean projection = false;
	protected String languageVersion = null;
	protected boolean updating = false;
	protected boolean writeback = false;
	protected boolean backup = true;
	protected String explainOutputFileName = null;
	// private PrintStream traceDestination = System.err;
	private boolean closeTraceDestination = false;

	private boolean schemaAware = false;

	/**
	 * Get the configuration in use
	 * 
	 * @return the configuration
	 */

	protected Configuration getConfiguration() {
		return config;
	}

	/**
	 * Main program, can be used directly from the command line.
	 * <p>
	 * The format is:
	 * </P>
	 * <p>
	 * java net.sf.saxon.Query [options] <I>query-file</I>
	 * &gt;<I>output-file</I>
	 * </P>
	 * <p>
	 * followed by any number of parameters in the form {keyword=value}... which
	 * can be referenced from within the query.
	 * </p>
	 * <p>
	 * This program executes the query in query-file.
	 * </p>
	 * 
	 * @param args
	 *            List of arguments supplied on operating system command line
	 * @throws Exception
	 *             Indicates that a compile-time or run-time error occurred
	 */

	public static void main(String args[]) throws Exception {
		// the real work is delegated to another routine so that it can be used
		// in a subclass
		System.out.println("start");
		(new Query()).doQuery(args, "java net.sf.saxon.Query");
	}

	/**
	 * Support method for main program. This support method can also be invoked
	 * from subclasses that support the same command line interface
	 * 
	 * @param args
	 *            the command-line arguments
	 * @param command
	 *            name of the class, to be used in error messages
	 */

	public void doQuery(String args[], String command) {

		for (int i = 0; i < args.length; i++) {
			if (args[i].startsWith("-config:")) {
				File configFile = new File(args[i].substring(8));
				try {
					config = Configuration.readConfiguration((new StreamSource(
							configFile)));
					outputProperties = new Properties(
							config.getDefaultSerializationProperties());
					schemaAware = config
							.isLicensedFeature(Configuration.LicenseFeature.SCHEMA_AWARE_XQUERY);
				} catch (XPathException e) {
					quit(e.getMessage(), 2);
				}
				break;
			}
		}

		if (config == null) {
			schemaAware = testIfSchemaAware(args);
			config = Configuration.newConfiguration();
		}

		config.setHostLanguage(Configuration.XQUERY);

		StaticQueryContext staticEnv = config.newStaticQueryContext();
		staticEnv.getExecutable().setSchemaAware(schemaAware);
		DynamicQueryContext dynamicEnv = new DynamicQueryContext(config);

		// Check the command-line arguments.

		try {
			parseOptions(args, command, staticEnv, dynamicEnv);

			if (languageVersion != null) {
				staticEnv.setLanguageVersion(languageVersion);
			}
			if (updating) {
				staticEnv.setUpdatingEnabled(true);
			}

			if (moduleURIResolverClass != null) {
				Object mr = config.getInstance(moduleURIResolverClass, null);
				if (!(mr instanceof ModuleURIResolver)) {
					badUsage(command, moduleURIResolverClass
							+ " is not a ModuleURIResolver");
				}
				staticEnv.setModuleURIResolver((ModuleURIResolver) mr);
			}

			if (uriResolverClass != null) {
				config.setURIResolver(config.makeURIResolver(uriResolverClass));
				dynamicEnv.setURIResolver(config
						.makeURIResolver(uriResolverClass));
			}

			config.displayLicenseMessage();
			if (schemaAware
					&& !config
							.isLicensedFeature(Configuration.LicenseFeature.SCHEMA_AWARE_XQUERY)) {
				if ("EE".equals(config.getEditionCode())) {
					quit("Installed license does not allow schema-aware query",
							2);
				} else {
					quit("Schema-aware query requires Saxon Enterprise Edition",
							2);
				}
			}
			if (pullMode) {
				// config.setLazyConstructionMode(true);
			}

			if (explain) {
				config.setOptimizerTracing(true);
			}

			Source sourceInput = null;

			if (sourceFileName != null) {
				sourceInput = processSourceFile(sourceFileName, useURLs);
			}

			long startTime = (new Date()).getTime();
			if (showTime) {
				System.err.println("Analyzing query from " + queryFileName);
			}

			// Compile the query

			XQueryExpression exp;
			try {
				// psiX if query has collection('/~CDN')
				/*
				if (queryFileName.contains("collection('/~CDN')")) {
					PsiXConnector psiX = new PsiXConnector();
					ArrayList<PsiXObject> psiXObjsArrayL = psiX
							.retrieveURLnDocID(psiX
									.locateDoc(
											"Actor",
											"/home/tivakar/Desktop/Thesis/XML and DTD examples/actors.dtd",
											"1"));
					File file = new File("collc.xml");
					BufferedWriter collcXmlOut = new BufferedWriter(
							new FileWriter("collc.xml"));
					collcXmlOut.write("<collection>");

					Iterator<PsiXObject> psiXObjsItr = psiXObjsArrayL
							.iterator();
					PsiXObject psixObj = null;

					while (psiXObjsItr.hasNext()) {
						psixObj = psiXObjsItr.next();
						collcXmlOut.write("<doc href=\"");
						collcXmlOut.write("http://");
						// Varies for Data shipping
						collcXmlOut.write(psixObj.getHostname());
						collcXmlOut.write(psixObj.getPath());
						collcXmlOut.write("/>");
					}

					collcXmlOut.write("</collection>");

					collcXmlOut.close();

					queryFileName.replace("collection('/~CDN')",
							"collection('collc.xml')");

				}
				*/
				exp = compileQuery(staticEnv, queryFileName, useURLs);

				if (showTime) {
					long endTime = (new Date()).getTime();
					System.err.println("Analysis time: "
							+ (endTime - startTime) + " milliseconds");
					startTime = endTime;
				}

			} catch (XPathException err) {
				int line = -1;
				String module = null;
				if (err.getLocator() != null) {
					line = err.getLocator().getLineNumber();
					module = err.getLocator().getSystemId();
				}
				if (err.hasBeenReported()) {
					quit("Static error(s) in query", 2);
				} else {
					if (line == -1) {
						System.err.println("Static error in query: "
								+ err.getMessage());
					} else {
						System.err.println("Static error at line " + line
								+ " of " + module + ':');
						System.err.println(err.getMessage());
					}
				}
				exp = null;
				System.exit(2);
			}

			if (explain) {
				explain(exp);
			}

			// Load the source file (applying document projection if requested)

			exp.setAllowDocumentProjection(projection);
			processSource(sourceInput, exp, dynamicEnv);

			// Run the query (repeatedly, if the -repeat option was set)

			startTime = (new Date()).getTime();
			long totalTime = 0;
			int r;
			for (r = 0; r < repeat; r++) { // repeat is for internal
											// testing/timing
				try {
					OutputStream destination;
					if (outputFileName != null) {
						File outputFile = new File(outputFileName);
						if (outputFile.isDirectory()) {
							quit("Output is a directory", 2);
						}
						if (!outputFile.exists()) {
							File directory = outputFile.getParentFile();
							if (directory != null && !directory.exists()) {
								directory.mkdirs();
							}
							outputFile.createNewFile();
						}
						destination = new FileOutputStream(outputFile);
					} else {
						destination = System.out;
					}

					runQuery(exp, dynamicEnv, destination, outputProperties);
				} catch (TerminationException err) {
					throw err;
				} catch (XPathException err) {
					if (err.hasBeenReported()) {
						// err.printStackTrace();
						throw new XPathException(
								"Run-time errors were reported");
					} else {
						throw err;
					}
				}

				if (showTime) {
					long endTime = (new Date()).getTime();
					if (r >= 3) {
						totalTime += (endTime - startTime);
					}
					if (repeat != 100) {
						System.err.println("Execution time: "
								+ (endTime - startTime) + " milliseconds");
						System.err.println("Memory used: "
								+ (Runtime.getRuntime().totalMemory() - Runtime
										.getRuntime().freeMemory()));
					} else if (totalTime > 100000) {
						break;
					}
					startTime = endTime;
				}
			}

			if (repeat > 3) {
				System.err.println("Average execution time: "
						+ (totalTime / (double) (r - 3)) + " milliseconds");
			}

		} catch (TerminationException err) {
			quit(err.getMessage(), 1);
		} catch (XPathException err) {
			quit("Query processing failed: " + err.getMessage(), 2);
		} catch (TransformerFactoryConfigurationError err) {
			err.printStackTrace();
			quit("Query processing failed", 2);
		} catch (SchemaException err) {
			quit("Schema processing failed: " + err.getMessage(), 2);
		} catch (Exception err2) {
			// TODO: move LicenseException to net.sf.saxon
			if ("com.saxonica.config.LicenseException".equals(err2.getClass()
					.getName())) {
				quit(err2.getMessage(), 2);
			} else {
				err2.printStackTrace();
				quit("Fatal error during query: "
						+ err2.getClass().getName()
						+ ": "
						+ (err2.getMessage() == null ? " (no message)"
								: err2.getMessage()), 2);
			}
		}
	}

	/**
	 * Prescan the command line arguments to see if any of them imply use of a
	 * schema-aware processor
	 * 
	 * @param args
	 *            the command line arguments
	 * @return true if a schema-aware processor is needed
	 */

	protected boolean testIfSchemaAware(String[] args) {
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-sa") || args[i].startsWith("-val:")
					|| args[i].equals("-val") || args[i].equals("-vlax")
					|| args[i].equals("-xsd:")
					|| args[i].startsWith("-xsdversion:")) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Parse the options supplied on the command line
	 * 
	 * @param args
	 *            the command line arguments
	 * @param command
	 *            the name of the command that was used (for diagnostics only)
	 * @param dynamicEnv
	 *            the XQuery dynamic context
	 * @throws TransformerException
	 *             if failures occur. Note, the method may also invoke
	 *             System.exit().
	 */

	protected void parseOptions(String[] args, String command,
			StaticQueryContext staticEnv, DynamicQueryContext dynamicEnv)
			throws TransformerException {
		int i = 0;
		String additionalSchemas = null;
		while (i < args.length) {

			if (args[i].charAt(0) == '-') {
				String option;
				String value = null;
				int colon = args[i].indexOf(':');
				if (colon > 0 && colon < args[i].length() - 1) {
					option = args[i].substring(1, colon);
					value = args[i].substring(colon + 1);
				} else {
					option = args[i].substring(1);
				}
				if (option.startsWith("-")) {
					// --feature:value
					try {
						String feature = "http://saxon.sf.net/feature/"
								+ option.substring(1);
						config.setConfigurationProperty(feature, value);
						i++;
					} catch (Exception err) {
						badUsage(command, "unrecognized feature " + option
								+ ": " + err.getMessage());
					}
				} else if (option.equals("backup")) {
					if (!("on".equals(value) || "off".equals(value))) {
						badUsage(command,
								"-backup option must be -backup:on or -backup:off");
					}
					backup = "on".equals(value);
					i++;
				} else if (option.equals("config")) {
					// already handled
					i++;
				} else if (option.equals("cr")) { // collection resolver
					i++;
					if (value == null) {
						if (args.length < i + 2) {
							badUsage(command, "No resolver after -cr");
						}
						value = args[i++];
					}
					Object resolver = config.getInstance(value, null);
					if (!(resolver instanceof CollectionURIResolver)) {
						quit(value + " is not a CollectionURIResolver", 2);
					}
					config.setCollectionURIResolver((CollectionURIResolver) resolver);

				} else if (option.equals("ds")) { // linked tree
					config.setTreeModel(Builder.LINKED_TREE);
					i++;
				} else if (option.equals("dt")) { // tiny tree (default)
					config.setTreeModel(Builder.TINY_TREE);
					i++;
				} else if (option.equals("dtd")) {
					if ("on".equals(value)) {
						config.getParseOptions().setDTDValidationMode(
								Validation.STRICT);
					} else if ("off".equals(value)) {
						config.getParseOptions().setDTDValidationMode(
								Validation.SKIP);
					} else if ("recover".equals(value)) {
						config.getParseOptions().setDTDValidationMode(
								Validation.LAX);
					} else {
						badUsage(command, "-dtd option must be on|off|recover");
					}
					i++;
				} else if (option.equals("e")) { // explain
					explain = true;
					i++;
				} else if (option.equals("expand")) {
					if (!("on".equals(value) || "off".equals(value))) {
						badUsage(command,
								"-expand option must be 'on' or 'off'");
					}
					config.getParseOptions().setExpandAttributeDefaults(
							"on".equals(value));
					i++;
				} else if (option.equals("explain")) { // explain
					explain = true;
					i++;
				} else if (option.startsWith("explain:")) { // explain:filename
					explain = true;
					explainOutputFileName = value;
					i++;
				} else if (option.equals("ext")) {
					if (!("on".equals(value) || "off".equals(value))) {
						badUsage(command,
								"-ext option must be -ext:on or -ext:off");
					}
					config.setAllowExternalFunctions("on".equals(value));
					i++;
					// } else if (option.equals("ief")) {
					// StringTokenizer tokenizer = new StringTokenizer(value,
					// ";");
					// while (tokenizer.hasMoreTokens()) {
					// String token = tokenizer.nextToken();
					// Class theClass = config.getClass(token, false, null);
					// try {
					// staticEnv.getIntegratedFunctionLibrary().registerFunction(theClass);
					// } catch (XPathException err) {
					// badUsage(command, "-ief: " + err.getMessage());
					// }
					// }
					// i++;
				} else if (option.equals("l")) {
					if (!(value == null || "on".equals(value) || "off"
							.equals(value))) {
						badUsage(command, "-l option must be -l:on or -l:off");
					}
					config.setLineNumbering(!"off".equals(value));
					i++;

					// i++;
				} else if (option.equals("mr")) {
					i++;
					if (value == null) {
						if (args.length < i + 2) {
							badUsage(command, "No resolver after -cr");
						}
						value = args[i++];
					}
					moduleURIResolverClass = value;
				} else if (option.equals("-noext")) {
					i++;
					config.setAllowExternalFunctions(false);
				} else if (option.equals("o")) {
					i++;
					if (value == null) {
						if (args.length < i + 2) {
							badUsage(command, "No output file name after -o");
						}
						value = args[i++];
					}
					outputFileName = value;
				} else if (option.equals("opt")) {
					i++;
					config.setConfigurationProperty(
							FeatureKeys.OPTIMIZATION_LEVEL, value);
				} else if (option.equals("outval")) {
					if (schemaAware) {
						if (!(value == null || "recover".equals(value) || "fatal"
								.equals(value))) {
							badUsage(command,
									"-outval option must be 'recover' or 'fatal'");
						}
						config.setValidationWarnings("recover".equals(value));
					} else {
						quit("The -outval option requires a schema-aware processor",
								2);
					}
					i++;
				} else if (option.equals("p")) {
					i++;
					if (!(value == null || "on".equals(value) || "off"
							.equals(value))) {
						badUsage(command, "-p option must be -p:on or -p:off");
					}
					if (!"off".equals(value)) {
						config.setParameterizedURIResolver();
						useURLs = true;
					}
				} else if (option.equals("pipe")) {
					i++;
					if (!("push".equals(value) || "pull".equals(value))) {
						badUsage(command,
								"-pipe option must be -p:push or -p:pull");
					}
					if ("pull".equals(value)) {
						pullMode = true;
					}
				} else if (option.equals("projection")) {
					i++;
					if (!(value == null || "on".equals(value) || "off"
							.equals(value))) {
						badUsage(command,
								"-projection option must be -projection:on or projection:off");
					}
					if (!"off".equals(value)) {
						projection = true;
					}
				} else if (option.equals("pull")) {
					i++;
					pullMode = true;
				} else if (option.equals("q")) {
					i++;
					queryFileName = value;
				} else if (option.equals("qs")) {
					i++;
					queryFileName = "{" + value + "}";
				} else if (option.equals("qversion")) {
					i++;
					if (!("1.0".equals(value) || "1.1".equals(value))) {
						badUsage(command, "-qversion option must be 1.0 or 1.1");
					}
					languageVersion = value;
				} else if (option.equals("r")) {
					i++;
					if (value == null) {
						if (args.length < i + 2) {
							badUsage(command, "No URIResolver class after -r");
						}
						value = args[i++];
					}
					uriResolverClass = value;
				} else if (option.equals("repeat")) {
					i++;
					if (value == null) {
						badUsage(command, "No number after -repeat");
					} else {
						try {
							repeat = Integer.parseInt(value);
						} catch (NumberFormatException err) {
							badUsage(command, "Bad number after -repeat");
						}
					}
				} else if (option.equals("s")) {
					i++;
					if (value == null) {
						if (args.length < i + 2) {
							badUsage(command, "No source file name after -s");
						}
						value = args[i++];
					}
					sourceFileName = value;
				} else if (option.equals("sa")) {
					// already handled
					i++;
				} else if (option.equals("snone")) {
					config.setStripsWhiteSpace(Whitespace.NONE);
					i++;
				} else if (option.equals("sall")) {
					config.setStripsWhiteSpace(Whitespace.ALL);
					i++;
				} else if (option.equals("signorable")) {
					config.setStripsWhiteSpace(Whitespace.IGNORABLE);
					i++;
				} else if (option.equals("strip")) {
					i++;
					if (value == null) {
						value = "all";
					}
					if ("none".equals(value)) {
						// no action|| || "ignorable".equals(value)) {
					} else if ("all".equals(value)) {
						config.setStripsWhiteSpace(Whitespace.ALL);
					} else if ("ignorable".equals(value)) {
						config.setStripsWhiteSpace(Whitespace.IGNORABLE);
					} else {
						badUsage(command,
								"-strip must be none, all, or ignorable");
					}
				} else if (option.equals("t")) {
					System.err.println(config.getProductTitle());
					System.err.println(Configuration.getPlatform()
							.getPlatformVersion());
					config.setTiming(true);
					showTime = true;
					i++;
				} else if (option.equals("traceout")) {
					i++;
					if (value.equals("#err")) {
						// no action, this is the default
					} else if (value.equals("#out")) {
						dynamicEnv.setTraceFunctionDestination(System.out);
					} else if (value.equals("#null")) {
						dynamicEnv.setTraceFunctionDestination(null);
					} else {
						try {
							dynamicEnv
									.setTraceFunctionDestination(new PrintStream(
											new FileOutputStream(
													new File(value))));
							closeTraceDestination = true;
						} catch (FileNotFoundException e) {
							badUsage(command, "Trace output file " + value
									+ " cannot be created");
						}
					}
				} else if (option.equals("tree")) {
					if ("linked".equals(value)) {
						config.setTreeModel(Builder.LINKED_TREE);
					} else if ("tiny".equals(value)) {
						config.setTreeModel(Builder.TINY_TREE);
					} else if ("tinyc".equals(value)) {
						config.setTreeModel(Builder.TINY_TREE_CONDENSED);
					} else {
						badUsage(command,
								"-tree option must be linked|tiny|tinyc");
					}
					i++;
				} else if (option.equals("T")) {
					i++;
					if (value == null) {
						config.setTraceListener(new XQueryTraceListener());
					} else {
						config.setTraceListenerClass(value);
					}
					config.setLineNumbering(true);
				} else if (option.equals("TJ")) {
					i++;
					config.setTraceExternalFunctions(true);
				} else if (option.equals("TL")) {
					if (args.length < i + 2) {
						badUsage(command, "No TraceListener class specified");
					}
					config.setTraceListenerClass(args[++i]);
					config.setLineNumbering(true);
					i++;
				} else if (option.equals("u")) {
					useURLs = true;
					i++;
				} else if (option.equals("update")) {
					i++;
					if (!(value == null || "on".equals(value)
							|| "off".equals(value) || "discard".equals(value))) {
						badUsage(command,
								"-update option must be on|off|discard");
					}
					if (!"off".equals(value)) {
						updating = true;
					}
					writeback = !("discard".equals(value));
					// } else if (option.equals("untyped")) {
					// // TODO: this is an experimental undocumented option. It
					// should be checked for consistency
					// config.setAllNodesUntyped(true);
					// i++;
				} else if (option.equals("v")) {
					config.setValidation(true);
					i++;
				} else if (option.equals("val")) {
					if (!schemaAware) {
						quit("The -val option requires a schema-aware processor",
								2);
					} else if (value == null || "strict".equals(value)) {
						config.setSchemaValidationMode(Validation.STRICT);
					} else if ("lax".equals(value)) {
						config.setSchemaValidationMode(Validation.LAX);
					} else {
						badUsage(command,
								"-val option must be 'strict' or 'lax'");
					}
					i++;
				} else if (option.equals("vlax")) {
					if (schemaAware) {
						config.setSchemaValidationMode(Validation.LAX);
					} else {
						quit("The -vlax option requires a schema-aware processor",
								2);
					}
					i++;
				} else if (option.equals("vw")) {
					if (schemaAware) {
						config.setValidationWarnings(true);
					} else {
						quit("The -vw option requires a schema-aware processor",
								2);
					}
					i++;
				} else if (option.equals("wrap")) {
					if (!(value == null || "on".equals(value) || "off"
							.equals(value))) {
						badUsage(command,
								"-wrap option must be -wrap:on or -wrap:off");
					}
					if (!"off".equals(value)) {
						wrap = true;
					}
					i++;
				} else if (option.equals("x")) {
					i++;
					config.setSourceParserClass(value);
				} else if (option.equals("xi")) {
					if (!(value == null || "on".equals(value) || "off"
							.equals(value))) {
						badUsage(command,
								"-xi option must be -xi:on or -xi:off");
					}
					if (!"off".equals(value)) {
						config.setXIncludeAware(true);
					}
					i++;
				} else if (option.equals("1.1")) {
					config.setXMLVersion(Configuration.XML11);
					i++;
				} else if (option.equals("xmlversion")) {
					i++;
					if ("1.0".equals(value)) {
						config.setXMLVersion(Configuration.XML10);
					} else if ("1.1".equals(value)) {
						config.setXMLVersion(Configuration.XML11);
					} else {
						badUsage(command, "-xmlversion must be 1.0 or 1.1");
					}
				} else if (option.equals("xsd")) {
					i++;
					additionalSchemas = value;
				} else if (option.equals("xsdversion")) { // XSD 1.1
					i++;
					if (!("1.0".equals(value) | "1.1".equals(value))) {
						badUsage(command, "-xsdversion must be 1.0 or 1.1");
					}
					config.setConfigurationProperty(FeatureKeys.XSD_VERSION,
							value);
				} else if (option.equals("xsiloc")) {
					i++;
					if ("off".equals(value)) {
						config.setConfigurationProperty(
								FeatureKeys.USE_XSI_SCHEMA_LOCATION,
								Boolean.FALSE);
					} else if ("on".equals(value)) {
						config.setConfigurationProperty(
								FeatureKeys.USE_XSI_SCHEMA_LOCATION,
								Boolean.TRUE);
					} else {
						badUsage(value, "format: -xsiloc:(on|off)");
					}
				} else if (args[i].equals("-?")) {
					badUsage(command, "");
				} else if (args[i].equals("-")) {
					queryFileName = "-";
					i++;
				} else {
					badUsage(command, "Unknown option " + args[i]);
				}
			} else {
				break;
			}
		}

		if (queryFileName == null) {
			if (args.length < i + 1) {
				badUsage(command, "No query file name");
			}
			queryFileName = args[i++];
		}

		for (int p = i; p < args.length; p++) {
			String arg = args[p];
			int eq = arg.indexOf("=");
			if (eq < 1 || eq >= arg.length()) {
				badUsage(command, "Bad param=value pair on command line: "
						+ arg);
			}
			String argname = arg.substring(0, eq);
			String argvalue = (eq == arg.length() ? "" : arg.substring(eq + 1));
			if (argname.startsWith("!")) {
				// parameters starting with "!" are taken as output properties
				// Allow the prefix "!saxon:" instead of
				// "!{http://saxon.sf.net}"
				if (argname.startsWith("!saxon:")) {
					argname = "{" + NamespaceConstant.SAXON + "}"
							+ argname.substring(7);
				} else {
					argname = argname.substring(1);
				}
				outputProperties.setProperty(argname, argvalue);
			} else if (argname.startsWith("+")) {
				// parameters starting with "+" are taken as input documents
				Object sources = Transform.loadDocuments(argvalue, useURLs,
						config, true);
				dynamicEnv.setParameter(argname.substring(1), sources);
			} else if (argname.startsWith("?")) {
				// parameters starting with "?" are taken as XPath expressions
				XPathEvaluator xpe = new XPathEvaluator(getConfiguration());
				XPathExpression expr = xpe.createExpression(argvalue);
				XPathDynamicContext context = expr.createDynamicContext(null);
				ValueRepresentation val = SequenceExtent
						.makeSequenceExtent(expr.iterate(context));
				dynamicEnv.setParameter(argname.substring(1), val);
			} else {
				dynamicEnv.setParameter(argname, new UntypedAtomicValue(
						argvalue));
			}
		}

		if (additionalSchemas != null) {
			loadAdditionalSchemas(config, additionalSchemas);
		}
	}

	protected static void loadAdditionalSchemas(Configuration config,
			String additionalSchemas) throws TransformerException {
		StringTokenizer st = new StringTokenizer(additionalSchemas, ";");
		while (st.hasMoreTokens()) {
			String schema = st.nextToken();
			File schemaFile = new File(schema);
			if (!schemaFile.exists()) {
				throw new TransformerException("Schema document " + schema
						+ " not found");
			}
			config.addSchemaSource(new StreamSource(schemaFile));
		}
	}

	protected Source processSourceFile(String sourceFileName, boolean useURLs)
			throws TransformerException {
		// DOM code for comparison
		// long start = new Date().getTime();
		// try {
		// DocumentBuilderFactory factory =
		// DocumentBuilderFactory.newInstance();
		// factory.setNamespaceAware(true);
		// factory.setIgnoringElementContentWhitespace(true);
		// DocumentBuilder builder = factory.newDocumentBuilder();
		// org.w3c.dom.Document doc = builder.parse(new File(sourceFileName));
		// DocumentWrapper dom = new DocumentWrapper(doc, sourceFileName,
		// getConfiguration());
		// long end = new Date().getTime();
		// System.err.println("Build time " + (end - start) + "ms");
		// return dom;
		// } catch (ParserConfigurationException e) {
		// e.printStackTrace();
		// } catch (SAXException e) {
		// e.printStackTrace();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// return null;
		Source sourceInput;
		if (useURLs || sourceFileName.startsWith("http:")
				|| sourceFileName.startsWith("file:")) {
			sourceInput = config.getURIResolver().resolve(sourceFileName, null);
			if (sourceInput == null) {
				sourceInput = config.getSystemURIResolver().resolve(
						sourceFileName, null);
			}
		} else if (sourceFileName.equals("-")) {
			// take input from stdin
			sourceInput = new StreamSource(System.in);
		} else {
			File sourceFile = new File(sourceFileName);
			if (!sourceFile.exists()) {
				quit("Source file " + sourceFile + " does not exist", 2);
			}

			if (Configuration.getPlatform().isJava()) {
				InputSource eis = new InputSource(sourceFile.toURI().toString());
				sourceInput = new SAXSource(eis);
			} else {
				sourceInput = new StreamSource(sourceFile.toURI().toString());
			}
		}
		return sourceInput;
	}

	/**
	 * Compile the query
	 * 
	 * @param staticEnv
	 *            the static query context
	 * @param queryFileName
	 *            the filename holding the query (or "-" for the standard input)
	 * @param useURLs
	 *            true if the filename is in the form of a URI
	 * @return the compiled query
	 * @throws XPathException
	 *             if query compilation fails
	 * @throws IOException
	 *             if the query cannot be read
	 */

	protected XQueryExpression compileQuery(StaticQueryContext staticEnv,
			String queryFileName, boolean useURLs) throws XPathException,
			IOException {
		XQueryExpression exp;
		if (queryFileName.equals("-")) {
			Reader queryReader = new InputStreamReader(System.in);
			exp = staticEnv.compileQuery(queryReader);
		} else if (queryFileName.startsWith("{") && queryFileName.endsWith("}")) {
			// query is inline on the command line
			String q = queryFileName.substring(1, queryFileName.length() - 1);
			exp = staticEnv.compileQuery(q);
		} else if (useURLs || queryFileName.startsWith("http:")
				|| queryFileName.startsWith("file:")) {
			ModuleURIResolver resolver = staticEnv.getModuleURIResolver();
			boolean isStandardResolver = false;
			if (resolver == null) {
				resolver = staticEnv.getConfiguration()
						.getStandardModuleURIResolver();
				isStandardResolver = true;
			}
			while (true) {
				String[] locations = { queryFileName };
				Source[] sources;
				try {
					sources = resolver.resolve(null, null, locations);
				} catch (Exception e) {
					if (e instanceof XPathException) {
						throw (XPathException) e;
					} else {
						XPathException err = new XPathException(
								"Exception in ModuleURIResolver: ", e);
						err.setErrorCode("XQST0059");
						throw err;
					}
				}
				if (sources == null) {
					if (isStandardResolver) {
						// this should not happen
						quit("System problem: standard ModuleURIResolver returned null",
								4);
					} else {
						resolver = staticEnv.getConfiguration()
								.getStandardModuleURIResolver();
						isStandardResolver = true;
					}
				} else {
					if (sources.length != 1
							|| !(sources[0] instanceof StreamSource)) {
						quit("Module URI Resolver must return a single StreamSource",
								2);
					}
					String queryText = QueryReader.readSourceQuery(
							(StreamSource) sources[0], config.getNameChecker());
					exp = staticEnv.compileQuery(queryText);
					break;
				}
			}
		} else {
			InputStream queryStream = new FileInputStream(queryFileName);
			staticEnv.setBaseURI(new File(queryFileName).toURI().toString());
			exp = staticEnv.compileQuery(queryStream, null);
		}
		return exp;
	}

	/**
	 * Explain the results of query compilation
	 * 
	 * @param exp
	 *            the compiled expression
	 * @throws FileNotFoundException
	 *             if the destination for the explanation doesn't exist
	 * @throws XPathException
	 *             if other failures occur
	 */

	protected void explain(XQueryExpression exp) throws FileNotFoundException,
			XPathException {
		OutputStream explainOutput;
		if (explainOutputFileName == null) {
			explainOutput = System.err;
		} else {
			explainOutput = new FileOutputStream(
					new File(explainOutputFileName));
		}
		Properties props = ExpressionPresenter.makeDefaultProperties();
		Receiver diag = config.getSerializerFactory().getReceiver(
				new StreamResult(explainOutput),
				config.makePipelineConfiguration(), props);
		ExpressionPresenter expressionPresenter = new ExpressionPresenter(
				config, diag);
		exp.explain(expressionPresenter);
	}

	/**
	 * Process the supplied source file
	 * 
	 * @param sourceInput
	 *            the supplied source
	 * @param exp
	 *            the compiled XQuery expression
	 * @param dynamicEnv
	 *            the dynamic query context
	 * @throws XPathException
	 *             if processing fails
	 */

	protected void processSource(Source sourceInput, XQueryExpression exp,
			DynamicQueryContext dynamicEnv) throws XPathException {
		if (sourceInput != null) {
			ParseOptions options = new ParseOptions();
			if (showTime) {
				System.err.println("Processing " + sourceInput.getSystemId());
			}
			if (!exp.usesContextItem()) {
				System.err
						.println("Source document ignored - query does not access the context item");
				sourceInput = null;

			} else if (projection) {
				PathMap map = exp.getPathMap();
				PathMap.PathMapRoot contextRoot = map.getContextRoot();
				if (explain) {
					System.err.println("DOCUMENT PROJECTION: PATH MAP");
					map.diagnosticDump(System.err);
				}
				if (contextRoot != null) {
					if (contextRoot.hasUnknownDependencies()) {
						System.err
								.println("Document projection for the context document is not possible, "
										+ "because the query uses paths that defy analysis");
					} else {
						ProxyReceiver filter = config
								.makeDocumentProjector(contextRoot);
						options.addFilter(filter);
					}
				} else {
					System.err
							.println("Source document supplied, but query does not access the context item");
				}
			}
			if (sourceInput != null) {
				DocumentInfo doc = config.buildDocument(sourceInput, options);
				dynamicEnv.setContextItem(doc);
			}
		}
	}

	/**
	 * Run the query
	 * 
	 * @param exp
	 *            the compiled query expression
	 * @param dynamicEnv
	 *            the dynamic query context
	 * @param destination
	 *            the destination for serialized results
	 * @param outputProps
	 *            serialization properties defining the output format
	 * @throws XPathException
	 *             if the query fails
	 * @throws IOException
	 *             if input or output fails
	 */
	protected void runQuery(XQueryExpression exp,
			DynamicQueryContext dynamicEnv, OutputStream destination,
			final Properties outputProps) throws XPathException, IOException {
		if (exp.getExpression().isUpdatingExpression() && updating) {

			if (writeback) {
				final List<XPathException> errors = new ArrayList<XPathException>(
						3);
				UpdateAgent agent = new UpdateAgent() {
					public void update(NodeInfo node, Controller controller)
							throws XPathException {
						try {
							DocumentPool pool = controller.getDocumentPool();
							String documentURI = pool.getDocumentURI(node);
							if (documentURI != null) {
								QueryResult.rewriteToDisk(node, outputProps,
										backup, (showTime ? System.err : null));
							} else if (showTime) {
								System.err
										.println("Updated document discarded because it was not read using doc()");
							}
						} catch (XPathException err) {
							System.err.println(err.getMessage());
							errors.add(err);
						}
					}
				};
				exp.runUpdate(dynamicEnv, agent);

				if (!errors.isEmpty()) {
					throw errors.get(0);
				}
			} else {
				Set affectedDocuments = exp.runUpdate(dynamicEnv);
				if (affectedDocuments.contains(dynamicEnv.getContextItem())) {
					QueryResult.serialize(
							(NodeInfo) dynamicEnv.getContextItem(),
							new StreamResult(destination), outputProps);
				}
			}
		} else if (wrap && !pullMode) {
			SequenceIterator results = exp.iterator(dynamicEnv);
			DocumentInfo resultDoc = QueryResult.wrap(results, config);
			QueryResult.serialize(resultDoc, new StreamResult(destination),
					outputProps);
			destination.close();
		} else if (pullMode) {
			if (wrap) {
				outputProps.setProperty(SaxonOutputKeys.WRAP, "yes");
			}
			// outputProps.setProperty(OutputKeys.METHOD, "xml");
			// outputProps.setProperty(OutputKeys.INDENT, "yes");
			exp.pull(dynamicEnv, new StreamResult(destination), outputProps);
		} else {
			exp.run(dynamicEnv, new StreamResult(destination), outputProps);
		}
		if (closeTraceDestination) {
			dynamicEnv.getTraceFunctionDestination().close();
		}
	}

	/**
	 * Exit with a message
	 * 
	 * @param message
	 *            The message to be output
	 * @param code
	 *            The result code to be returned to the operating system shell
	 */

	protected static void quit(String message, int code) {
		System.err.println(message);
		System.exit(code);
	}

	// public void setPOption(Configuration config) {
	// config.getSystemURIResolver().setRecognizeQueryParameters(true);
	// }

	/**
	 * Report incorrect usage of the command line, with a list of the options
	 * and arguments that are available
	 * 
	 * @param name
	 *            The name of the command being executed (allows subclassing)
	 * @param message
	 *            The error message
	 */
	protected void badUsage(String name, String message) {
		if (!"".equals(message)) {
			System.err.println(message);
		}
		System.err.println(config.getProductTitle());
		System.err.println("Usage: " + name
				+ " [options] query {param=value}...");
		System.err.println("Options: ");
		System.err
				.println("  -backup:on|off        Save updated documents before overwriting");
		System.err.println("  -config:filename      Use configuration file");
		System.err
				.println("  -cr:classname         Use specified CollectionURIResolver class");
		System.err.println("  -dtd:on|off           Validate using DTD");
		System.err
				.println("  -expand:on|off        Expand defaults defined in schema/DTD");
		System.err
				.println("  -explain[:filename]   Display compiled expression tree");
		System.err
				.println("  -ext:[on|off]         Allow|Disallow external Java functions");
		System.err
				.println("  -ief:class;class;...  List of integrated extension functions");
		System.err
				.println("  -l:on|off             Line numbering for source document");
		System.err
				.println("  -mr:classname         Use specified ModuleURIResolver class");
		System.err.println("  -o:filename           Send output to named file");
		System.err
				.println("  -opt:0..10            Set optimization level (0=none, 10=max)");
		System.err
				.println("  -outval:recover|fatal Handling of validation errors on result document");
		System.err
				.println("  -p                    Recognize Saxon file extensions and query parameters");
		System.err
				.println("  -pipe:push|pull       Execute internally in push or pull mode");
		System.err
				.println("  -projection:[on|off]  Use source document projection");
		System.err.println("  -q:filename           Query file name");
		System.err
				.println("  -qs:string            Query string (usually in quotes)");
		System.err.println("  -qversion:1.0|1.1     XQuery language version");
		System.err.println("  -r:classname          Use URIResolver class");
		System.err
				.println("  -repeat:N             Repeat N times for performance measurement");
		System.err
				.println("  -s:file|URI           Provide initial context document");
		System.err
				.println("  -sa                   Schema-aware query (requires Saxon-EE)");
		System.err
				.println("  -strip:all|none|ignorable      Strip whitespace text nodes");
		System.err
				.println("  -t                    Display version and timing information");
		System.err
				.println("  -traceout:file|#null  Destination for fn:trace() output");
		System.err.println("  -tree:tiny|linked     Select tree model");
		System.err.println("  -T[:classname]        Use TraceListener class");
		System.err
				.println("  -TJ                   Trace calls to external Java functions");
		System.err
				.println("  -u                    Names are URLs not filenames");
		System.err
				.println("  -update:on|off|discard  Enable|Disable XQuery Update (needs Saxon-EE)");
		System.err.println("  -val:strict|lax       Validate using schema");
		System.err
				.println("  -wrap:on|off          Wrap result sequence in XML elements");
		System.err
				.println("  -x:classname          Parser (XMLReader) used for source files");
		System.err
				.println("  -xi:on|off            Expand XInclude on all documents");
		System.err
				.println("  -xmlversion:1.0|1.1   Version of XML to be handled");
		System.err
				.println("  -xsd:file;file..      Additional schema documents to be loaded");
		System.err
				.println("  -xsdlversion:1.0|1.1  Version of XML Schema to be used");
		System.err
				.println("  -xsiloc:on|off        Take note of xsi:schemaLocation");
		System.err
				.println("  --feature:value       Set configuration feature (see FeatureKeys)");
		System.err.println("  -?                    Display this message ");
		System.err
				.println("  param=value           Set query string parameter");
		System.err
				.println("  +param=value          Set query document parameter");
		System.err
				.println("  ?param=expression     Set query parameter using XPath");
		System.err.println("  !option=value         Set serialization option");
		if ("".equals(message)) {
			System.exit(0);
		} else {
			System.exit(2);
		}
	}

}

//
// The contents of this file are subject to the Mozilla Public License Version
// 1.0 (the "License");
// you may not use this file except in compliance with the License. You may
// obtain a copy of the
// License at http://www.mozilla.org/MPL/
//
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied.
// See the License for the specific language governing rights and limitations
// under the License.
//
// The Original Code is: all this file.
//
// The Initial Developer of the Original Code is Michael H. Kay.
//
// Portions created by (your name) are Copyright (C) (your legal entity). All
// Rights Reserved.
//
// Contributor(s): changes to allow source and/or stylesheet from stdin
// contributed by
// Gunther Schadow [gunther@aurora.regenstrief.org]
//
