package DataModel;

import java.util.*;

public class Land {
	public String name;
	public String alternativerName;
	public String flaggenbilder;
	public String kartenverweise;
	public String allgemeineInformationen;
	public String abkuerzung;
	public String flaeche;
	public String naturkatastrophen;
	public String lagebeschreibung;
	public List<Grenze> grenze = new ArrayList<Grenze>();
	public Erdteil erdteil;
	public List<String> orte = new ArrayList<String>();
	public Koordinaten koordinaten;
	public List<String> organisationen = new ArrayList<String>();
	public List<Zeitabhaengig> zeitabhaengig = new ArrayList<Zeitabhaengig>();
	
	
}
