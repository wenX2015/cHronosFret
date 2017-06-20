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

public class OWL_RB67RB71 {

public static void main(String[] args) throws Exception {
		
		final int TIMER=500 ;
		Vector<Tache> ListeTache=new Vector<Tache>();
		Vector<Tache> ListeTache2=new Vector<Tache>();
		Vector<Journee> ListeJournee=new Vector<Journee>();			
		Map<String, Integer> retournement = new HashMap<String, Integer>(); 
		Map<String, String> acro = new HashMap<String, String>();

		String nomFichier = new String("input/ENP RB67 RB71.xlsx");
		FileInputStream file = new FileInputStream(nomFichier);
		XSSFWorkbook Classeur = new XSSFWorkbook(file);	
		
		/*Lecture des donn�es*/
		System.out.println("Lecture des donn�es de " + nomFichier); //debug	
		
		InputGen dataIn1 = new InputGen(2,45,9,40, 6,0); 	//week
		InputGen dataIn2 = new InputGen(2,43,7,38,4,0);		//week
		String date= "week" ; 
//		InputGen dataIn1 =new InputGen(2,39,49,80,46,0) ; //saturday 
//		InputGen dataIn2= new InputGen(2,43,48,79,45,0) ; //saturday
//		String date = "saturday" ; 
//		InputGen dataIn1 =new InputGen(2,41,91,122,88,0) ; //sunday
//		InputGen dataIn2= new InputGen(2,41,90,121,87,0) ; //sunday
//		String date = "sunday"; 
		
		ListeTache=dataIn1.getTaches(Classeur,"ENP RB 67 RB 71 EMST - HRAH");
		ListeTache2=dataIn2.getTaches(Classeur,"ENP RB 67 RB 71 HRAH - EMST"); 
		ListeTache2.remove(0); //on enleve maux qui est en doublon 
		ListeTache.addAll(ListeTache2);
		
	
		/*Lecture des temps de retournement */
		//retournement = dataIn.getRetournement(Classeur, "retournements"); // TODO : aller d�commenter pour ajouter les vraies donn�es 
				
		/*Lecture des acronymes*/
		String nomFichierAcronyms =new String("input/acronyms.xlsx") ; 
		FileInputStream fileAcro = new FileInputStream(nomFichierAcronyms);
		XSSFWorkbook ClasseurAcro = new XSSFWorkbook(fileAcro);
		acro=dataIn1.getAcronyms(ClasseurAcro, "Acronyms");
		
		/*Ajout manuel des r�sidences*/ 
		Vector<String> competences=new Vector<String>();

		Residence r1=new Residence("R1", competences,180, "BielefeldHbf");
		Residence r2=new Residence("R2",  competences,180, "Rahden");
		Residence r3=new Residence("R3",  competences,180, "Rheda-Wiedenbr�ck");
		//Residence r4=new Residence("R4",  competences,180, "Warendorf");
		//Residence r5=new Residence("R5",  competences,180, "Beelen");
		//Residence r6= new Residence("R6", competences, 180, "M�nster(Westf)Hbf"); 

		ListeTache.add(r1);
		ListeTache.add(r2);
		ListeTache.add(r3);
	    //ListeTache.add(r4) ; 
		//ListeTache.add(r5); 
		//ListeTache.add(r6);
		
		
		/*Ajout manuel des journees*/
		for (int i=0; i<40; i++){
			Journee j = new Journee(competences, "R1");
			ListeJournee.add(j);
		}
		
		/*Cr�ation des tableaux*/
		Arc[][] arc=new Arc[ListeTache.size()][ListeTache.size()] ;
		Tache[][][] res= new Tache[ListeJournee.size()][ListeTache.size()][3]; 							
		
		/*Cr�ation du mod�le Cplex*/
		IloCplex Cplex = new IloCplex();		

		System.out.println("nb de taches =" + ListeTache.size());
		/*Cr�ation des variables*/
		IloIntVar[][][] x=new IloIntVar[ListeTache.size()][ListeTache.size()][ListeJournee.size()]; 
		IloIntVar[][][] p=new IloIntVar[ListeTache.size()][ListeTache.size()][ListeJournee.size()]; 
		IloIntVar[][][] v=new IloIntVar[ListeTache.size()][ListeTache.size()][ListeJournee.size()]; 	
										
		/*G�n�ration du graphe et r�solution*/
		GenerationCplex g = new GenerationCplex(ListeTache.size(), ListeJournee.size(), 37000, 35000, 35000,30000, false, ListeJournee, ListeTache, retournement, Cplex, x,p, arc,v);
		System.out.println("R�solution...");
		res=g.solve(TIMER);
					
		
		/*Ecriture dans le fichier*/
		System.out.println("Traitement des donn�es"); //debug 
		Output o= new Output("OWL_RB67RB71-" + date +".xlsx", ListeTache, ListeJournee, date, acro);
		try {
			o.ecritureResultats(ListeTache.size(), res);
		} catch (Exception e1) {
			System.out.println("Erreur d'�criture ");
			e1.printStackTrace();
		}

				
		System.out.println("Fin du Test, r�sultats dans OWL_RB67RB71 - "+date + ".xlsx"  );		 //debug
}
	
	
}
