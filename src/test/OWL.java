package test;
import java.io.FileInputStream;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Vector;

import localsolver.LSExpression;
import localsolver.LSModel;
import localsolver.LocalSolver;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import scheduling.*;
import Data.Input;
import Data.InputGen;
import Data.Output;
import ilog.concert.IloIntVar;
import ilog.cplex.IloCplex;



public class OWL {
	
	

public static void main(String[] args) throws Exception {
		
		final int TIMER=4000 ;
		Vector<Tache> ListeTache=new Vector<Tache>();
		Vector<Tache> ListeTache2=new Vector<Tache>();
		Vector<Journee> ListeJournee=new Vector<Journee>();			
		Map<String, Integer> retournement = new HashMap<String, Integer>(); 
		Map<String, String> acro = new HashMap<String, String>();

		
		/*Lecture des donn�es*/
		System.out.println("Lecture des donn�es r�seau OWL !"); //debug	
		
		
		/*ENP RB73*/
		InputGen dataIn1 = new InputGen(2, 22,5,18,2,0); 	
		InputGen dataIn2 = new InputGen(2, 20,6,19,3,0);		
		String nomFichier = new String("input/ENP RB73.xlsx");
		FileInputStream file = new FileInputStream(nomFichier);
		XSSFWorkbook Classeur = new XSSFWorkbook(file);	

		ListeTache=dataIn1.getTaches(Classeur,"ENP RB 73 ELGO - EBIL");		
		ListeTache2=dataIn2.getTaches(Classeur,"ENP RB 73 EBIL - ELGO"); 
		ListeTache2.remove(0); // on enleve maux qui est en doublon 
		ListeTache.addAll(ListeTache2);
		
		/*ENP RE82*/
		nomFichier = new String("input/ENP RE82.xlsx");
		file = new FileInputStream(nomFichier);
		Classeur = new XSSFWorkbook(file);			
		
		Input dataIn = new Input(); 		
		ListeTache2=dataIn.getTaches(Classeur,"EBIL - EDTM"); 		
		ListeTache2.remove(0);
		ListeTache.addAll(ListeTache2);
		ListeTache2=dataIn.getTaches(Classeur,"EDTM - EBIL"); 
		ListeTache2.remove(0); // on enleve maux qui est en doublon 
		ListeTache.addAll(ListeTache2);
		
		
		/*ENP RB67 RB71*/
		nomFichier = new String("input/ENP RB67 RB71.xlsx");
		file = new FileInputStream(nomFichier);
		Classeur = new XSSFWorkbook(file);			
		
		InputGen dataIn3 = new InputGen(2,45,9,40, 6,0); 	
		InputGen dataIn4 = new InputGen(2,43,7,38,4,0);
		ListeTache2=dataIn3.getTaches(Classeur,"ENP RB 67 RB 71 EMST - HRAH"); 
		ListeTache2.remove(0); // on enleve maux qui est en doublon 
		ListeTache.addAll(ListeTache2);
		ListeTache2=dataIn4.getTaches(Classeur,"ENP RB 67 RB 71 HRAH - EMST"); 
		ListeTache2.remove(0); // on enleve maux qui est en doublon 
		ListeTache.addAll(ListeTache2);
		
		String date="week"; 
		
		/*Trains non commerciaux a ajouter*/
		 
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
		Residence r4=new Residence("R4",  competences,180, "Rahden");
		Residence r5=new Residence("R5",  competences,180, "Rheda-Wiedenbr�ck");
		//Residence r6=new Residence("R6",  competences,180, "Horn-BadMeinbg");
		//Residence r7=new Residence("R7",  competences,180, "Warendorf");
		//Residence r8=new Residence("R8",  competences,180, "Beelen");
		//Residence r9= new Residence("R9", competences, 180, "M�nster(Westf)Hbf"); 
		ListeTache.add(r1);
		ListeTache.add(r2);
		ListeTache.add(r3);
		ListeTache.add(r4);
		ListeTache.add(r5);
		//ListeTache.add(r6);
		//ListeTache.add(r7);
		//ListeTache.add(r8);
		//ListeTache.add(r9);
		
		
		
		/*Ajout manuel des journees*/
		for (int i=0; i<45; i++){
			Journee j = new Journee(competences, "R1");
			ListeJournee.add(j);
		}
		
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
			GenerationCplex g = new GenerationCplex(ListeTache.size(), ListeJournee.size(), 35000, 35000, 35000,30000, false, ListeJournee, ListeTache, retournement, Cplex, x,p, arc,v);
			res=g.solve(TIMER);
			
		
		
		
		
		
		/*Ecriture dans le fichier*/
		System.out.println("Traitement des donn�es"); //debug 
		Output o= new Output("Results_OWL.xlsx", ListeTache, ListeJournee, date, acro);
		try {
			o.ecritureResultats(ListeTache.size(), res);
		} catch (Exception e1) {
			System.out.println("Erreur d'�criture ");
			e1.printStackTrace();
		}

				
		System.out.println("Fin du Test, r�sultats dans Results_OWL.xlsx"  );		 //debug
}
		
	

}
