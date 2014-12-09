package net.cdn.functions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import net.cdn.Objects.XQueryObject;
import net.psiX.connection.PsiXConnector;

public class Xq2MaximalXp {

	/**
	 * @param args
	 */
	// public HashMap<String, String> getMaximalXpath(String xQuery) {
	// xQuery =
	// "for $x in collection('...')/bookstore/book for $y in collection('////kc.umkc.edu/kc-users/home/t/tk27f/Desktop/Thesis/collc.xml')/bookstore/book where $x/ti/price>30 and $y/price>30 order by $x/title return $x/title";
	// String maxXpath = null;
	// ArrayList<String> xQuerySplits = getEachLines(xQuery);
	//
	// return(getMaxXpath(xQuerySplits));
	//
	// }

	public static void main(String[] args) {
		// String xQuery =
		// "for $x in collection('////kc.umkc.edu/kc-users/home/t/tk27f/Desktop/Thesis/collc1.xml')/W4F_DOC/Actor for $y in collection('////kc.umkc.edu/kc-users/home/t/tk27f/Desktop/Thesis/collc2.xml')/PersonName where $x/Name/FirstName = $y/GivenName return $x/Filmograph/Movie/Title";
		// // String xQuery =
		// //
		// "for $x in collection('...')/bookstore/book for $y in collection('////kc.umkc.edu/kc-users/home/t/tk27f/Desktop/Thesis/collc.xml')/bookstore/book where $x/ti/price>30 and $y/price>30 order by $x/title return $x/title";
		// String maxXpath = null;
		// ArrayList<String> xQuerySplits = getEachLines(xQuery);
		//
		// getMaxXpath(xQuerySplits);
		//
		// PsiXConnector p = new PsiXConnector();
		//
		// p.retrieveURLnDocID(p
		// .locateDoc(
		// "/W4F_DOC/Actor[Name/FirstName]",
		// "\\\\kc.umkc.edu\\kc-users\\home\\t\\tk27f\\Desktop\\Thesis\\DTDs\\actors.xml",
		// "1"));

	}

	/**
	 * Method to seperate each line of XQuery commands
	 * 
	 * @param XQuery
	 * @return
	 */
	public ArrayList<String> getEachLines(XQueryObject xQueryObj) {
		String cutWordOld = null;
		String cutWord = null;
		StringTokenizer strTknzr = null;
		String temp = null;
		String tempArray[] = null;
		String XQuery = null;

		ArrayList<String> xQuerySplits = new ArrayList<String>();

		XQuery = xQueryObj.getxQuery();
		int i;
		int countSplit;
		String tempXQuery = null;
		String addXQueryPart = null;

		while (!XQuery.equals("")) {

			cutWord = null;

			strTknzr = new StringTokenizer(XQuery, " ");

			while (strTknzr.hasMoreTokens()) {
				temp = strTknzr.nextToken().trim();
				if (temp.equalsIgnoreCase("for")
						|| temp.equalsIgnoreCase("let")
						|| temp.equalsIgnoreCase("where")
						|| temp.equalsIgnoreCase("order")
						|| temp.equalsIgnoreCase("return")) {
					cutWordOld = cutWord;
					cutWord = temp;
					if (cutWordOld != null
							&& !cutWord.equalsIgnoreCase("return")) {
						tempArray = XQuery.split(cutWord);
						i = 1;
						countSplit = tempArray.length;
						tempXQuery = "";
						if (cutWord.equals(cutWordOld)) {
							while (i < countSplit - 1) {
								if (tempXQuery.equals("")) {
									tempXQuery = cutWord + " "
											+ tempArray[i + 1].trim();
								} else {
									tempXQuery = tempXQuery + " " + cutWord
											+ " " + tempArray[i + 1].trim();
								}
								i++;
							}
							XQuery = tempXQuery;
							addXQueryPart = tempArray[1].trim();
							if (!addXQueryPart.equals("")) {
								xQuerySplits.add(cutWord + " " + addXQueryPart);
							}
						} else {
							XQuery = cutWord + " " + tempArray[1];
							if (!tempArray[1].trim().equals("")) {
								xQuerySplits.add(tempArray[0]);
							}

						}

						// System.out.println(tempArray[0]);//Remove to see o/p
						break;
					} else if (cutWord.equalsIgnoreCase("return")) {
						tempArray = XQuery.split(cutWord);

						// code needs to be added
						XQuery = cutWord + tempArray[1];
						// System.out.println(tempArray[0]);//Remove to see o/p
						xQuerySplits.add(tempArray[0]);
						// System.out.println(cutWord + tempArray[1]);
						xQuerySplits.add(cutWord + tempArray[1]);
						XQuery = "";
						break;
					}

				}
			}

		}
		return xQuerySplits;
	}

