import java.sql.ResultSet;

public class Main {

	static boolean debug = true;
	
	public static void main(String[] args) {
		//Database db = new Database();
		
		//ResultSet output = db.executeQuery("SELECT datei, dok FROM dbprak00.XMLDATEN");
		//db.readResultSet(output);
		
		//XMLBuilder.parseXMLToClass("GermanyTest.xml");
		XMLBuilder.parseXMLToClass("loadedXMLs/countries2000.xml");
	}
	
	public static void out(Object input) {
		if(debug)
			System.out.println(input);
	}
}
