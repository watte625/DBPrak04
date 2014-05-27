import java.io.*;
import java.io.ObjectInputStream.GetField;

import javax.xml.parsers.*;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import DataModel.FactBook;
import DataModel.Grenze;
import DataModel.Koordinaten;
import DataModel.Land;
import DataModel.Religion;
import DataModel.Zeitabhaengig;



public class XMLBuilder {

	static boolean debug = true;
	
	public static Document convertStringToXML(String input) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		
		try {
			builder = factory.newDocumentBuilder();
			Document doc = builder.parse(new InputSource(new StringReader(input)));
			return doc;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/*
	public static void saveStringToFile(String input) {
		File dest = new File("baum.xml");
		try {
			FileWriter fw = new FileWriter(dest, false);
			fw.write(input);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	*/
	
	/**
	 * Speichert ein �bergebenes Document als XML.
	 * @param String Dateiname
	 * @param Document zuspeicherndes XML-Objekt
	 * 
	 */
	public static void saveXMLToFile(String name, Document doc) {
		File destDir = new File("loadedXMLs");
		destDir.mkdir();
		File destination = new File("loadedXMLs/"+name+".xml");
		
		DOMSource doms = new DOMSource(doc);
		StreamResult streamre = new StreamResult(destination);
		TransformerFactory transformer = TransformerFactory.newInstance();
		
		Transformer serializer;
		try {
			serializer = transformer.newTransformer();
			serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			serializer.setOutputProperty(OutputKeys.INDENT, "yes");
			serializer.transform(doms, streamre);
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	/**
	 * 
	 * @param String File path
	 */
	public static void parseXMLToClass(String filePath) {
		File source = new File(filePath);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		
		FactBook fb = new FactBook();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(source);
			doc.normalize();

			NodeList nList = doc.getElementsByTagName("country");
			out("amount of countries: "+nList.getLength());
			
			for(int i=0; i<nList.getLength(); i++) {
				//Jede node repr�sentiert ein Country
				Node node = nList.item(i);
				
				NodeList infoList = node.getChildNodes();
				out("Informationtags avaiable for Country: "+infoList.getLength());
			
				//Element entspricht einem Country
				Element element = (Element) node;
				Land land = new Land();
				land.name = element.getElementsByTagName("name").item(0).getTextContent();
				out(land.name);
				
				Zeitabhaengig zeit = new Zeitabhaengig();
				//Holt das Jahr aus dem Dateinamen.
				zeit.jahr = filePath.substring(filePath.length()-8, filePath.length()-4);
				out("Jahr: "+zeit.jahr);
			
				
				//Karte
				try {
					land.kartenverweise = element.getElementsByTagName("Map_references").item(0).getTextContent();
				}  catch (NullPointerException e) {
					out("Keine Kartenbeschreibungen f�r " + land.name + " verf�gbar");
				}
				
				//Allgemeine Inforationen
				try {
					Node temp = element.getElementsByTagName("Introduction").item(0);
					if(temp.getNodeType() == Node.ELEMENT_NODE) {
						Element tempElement = (Element) node;
						land.allgemeineInformationen = tempElement.getElementsByTagName("Background").item(0).getTextContent();
					}
				} catch (NullPointerException e) {
					out("Keine allgemeinen Informationen f�r " + land.name + " verf�gbar");
				}
				
				//Economy. F�r BIP und Agrarprodukte
				try{
					Node ecoNode = element.getElementsByTagName("Economy").item(0);
					if(ecoNode.getNodeType() == Node.ELEMENT_NODE) {
						Element ecoElement = (Element) ecoNode;
						
						try {
							Node bipNode = ecoElement.getElementsByTagName("GDP").item(0);
							if (bipNode.getNodeType() == Node.ELEMENT_NODE) {
								Element bipElement = (Element) bipNode;
								//BIP
								zeit.bip = bipElement.getTextContent();
							}
						} catch (NullPointerException e) {
							out("Keine BIP-Informationen f�r " + land.name + " verf�gbar");
						}
						
						//Agrarprodukte
						try {
							Node agrarNode = ecoElement.getElementsByTagName("Agriculture_products").item(0);
							if(agrarNode.getNodeType() == Node.ELEMENT_NODE) {
								Element agrarElement = (Element) agrarNode;
								
								NodeList agrarProducts = agrarElement.getElementsByTagName("name");
								//Durchl�uft alle Agrarprodukte
								for(int h=0; h<agrarProducts.getLength(); h++) {
									Node productNode = agrarProducts.item(h);
									if(productNode.getNodeType() == Node.ELEMENT_NODE) {
										Element productElement = (Element) productNode;
										//F�ge Agrarprodukt zur Liste hinzu
										zeit.agrarprodukte.add(productElement.getTextContent());
									}
								}
							}
						} catch(NullPointerException e) {
							out("Keine Agrarprodukt-Angaben f�r "+land.name+" verf�gbar");
						}
					}
					
				} catch (NullPointerException e) {
					out("KeineWirtschaftsinformationen f�r " + land.name + " verf�gbar");
				}
				
				//Geographie-Part
				try {
					// geoNode repr�sentiert den Tag Geography
					Node geoNode = element.getElementsByTagName("Geography").item(0);

						// geoList repr�sentiert die Tags innerhalb der
						// GeographyNode
						if (geoNode.getNodeType() == Node.ELEMENT_NODE) {
							Element geoElement = (Element) geoNode;

							// Area-Part
							try {
								Node areaNode = geoElement.getElementsByTagName("Area").item(0);
								if (areaNode.getNodeType() == Node.ELEMENT_NODE) {
									Element areaElement = (Element) areaNode;
									land.flaeche = areaElement.getElementsByTagName("total").item(0).getTextContent();
								}
							} catch (NullPointerException e) {
								out("Keine Fl�chenangaben f�r " + land.name + " verf�gbar");
							}

							
							
							//Naturkatastrophen
							try {
								land.naturkatastrophen = geoElement.getElementsByTagName("Natural_hazards").item(0).getTextContent();
							} catch (NullPointerException e) {
								out("Keine Naturkatastrophenbeschreibung f�r "+land.name+" verf�gbar");
							}
							
							//Lagebescheibung/Klima
							try{
								land.lagebeschreibung = geoElement.getElementsByTagName("Climate").item(0).getTextContent();
							} catch (NullPointerException e) {
								out("Keine Lagebeschreibung f�r "+land.name+" verf�gbar");
							}
							
							//Koordinaten
							try{
								Node geoCoord = geoElement.getElementsByTagName("Geographic_coordinates").item(0);
								if(geoCoord.getNodeType() == Node.ELEMENT_NODE) {
									Element geoCoordEle = (Element) geoCoord;
									Koordinaten koordinaten = new Koordinaten();
									
									//Breitengrad
									Node latitude = geoCoordEle.getElementsByTagName("Latitude").item(0);
									if(latitude.getNodeType() == Node.ELEMENT_NODE) {
										Element latitudeEle = (Element) latitude;
										String latitudeString = "";
										latitudeString += latitudeEle.getElementsByTagName("Degree").item(0).getTextContent() + ",";
										latitudeString += latitudeEle.getElementsByTagName("Minute").item(0).getTextContent();
										koordinaten.breitengrad = latitudeString;
									}
									
									//L�ngengrad
									Node longitude = geoCoordEle.getElementsByTagName("Longitude").item(0);
									if(longitude.getNodeType() == Node.ELEMENT_NODE) {
										Element longitudeEle = (Element) latitude;
										String longitudeString = "";
										longitudeString += longitudeEle.getElementsByTagName("Degree").item(0).getTextContent() + ",";
										longitudeString += longitudeEle.getElementsByTagName("Minute").item(0).getTextContent();
										koordinaten.laengengrad = longitudeString;
									}
								}
							} catch (NullPointerException e) {
								out("Keine Koordinaten f�r "+land.name+" verf�gbar");
							}
							
							//Grenzen
							try {
								//Land_Boundaries Tag
								Node landBoundNode = geoElement.getElementsByTagName("Land_boundaries").item(0);
								if(landBoundNode.getNodeType() == Node.ELEMENT_NODE) {
									Element landBoundElement = (Element) landBoundNode;
									
									//Bound_Countries Tag
									Node boundCountryNode = landBoundElement.getElementsByTagName("border_countries").item(0);

									if(boundCountryNode.getNodeType() == Node.ELEMENT_NODE) {
										Element bound = (Element) boundCountryNode;
										//Item-Liste
										NodeList items = bound.getElementsByTagName("item");
										for(int k=0; k<items.getLength(); k++) {
											if(items.item(k).getNodeType() == Node.ELEMENT_NODE) {
												Element eleItem = (Element)items.item(k);
												
												//Erstelle eine neue Grenze und f�ge sie dem Land hinzu
												Grenze grenze = new Grenze();
												grenze.laenge = eleItem.getElementsByTagName("value").item(0).getTextContent();
												grenze.land = eleItem.getElementsByTagName("name").item(0).getTextContent();
												land.grenze.add(grenze);
											}
										}
									}
								}
								
							} catch (NullPointerException e) {
								out("Keine Grenzbeschreibung f�r "+land.name+" verf�gbar");
							}
							
						}

				} catch (NullPointerException e) {
					out("Keine Geographiebeschreibungen f�r " + land.name
							+ " verf�gbar");
				}
					
				//## GOVERNMENT ##
				//Abk�rzung, (vielleicht Hauptstadt), alternativ Namen, Flaggen, Organisationen
				try{
					Node govNode = element.getElementsByTagName("Government").item(0);
					if(govNode.getNodeType() == Node.ELEMENT_NODE) {
						Element govElement = (Element) govNode;
						
						//Abk�rzung
						try {
							land.abkuerzung = govElement.getElementsByTagName("Data_code").item(0).getTextContent();
						} catch (NullPointerException e) {
							out("Keine Abk�rzung f�r " + land.name
									+ " verf�gbar");
						}
						
						//Alternativ Namen
						try {
							Node countryNameNode = govElement.getElementsByTagName("Country_name").item(0);
							if(countryNameNode.getNodeType() == Node.ELEMENT_NODE) {
								Element countryNameElement = (Element) countryNameNode;
								
								String alternativeNamen = "";
								try {
									alternativeNamen += countryNameElement.getElementsByTagName("conventional_long_form").item(0).getTextContent() + "";
								} catch (NullPointerException e) {
									out("Keine konventionelle lange Form des Namens f�r "+ land.name + " verf�gbar");
								}
								try {
									alternativeNamen += countryNameElement.getElementsByTagName("conventional_short_form").item(0).getTextContent() + "";
								} catch (NullPointerException e) {
									out("Keine konventionelle kurze Form des Namens f�r "+ land.name + " verf�gbar");
								}
								try {
									alternativeNamen += countryNameElement.getElementsByTagName("local_short_form").item(0).getTextContent() + "";
								} catch (NullPointerException e) {
									out("Keine nationale kurze Form des Namens f�r "+ land.name + " verf�gbar");
								}
								try {
									alternativeNamen += countryNameElement.getElementsByTagName("former").item(0).getTextContent() + "";
								} catch (NullPointerException e) {
									out("Keine formelle Form des Namens f�r "+ land.name + " verf�gbar");
								}
								try {
									alternativeNamen += countryNameElement.getElementsByTagName("local_long_form").item(0).getTextContent() + "";
								} catch (NullPointerException e) {
									out("Keine nationale lange Form des Namens f�r "+ land.name + " verf�gbar");
								}
								
								land.alternativerName = alternativeNamen;
							}
						} catch (NullPointerException e) {
							out("Keine (weiteren) alternative Namen f�r " + land.name + " verf�gbar");
						}
						
						// Flaggenbilder-Part
						try {
						land.flaggenbilder = govElement
								.getElementsByTagName("Flag_description")
								.item(0).getTextContent();
						} catch (NullPointerException e) {
							out("Keine Flaggenbeschreibung f�r "+land.name+" verf�gbar");
						}
						
						//Organisationen
						try {
							Node orgaNode = govElement.getElementsByTagName("International_organization_participation").item(0);
							if(orgaNode.getNodeType() == Node.ELEMENT_NODE) {
								Element orgaElement = (Element) orgaNode;
								
								NodeList orgaList = orgaElement.getElementsByTagName("name");
								for(int l = 0; l<orgaList.getLength(); l++) {
									Node orgaName = orgaList.item(l);
									Element orgaNameEle = (Element) orgaName;
									//F�ge den Organisationsnamen der Liste von Organisationen des Landes hinzu
									land.organisationen.add(orgaNameEle.getTextContent());
								}
							}
						} catch (NullPointerException e) {
							out("Keine Organisationen f�r " + land.name + " verf�gbar");
						}
						
						try{
							//TODO 
							//land.hauptstadt = govElement.getElementsByTagName("Capital").item(0).getTextContent();
						} catch (NullPointerException e) {
							out("Keine Hauptstadt f�r "+land.name+" verf�gbar");
						}
					}
					
				} catch(NullPointerException e) {
					out("Keine Regierungsangaben f�r "+land.name+" verf�gbar");
				}
				
				//People-Part. F�r Einwohnerzahl, Religionen und Verh�ltnisse
				
				try{
					Node peopleNode = element.getElementsByTagName("People").item(0);
					if(peopleNode.getNodeType() == Node.ELEMENT_NODE) {
						Element peopleElement = (Element) peopleNode;
						
						//Einwohnerzahl
						try {
						zeit.einwohnerzahl = peopleElement.getElementsByTagName("Population").item(0).getTextContent();
						} catch(NullPointerException e) {
							out("Keine Einwohnerzahlangaben f�r "+land.name+" verf�gbar");
						}
						
						//Lebenserwartung Maennlich und Weiblich
						try {
							Node erwartNode = peopleElement.getElementsByTagName("Life_expectancy_at_birth").item(0);
							if(erwartNode.getNodeType() == Node.ELEMENT_NODE) {
								Element erwartEle = (Element) erwartNode;
								
								//Lebenserwartung Maennlich
								zeit.lebenserwartungM = erwartEle.getElementsByTagName("male").item(0).getTextContent();
								
								//Lebenserwartung Weiblich
								zeit.lebenserwartungW = erwartEle.getElementsByTagName("female").item(0).getTextContent();
							}
							
						} catch(NullPointerException e) {
							out("Keine Lebenserwartungsangaben f�r "+land.name+" verf�gbar");
						}
						
						//Geburtsverh�ltnis
						try {
							Node sexRatioNode = peopleElement.getElementsByTagName("Sex_ratio").item(0);
							if(sexRatioNode.getNodeType() == Node.ELEMENT_NODE) {
								Element sexRatioElement = (Element) sexRatioNode;
								//Geburtsverh�ltnis
								zeit.geburtRatioMW = sexRatioElement.getElementsByTagName("at_birth").item(0).getTextContent();
								//Gesamtverh�ltnis
								zeit.gesamtRatioMW = sexRatioElement.getElementsByTagName("total_population").item(0).getTextContent();
							}
						} catch(NullPointerException e) {
							out("Keine Geschlechterangaben f�r "+land.name+" verf�gbar");
						}
						
						//Religionen
						try{
							Node religionsNode = peopleElement.getElementsByTagName("Religions").item(0);
							if(religionsNode.getNodeType() == Node.ELEMENT_NODE) {
								Element religionsElement = (Element) religionsNode;
								
								NodeList religionsItems = religionsElement.getElementsByTagName("item");
								
								for(int c=0; c<religionsItems.getLength(); c++) {
									Node itemNode = religionsItems.item(c);
									if(itemNode.getNodeType() == Node.ELEMENT_NODE) {
										Element itemElement = (Element) itemNode;
										
										Religion religion = new Religion();
										religion.bezeichnung = itemElement.getElementsByTagName("name").item(0).getTextContent();
										
										try {
										religion.prozentualerAnteil = itemElement.getElementsByTagName("value").item(0).getTextContent();
										} catch(NullPointerException e) {
											out("Keine Angaben zur prozentualen Verteilung der Religion "+religion.bezeichnung+" in "+land.name+" gefunden");
										}
										zeit.religionen.add(religion);
									}
								}
							}
						} catch(NullPointerException e) {
							out("Keine Religionsangaben f�r "+land.name+" verf�gbar");
						}
						
					}
					
				} catch(NullPointerException e) {
					out("Keine Einwohnerangaben f�r "+land.name+" verf�gbar");
				}
				
				
				//TODO: Erdteil
			
				

			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void out(Object input) {
		if(debug)
			System.out.println(input);
	}
	
}
