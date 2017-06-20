package test;

import ilog.concert.IloIntVar;
import ilog.cplex.IloCplex;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import scheduling.Arc;
import scheduling.GenerationCplex;
import scheduling.Journee;
import scheduling.Residence;
import scheduling.Tache;
import Data.Input;
import Data.InputGen;
import Data.Output;
import Data.Input;
import Data.Input;

public class OWL_RE82_RB73 {public static void main(String[] args) throws Exception {
	
	final int TIMER=90 ;
	Vector<Tache> ListeTache=new Vector<Tache>();
	Vector<Tache> ListeTache2=new Vector<Tache>();
	Vector<Journee> ListeJournee=new Vector<Journee>();			
	Map<String, Integer> retournement = new HashMap<String, Integer>(); 
	Map<String, String> acro = new HashMap<String, String>();
	
	/*Lecture des donn�es*/
	System.out.println("Lecture des donn�es RE82 et RB73!"); //debug	
	

	/*ENP RB73*/
	InputGen dataIn1 = new InputGen(2, 22,5,18,2,0); 	//week
	InputGen dataIn2 = new InputGen(2, 20,6,19,3,0);	
	String date = "week" ; 
	
	//InputGen dataIn1 = new InputGen(2, 18,26,39,23,0); 	//saturday
	//InputGen dataIn2 = new InputGen(2, 20,28,41,25,0);	
	//String date = "saturday" ; 
	
	//InputGen dataIn1 = new InputGen(2, 15,47,60,44,0); 	//sunday 
	//InputGen dataIn2 = new InputGen(2, 18,49,62,46,0);	
	//String date = "sunday"
	
	String nomFichier = new String("input/ENP RB73.xlsx");
	FileInputStream file = new FileInputStream(nomFichier);
	XSSFWorkbook Classeur = new XSSFWorkbook(file);	

	ListeTache=dataIn1.getTaches(Classeur,"ENP RB 73 ELGO - EBIL");		
	ListeTache2=dataIn2.getTaches(Classeur,"ENP RB 73 EBIL - ELGO"); 
	ListeTache2.remove(0); // on enleve maux qui est en doublon 
	ListeTache.addAll(ListeTache2);
	System.out.println("RB73 lu avec succ�s"); //debug
	
	/*ENP RE82*/
	nomFichier = new String("input/ENP RE82.xlsx");
	file = new FileInputStream(nomFichier);
	Classeur = new XSSFWorkbook(file);			
	
	Input dataIn = new Input(); //week		
	ListeTache2=dataIn.getTaches(Classeur,"EBIL - EDTM"); 		
	ListeTache2.remove(0);// on enleve maux qui est en doublon 
	ListeTache.addAll(ListeTache2);
	ListeTache2=dataIn.getTaches(Classeur,"EDTM - EBIL"); 
	ListeTache2.remove(0); // on enleve maux qui est en doublon 
	ListeTache.addAll(ListeTache2);
	

	//InputGen dataIn3 = new InputGen(2, 17,29,44,26,0); 	//saturday
	//InputGen dataIn4 = new InputGen(2, 17,29,44,26,0);		
	//InputGen dataIn3 = new InputGen(2, 9,52,67,49,0); 	//sunday
	//InputGen dataIn4 = new InputGen(2, 10,52,67,49,0);	
	
//	ListeTache2=dataIn3.getTaches(Classeur,"EDTM - EBIL"); //sat-sun
//	ListeTache2.remove(0); // on enleve maux qui est en doublon 
//	ListeTache.addAll(ListeTache2);
//	ListeTache2=dataIn4.getTaches(Classeur,"EBIL - EDTM"); 
//	ListeTache2.remove(0); // on enleve maux qui est en doublon 
//	ListeTache.addAll(ListeTache2);
	
	
	/*Trains non commerciaux*/
	//String nomFichier2 = new String("input/EmptyTrains.xlsx");
	//FileInputStream file2 = new FileInputStream(nomFichier2);
	//XSSFWorkbook Classeur2 = new XSSFWorkbook(file2);	
	//InputEmptyTrains dataW = new InputEmptyTrains();		
	//ListeTache.addAll(dataW.getTaches(Classeur2, "OWL")); 
	
	/*Lecture des temps de retournement */
	//retournement = dataIn.getRetournement(Classeur, "retournements"); // TODO : aller d�commenter pour ajouter les vraies donn�es 
			
	/*Lecture des acronymes*/
	String nomFichierAcronyms =new String("input/acronyms.xlsx") ; 
	FileInputStream fileAcro = new FileInputStream(nomFichierAcronyms);
	XSSFWorkbook ClasseurAcro = new XSSFWorkbook(fileAcro);
	acro=dataIn1.getAcronyms(ClasseurAcro, "Acronyms");
	
	/*Ajout manuel des r�sidences*/ 
	Vector<String> competences=new Vector<String>();
	Residence r1=new Residence("R1", competences,180, "Detmold");
	Residence r2=new Residence("R2", competences,180, "BielefeldHbf");
	Residence r3=new Residence("R3", competences,180,"Lemgo-L�ttfeld");
	//Residence r4=new Residence("R4",  competences,180, "Horn-BadMeinbg");
	//Residence r5=new Residence("R5", competences, 180, "Lage(Lippe)"); 

	ListeTache.add(r1);
	ListeTache.add(r2);
	ListeTache.add(r3);
	//ListeTache.add(r4);
	//ListeTache.add(r5);
	
	/*Ajout manuel des journees*/
	for (int i=0; i<25; i++){
		Journee j = new Journee(competences, "R1");
		ListeJournee.add(j);
	}
	System.out.println("nb de taches =" + ListeTache.size());

	/*Cr�ation des tableaux*/
	Arc[][] arc=new Arc[ListeTache.size()][ListeTache.size()] ;
	Tache[][][] res= new Tache[ListeJournee.size()][ListeTache.size()][3]; 							
	
		/*Cr�ation du mod�le Cplex*/
		IloCplex Cplex = new IloCplex();		

		
		/*Cr�ation des variables*/
		IloIntVar[][][] x=new IloIntVar[ListeTache.size()][ListeTache.size()][ListeJournee.size()]; 
		IloIntVar[][][] p=new IloIntVar[ListeTache.size()][ListeTache.size()][ListeJournee.size()]; 
		IloIntVar[][][] v=new IloIntVar[ListeTache.size()][ListeTache.size()][ListeJournee.size()]; 	
									
		/*G�n�ration du graphe et r�solution*/
		System.out.println("G�n�ration du graphe"); 
		GenerationCplex g = new GenerationCplex(ListeTache.size(), ListeJournee.size(), 36000, 35000, 35000,30000, false, ListeJournee, ListeTache, retournement, Cplex, x,p, arc,v);
		res=g.solve(TIMER);
		
	
	/*Ecriture dans le fichier*/
	System.out.println("Traitement des donn�es"); //debug 
	Output o= new Output("RE82-RB73 --"+date +".xlsx", ListeTache, ListeJournee,date, acro);
	try {
		o.ecritureResultats(ListeTache.size(), res);
	} catch (Exception e1) {
		System.out.println("Erreur d'�criture ");
		e1.printStackTrace();
	}

			
	System.out.println("Fin du Test, r�sultats dans RE82-RB73--"+date +".xlsx"  );		 //debug
	}
	
	
	

}
