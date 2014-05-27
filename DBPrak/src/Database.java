import com.ibm.db2.jcc.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;

public class Database {
	
	static boolean debug = true;
	//Verbindung f�r die Datenbank
	Connection connection;
	//Host-Path
	String url = "jdbc:db2://leutzsch.informatik.uni-leipzig.de:50001/PRAK14A";
	//Durchzuf�hrende Query, wird sp�ter gef�llt
	String query = "";
	//Statement f�r das Query
	Statement statement;
	//Ergebnis der Query
	ResultSet rs;
	
	/**
	 * F�hrt eine Query auf der Datenbank aus.
	 * Dabei sollte die Query vom Typ SELECT sein.
	 * 
	 * @param String Auszuf�hrende Query (SELECT)
	 */
	public ResultSet executeQuery(String query) {
		
		//Baue Verbindung zur Datenbank auf
		connect();
		
		//Erstelle Statement
		try {
			statement = connection.createStatement();
		} catch (SQLException e) {
			out("Problem creating Statement");
			e.printStackTrace();
		}
		
		//F�hre Query durch
		try {
			rs = statement.executeQuery(query);
		} catch (SQLException e) {
			out("Problem executing Query");
			e.printStackTrace();
		}
		
		return rs;
		
	}
	
	/**
	 * Die Methode lie�t das ResultSet aus, �bergibt jede Zeile, die eine String-Repr�sentation 
	 * einer XML enth�lt an einen Parser, der aus dem String eine XML konstruiert und speichert diese.
	 * @param ResultSet 
	 */
	public void readResultSet(ResultSet result) {
		// Werte ResultSet aus
		try {
			// Schleifenvariable
			int count = 0;
			// Solange im ResultSet etwas auszulesen ist:
			while (rs.next()) {
				// Die Tabelle hat eine Null-Zeile am Ende. Also die ersten 6
				// Zeilen ausgeben, die 7. nicht mehr.
				if (count <= 5) {
					// Der XML-Content ist in der Datenbank als String
					// gespeichert.
					// Dieser wird in eine DOM-Struktur geparst und als Document
					// zwischengespeichert.
					Document doc = XMLBuilder.convertStringToXML(rs.getString(2));
					// Dieses DOM-Objekt wird lokal gespeichert, um
					// Zugriffszeiten w�hrend der Test-
					// zeit zu verringern
					XMLBuilder.saveXMLToFile(rs.getString(1), doc);
				}
				count++;
			}

		} catch (SQLException e) {
			out("Problem reading ResultSet");
			e.printStackTrace();
		}
	}
	
	/*
	 * L�dt den IBM DB2 Treiber, baut eine Verbindung zur Datenbank mit Username und Passwort auf
	 */
	public void connect() {
		String jdbc = "com.ibm.db2.jcc.DB2Driver";
		try {
			Class.forName(jdbc);
		} catch (ClassNotFoundException e) {
			out("Problem loading JDBC Driver for IBM DB2");
			e.printStackTrace();
		}
		
		try {
			connection = DriverManager.getConnection(url, "dbprak04", ".pRA$k04");
		} catch (Exception e) {
			out("Problem connecting to Database");
			e.printStackTrace();
		}
	}
	
	public static void out(Object input) {
		if(debug)
			System.out.println("Database: "+input);
	}
	
}