	public HashMap<String, String> getMaxXpath(ArrayList<String> xQuerySplits) {

		Iterator<String> itr = xQuerySplits.iterator();
		String temp = null;
		HashMap<String, String> hashMap = new HashMap<String, String>();
		HashMap<String, String> hMapInitialValue = new HashMap<String, String>();

		while (itr.hasNext()) {
			temp = (String) itr.next();
			// System.out.println(temp);
			if (temp.contains("for")) {

				StringTokenizer strTkzrFor = new StringTokenizer(temp);
				String tempFor = null;
				String variable = null;
				String temp1 = null;
				while (strTkzrFor.hasMoreTokens()) {
					tempFor = strTkzrFor.nextToken();

					if (tempFor.contains("$")) {
						variable = tempFor;
					}

					if (tempFor.contains("collection")) {
						StringTokenizer strTkzrForTemp = new StringTokenizer(
								tempFor, "()");
						int tokenNo = strTkzrForTemp.countTokens();
						int start = 1;
						while (start != tokenNo) {
							strTkzrForTemp.nextToken();
							start++;
						}
						String old = hashMap.get(variable);
						if (old != null) {
							temp1 = strTkzrForTemp.nextToken();
							hashMap.put(variable, old + temp1);
							hMapInitialValue.put(variable, old + temp1);
						} else {
							temp1 = strTkzrForTemp.nextToken();
							hashMap.put(variable, temp1);
							hMapInitialValue.put(variable, temp1);
						}

					}

				}

			} else if (temp.contains("let")) {

			} else if (temp.contains("where")) {
				// New code
				StringTokenizer strTkzrWhere = new StringTokenizer(temp);
				String tempWhere = null;
				String variable = null;
				String[] split1 = null;
				String constructString = null;
				String tempConstructString = null;
				boolean startbracket;// Start bracket [
				boolean startSlashIgnore;

				while (strTkzrWhere.hasMoreTokens()) {
					tempWhere = strTkzrWhere.nextToken();
					startbracket = false;
					startSlashIgnore = true;
					if (tempWhere.contains("$")) {
						if (tempWhere.contains("/")) {
							// Splitting based on /
							split1 = tempWhere.split("/");
							// Checking for occurrence of $
							int splitLength = split1.length;
							int i = 0;
							if (splitLength != 0) {
								while (i != splitLength) {
									tempConstructString = "";
									if (split1[i].contains("$")) {

										int j = i + 1;
										while (j != splitLength) {
											if (split1[j].contains("$")) {
												tempConstructString = tempConstructString
														+ "/"
														+ hashMap
																.get(split1[j]);
											} else {
												if (splitLength > 1
														&& !startbracket) {
													tempConstructString = tempConstructString
															+ "[";
													startbracket = true;// Start
																		// bracket
																		// [
																		// added

												}
												if (startSlashIgnore) {
													tempConstructString = tempConstructString
															+ split1[j];
													startSlashIgnore = false;
												} else {
													tempConstructString = tempConstructString
															+ "/" + split1[j];
												}
											}
											j++;
										}
										if (startbracket) {
											tempConstructString = tempConstructString
													+ "]";// close bracket ]
										}
										if (hashMap.get(split1[i]).contains(
												tempConstructString)) {
											constructString = hashMap
													.get(split1[i]);
										} else {
											constructString = hashMap
													.get(split1[i])
													+ tempConstructString;
										}
										hashMap.put(split1[i], constructString);
									}
									i++;
								}
							}

						}
					}
				}

			} else if (temp.contains("return")) {
				StringTokenizer strTkzrWhere = new StringTokenizer(temp);
				String tempWhere = null;
				String variable = null;
				String[] split1 = null;
				String constructString = null;
				String tempConstructString = null;
				boolean startbracket;// Start bracket [
				boolean startSlashIgnore;

				while (strTkzrWhere.hasMoreTokens()) {
					tempWhere = strTkzrWhere.nextToken();
					startbracket = false;
					startSlashIgnore = true;
					if (tempWhere.contains("$")) {
						tempWhere = tempWhere.replace("{", "");
						tempWhere = tempWhere.replace("}", "");
						if (tempWhere.contains("/")) {
							// Splitting based on /
							split1 = tempWhere.split("/");
							// Checking for occurrence of $
							int splitLength = split1.length;
							int i = 0;
							if (splitLength != 0) {
								while (i != splitLength) {
									tempConstructString = "";
									if (split1[i].contains("$")) {
										int j = i + 1;
										while (j != splitLength) {
											if (split1[j].contains("$")) {
												tempConstructString = tempConstructString
														+ "/"
														+ hashMap
																.get(split1[j]);
											} else {
												if (splitLength > 1
														&& !startbracket) {
													tempConstructString = tempConstructString
															+ "[";
													startbracket = true;// Start
																		// bracket
																		// [
																		// added

												}
												if (startSlashIgnore) {
													tempConstructString = tempConstructString
															+ split1[j];
													startSlashIgnore = false;
												} else {
													tempConstructString = tempConstructString
															+ "/" + split1[j];
												}
											}
											j++;
										}
										if (startbracket) {
											tempConstructString = tempConstructString
													+ "]";// close bracket ]
										}
										if (hashMap.get(split1[i]).contains(
												tempConstructString)) {
											constructString = hashMap
													.get(split1[i]);
										} else {
											constructString = hashMap
													.get(split1[i])
													+ tempConstructString;
										}
										hashMap.put(split1[i], constructString);
									}
									i++;
								}
							}

						}
					}
				}

			}
		}

		return hashMap;
	}
}
