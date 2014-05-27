package DataModel;

import java.util.*;

public class Zeitabhaengig {
	public String jahr;
	public String bip;
	public String einwohnerzahl;
	public List<Religion> religionen = new ArrayList<Religion>();
	//Bei Agrarprodukten existiert in der XML keine Mengenangabe.
	public List<String> agrarprodukte = new ArrayList<String>();
	public String lebenserwartungM;
	public String lebenserwartungW;
	public String geburtRatioMW;
	public String gesamtRatioMW;
}
