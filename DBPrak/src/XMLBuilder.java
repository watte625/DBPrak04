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
	 * Speichert ein übergebenes Document als XML.
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
				//Jede node repräsentiert ein Country
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
					out("Keine Kartenbeschreibungen für " + land.name + " verfügbar");
				}
				
				//Allgemeine Inforationen
				try {
					Node temp = element.getElementsByTagName("Introduction").item(0);
					if(temp.getNodeType() == Node.ELEMENT_NODE) {
						Element tempElement = (Element) node;
						land.allgemeineInformationen = tempElement.getElementsByTagName("Background").item(0).getTextContent();
					}
				} catch (NullPointerException e) {
					out("Keine allgemeinen Informationen für " + land.name + " verfügbar");
				}
				
				//Economy. FÜr BIP und Agrarprodukte
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
							out("Keine BIP-Informationen für " + land.name + " verfügbar");
						}
						
						//Agrarprodukte
						try {
							Node agrarNode = ecoElement.getElementsByTagName("Agriculture_products").item(0);
							if(agrarNode.getNodeType() == Node.ELEMENT_NODE) {
								Element agrarElement = (Element) agrarNode;
								
								NodeList agrarProducts = agrarElement.getElementsByTagName("name");
								//Durchläuft alle Agrarprodukte
								for(int h=0; h<agrarProducts.getLength(); h++) {
									Node productNode = agrarProducts.item(h);
									if(productNode.getNodeType() == Node.ELEMENT_NODE) {
										Element productElement = (Element) productNode;
										//Füge Agrarprodukt zur Liste hinzu
										zeit.agrarprodukte.add(productElement.getTextContent());
									}
								}
							}
						} catch(NullPointerException e) {
							out("Keine Agrarprodukt-Angaben für "+land.name+" verfügbar");
						}
					}
					
				} catch (NullPointerException e) {
					out("KeineWirtschaftsinformationen für " + land.name + " verfügbar");
				}
				
				//Geographie-Part
				try {
					// geoNode repräsentiert den Tag Geography
					Node geoNode = element.getElementsByTagName("Geography").item(0);

						// geoList repräsentiert die Tags innerhalb der
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
								out("Keine Flächenangaben für " + land.name + " verfügbar");
							}

							
							
							//Naturkatastrophen
							try {
								land.naturkatastrophen = geoElement.getElementsByTagName("Natural_hazards").item(0).getTextContent();
							} catch (NullPointerException e) {
								out("Keine Naturkatastrophenbeschreibung für "+land.name+" verfügbar");
							}
							
							//Lagebescheibung/Klima
							try{
								land.lagebeschreibung = geoElement.getElementsByTagName("Climate").item(0).getTextContent();
							} catch (NullPointerException e) {
								out("Keine Lagebeschreibung für "+land.name+" verfügbar");
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
									
									//Längengrad
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
								out("Keine Koordinaten für "+land.name+" verfügbar");
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
												
												//Erstelle eine neue Grenze und füge sie dem Land hinzu
												Grenze grenze = new Grenze();
												grenze.laenge = eleItem.getElementsByTagName("value").item(0).getTextContent();
												grenze.land = eleItem.getElementsByTagName("name").item(0).getTextContent();
												land.grenze.add(grenze);
											}
										}
									}
								}
								
							} catch (NullPointerException e) {
								out("Keine Grenzbeschreibung für "+land.name+" verfügbar");
							}
							
						}

				} catch (NullPointerException e) {
					out("Keine Geographiebeschreibungen für " + land.name
							+ " verfügbar");
				}
					
				//## GOVERNMENT ##
				//Abkürzung, (vielleicht Hauptstadt), alternativ Namen, Flaggen, Organisationen
				try{
					Node govNode = element.getElementsByTagName("Government").item(0);
					if(govNode.getNodeType() == Node.ELEMENT_NODE) {
						Element govElement = (Element) govNode;
						
						//Abkürzung
						try {
							land.abkuerzung = govElement.getElementsByTagName("Data_code").item(0).getTextContent();
						} catch (NullPointerException e) {
							out("Keine Abkürzung für " + land.name
									+ " verfügbar");
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
									out("Keine konventionelle lange Form des Namens für "+ land.name + " verfügbar");
								}
								try {
									alternativeNamen += countryNameElement.getElementsByTagName("conventional_short_form").item(0).getTextContent() + "";
								} catch (NullPointerException e) {
									out("Keine konventionelle kurze Form des Namens für "+ land.name + " verfügbar");
								}
								try {
									alternativeNamen += countryNameElement.getElementsByTagName("local_short_form").item(0).getTextContent() + "";
								} catch (NullPointerException e) {
									out("Keine nationale kurze Form des Namens für "+ land.name + " verfügbar");
								}
								try {
									alternativeNamen += countryNameElement.getElementsByTagName("former").item(0).getTextContent() + "";
								} catch (NullPointerException e) {
									out("Keine formelle Form des Namens für "+ land.name + " verfügbar");
								}
								try {
									alternativeNamen += countryNameElement.getElementsByTagName("local_long_form").item(0).getTextContent() + "";
								} catch (NullPointerException e) {
									out("Keine nationale lange Form des Namens für "+ land.name + " verfügbar");
								}
								
								land.alternativerName = alternativeNamen;
							}
						} catch (NullPointerException e) {
							out("Keine (weiteren) alternative Namen für " + land.name + " verfügbar");
						}
						
						// Flaggenbilder-Part
						try {
						land.flaggenbilder = govElement
								.getElementsByTagName("Flag_description")
								.item(0).getTextContent();
						} catch (NullPointerException e) {
							out("Keine Flaggenbeschreibung für "+land.name+" verfügbar");
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
									//Füge den Organisationsnamen der Liste von Organisationen des Landes hinzu
									land.organisationen.add(orgaNameEle.getTextContent());
								}
							}
						} catch (NullPointerException e) {
							out("Keine Organisationen für " + land.name + " verfügbar");
						}
						
						try{
							//TODO 
							//land.hauptstadt = govElement.getElementsByTagName("Capital").item(0).getTextContent();
						} catch (NullPointerException e) {
							out("Keine Hauptstadt für "+land.name+" verfügbar");
						}
					}
					
				} catch(NullPointerException e) {
					out("Keine Regierungsangaben für "+land.name+" verfügbar");
				}
				
				//People-Part. Für Einwohnerzahl, Religionen und Verhältnisse
				
				try{
					Node peopleNode = element.getElementsByTagName("People").item(0);
					if(peopleNode.getNodeType() == Node.ELEMENT_NODE) {
						Element peopleElement = (Element) peopleNode;
						
						//Einwohnerzahl
						try {
						zeit.einwohnerzahl = peopleElement.getElementsByTagName("Population").item(0).getTextContent();
						} catch(NullPointerException e) {
							out("Keine Einwohnerzahlangaben für "+land.name+" verfügbar");
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
							out("Keine Lebenserwartungsangaben für "+land.name+" verfügbar");
						}
						
						//Geburtsverhältnis
						try {
							Node sexRatioNode = peopleElement.getElementsByTagName("Sex_ratio").item(0);
							if(sexRatioNode.getNodeType() == Node.ELEMENT_NODE) {
								Element sexRatioElement = (Element) sexRatioNode;
								//Geburtsverhältnis
								zeit.geburtRatioMW = sexRatioElement.getElementsByTagName("at_birth").item(0).getTextContent();
								//Gesamtverhältnis
								zeit.gesamtRatioMW = sexRatioElement.getElementsByTagName("total_population").item(0).getTextContent();
							}
						} catch(NullPointerException e) {
							out("Keine Geschlechterangaben für "+land.name+" verfügbar");
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
							out("Keine Religionsangaben für "+land.name+" verfügbar");
						}
						
					}
					
				} catch(NullPointerException e) {
					out("Keine Einwohnerangaben für "+land.name+" verfügbar");
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
