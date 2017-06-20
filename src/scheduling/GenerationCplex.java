package scheduling;
import java.util.Map;
import java.util.Vector;


import ilog.concert.*;
import ilog.cplex.*;
import ilog.cplex.IloCplex.UnknownObjectException;
//import localsolver.*;

/**
 * Generation du mod�le
 * 
 * @author 9505389V
 *
 */
public class GenerationCplex {
	
	private final int nbTaches ; 
	private final int nbJourneesMax ;
	private final int amplitudeMax ;
	private final int amplitudeNuit; 
	private final int amplitudeCoupure; 
	private final int amplitudeMoyenneMax; 
	private Map<String, Integer> retournement ; 
	
	private boolean ResAssignee ; 
	
	private Vector<Journee> ListeJournee ; 
	private Vector<Tache> ListeTache ; 
	

	private IloCplex Cplex;
	
	private IloIntVar[][][] p; 
	private IloIntVar[][][] x; 
	private IloIntVar[][][] v; 
	private IloLinearNumExpr  releves;
	private IloLinearNumExpr objJournee ; 
	private IloNumExpr objTransfert ; 
	private Arc[][] arc; 
	

	/**
	 * 
	 * Constructeur de classe
	 * 
	 * @param nbTaches
	 * @param nbJourneesMax
	 * @param amplitudeMax
	 * @param amplitudeNuit
	 * @param amplitudeCoupure
	 * @param listeJournee
	 * @param listeTache
	 * @param retournement
	 * @param ls
	 * @param model
	 * @param x
	 * @param arc
	 */
	public GenerationCplex(int nbTaches, int nbJourneesMax, int amplitudeMax,
			int amplitudeNuit, int amplitudeCoupure, int amplitudeMoyenneMax, boolean ResAssignee,
			Vector<Journee> listeJournee, Vector<Tache> listeTache, Map<String, Integer> retournement ,
			IloCplex model, IloIntVar[][][] x,IloIntVar[][][]p, Arc[][] arc,	 IloIntVar[][][] v ) {
		this.nbTaches = nbTaches;
		this.nbJourneesMax = nbJourneesMax;
		this.amplitudeMax = amplitudeMax;
		this.amplitudeNuit = amplitudeNuit;
		this.amplitudeCoupure = amplitudeCoupure;
		this.amplitudeMoyenneMax= amplitudeMoyenneMax ; 
		this.ListeJournee = listeJournee;
		this.ListeTache = listeTache;
		this.retournement  = retournement; 
		this.Cplex = model;
		this.x = x;
		this.p=p ; 
		this.v=v ;
		this.arc = arc;
		this.ResAssignee=ResAssignee ;
		
	}
	


	/**
	 * 
	 * R�solution du mod�le
	 * @throws IloException 
	 * @throws Exception 
	 * 
	 */
	public Tache[][][] solve(int time) throws IloException {

		Cplex.setParam(IloCplex.DoubleParam.TiLim, time);		

		/* G�n�ration des arcs*/
		generationArc();		
		System.out.println("Graphe g�n�r� "); //debug 
		
		/*Calcul des rel�ves*/
		int[] r = new int [nbTaches] ; 
		r=releves(arc); 
		releves = chgt(r); 
		
		/*Fonction Objectif principale*/	
		
		objJournee=objectif(); 
		
		/*Contraintes*/		
		objTransfert = contraintesFlot(100, 1000); //wpass; wtaxi
		
		contraintesResidence();		
		
		IloLinearNumExpr [] violationC = new IloLinearNumExpr[nbTaches] ; 
		violationC= contraintesTaches(); 
		
		IloLinearNumExpr[] duree=new IloLinearNumExpr[nbJourneesMax];
		duree=contraintesAmplitude(); 	
		//contraintesAmplitudeMoyenne(duree); 
		//contraintesPause(2700) ; //45min d�coup�es en 15 min 
		
		
		
		/*Construction de l'obj pond�r�*/
		IloNumExpr obj = Cplex.numExpr(); 
		obj=Cplex.sum( Cplex.prod(10000,objJournee),Cplex.prod(1,objTransfert),Cplex.prod(10,releves));	//minimisation des journ�es	+ (p+v)+ releves
		Cplex.addMinimize(obj);


		/*Exportation mod�le */
		//Cplex.exportModel("modele.lp");

		/*R�solution */		     
	     Cplex.solve();	 

		 		 		    
		/*Ecriture des r�sultats en console - //debug */
		printSolState();
		printGeneration(); 
		printTab(); 
		//printSol(); 		
		
		
		/*R�sultat stock� et ordonn� dans res pour la sortie*/
		Tache [][][] res= new Tache[ListeJournee.size()][ListeTache.size()+2][3];
		res=getRes();				

		return res;
	}
	
	/**
	 * 
	 * G�n�ration des arcs et des variables de d�cision x, p,v 
	 * @throws IloException 
	 * 
	 */
	public void generationArc() throws IloException{ 
		
		/*D�claration de variables booleennes*/
		boolean tachesCompatibles, journeeCompatible1, journeeCompatible2  ;	
	
		/* G�n�ration des arcs */
	
	    for (int m = 0; m < nbTaches; m++) {
	        for(int n=0 ; n< nbTaches; n++){
	        	for (int j=0; j< nbJourneesMax; j++){
	        		
	        		tachesCompatibles=false ; 
	        		journeeCompatible1=false ;
	        		journeeCompatible2=false;
		        		
	        		if(m!=n){
	        			
	        			/*Compatibilit� m1-m2*/
	        			if (ListeTache.get(n) instanceof Mission){/*r-m ou m-m*/
	        				tachesCompatibles=ListeTache.get(m).compatibilite((Mission)ListeTache.get(n), retournement);
	        				
	        			} else if(ListeTache.get(m) instanceof Mission) { /* m-r */
	        				tachesCompatibles=ListeTache.get(m).compatibilite((Residence)ListeTache.get(n));
	        				//System.out.println("Compatibilit� t-r :" +ListeTache.get(m).getId()+"-"+ ListeTache.get(n).getId() + "="+ tachesCompatibles);//debug
	        			}
	        			
	        			/*Compatibilit� m1 - journee en competences */
	        			journeeCompatible1=ListeJournee.get(j).compatibilite_competence(ListeTache.get(m));
	        			//System.out.println("Compatibilit� j-m :" +j +"-"+ ListeTache.get(m).getId() + "="+ journeeCompatible1);//debug
	        			
	        			/*Compatibilit� m2-journ�e en competences */
	        			journeeCompatible2=ListeJournee.get(j).compatibilite_competence(ListeTache.get(n));
	        			//System.out.println("Compatibilit� j-m :" +j +"-"+ ListeTache.get(n).getId() + "="+ journeeCompatible2);//debug
	        			        		
	            		
	            		/*Si les conditions sont r�unies on cr�e l'arc*/         		
	            		if (tachesCompatibles && journeeCompatible1 && journeeCompatible2){	  
	            			if (arc[m][n]==null){
	            				arc[m][n]= new Arc(ListeTache.get(m), ListeTache.get(n),false);
	            				//System.out.println("arc " + ListeTache.get(m).id + ", "+ ListeTache.get(n).id);//debug
	            			}
	            			x[m][n][j] = Cplex.boolVar();   
	            			p[m][n][j]=Cplex.boolVar(); 	    
	            		} else if (tachesCompatibles  ){
	            			arc[m][n]=new Arc(ListeTache.get(m), ListeTache.get(n), false);
	            			p[m][n][j]=Cplex.boolVar() ; 
	            		}
	            		
	            		/* Cr�ation d'arcs pour trajets en voiture en PS : arcs entre tout couple (r,m)  */
	            		if(  n!=0 &&  (ListeTache.get(m) instanceof Residence && ListeTache.get(n) instanceof Mission) ){
	            			arc[m][n]=new Arc(ListeTache.get(m), ListeTache.get(n),false);
	            			v[m][n][j]=Cplex.boolVar() ;	//notaxi
	            		}
	            		
	            		/*Arc m-maux*/
	            		if(m!=0 && ListeTache.get(m)instanceof Mission){
	            			arc[m][0]=new Arc(ListeTache.get(m), ListeTache.get(0),false);
	            			x[m][0][j]=Cplex.boolVar() ;
	            		} 
	            		/*Arc maux-R*/
	            		if(ListeTache.get(n) instanceof Residence){
	            			arc[0][n]=new Arc(ListeTache.get(0), ListeTache.get(n),false);
	            			v[0][n][j]=Cplex.boolVar() ; //notaxi
	            		}
	        		
	        		}// Fin if
	        		
	        	}//Fin boucle 3
	        } //Fin boucle 2
	    } //Fin boucle 1
	}

	/**
	 * 
	 * Cr�e la fonction objectif principale du mod�le
	 * @return l'objectif journ�e
	 * @throws IloException 
	 */
	public IloLinearNumExpr objectif() throws IloException{
		/*Objectif 1 : minimiser le nombre de journ�es cr��es*/
		IloLinearNumExpr obj = Cplex.linearNumExpr(); 
	
		//System.out.println("Fonction objectif principale"); 
		
		/*On compte le nombre d'arcs R-M emprunt�s=nombre de journ�es*/
		for(int r=0 ; r< nbTaches; r++){
			for(int m=0; m<nbTaches; m++){
				for(int j=0; j<nbJourneesMax; j++){
					
					if(x[r][m][j]!=null && ListeTache.get(r) instanceof Residence 
										&& ListeTache.get(m) instanceof Mission){
						obj.addTerm(1,x[r][m][j]);
						//System.out.println("x[" + r + "][" + m +"][" + j +"]"); 
					} 
					
					if(p[r][m][j]!=null && ListeTache.get(r) instanceof Residence 
							&& ListeTache.get(m) instanceof Mission){
						obj.addTerm(1,p[r][m][j]);
						//System.out.println("p[" + r + "][" + m +"][" + j +"]"); 
					}
					if(v[r][m][j]!=null && ListeTache.get(r) instanceof Residence 
							&& ListeTache.get(m) instanceof Mission ){
						obj.addTerm(1,v[r][m][j]);		
						//System.out.println("v[" + r + "][" + m +"][" + j +"]"); 
					}
							
					
				}//Fin 3
			}//Fin 2
		}// Fin 1
						
		return obj ;		
	}

	
	///////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////////
	//Contraintes
	
	


	/**
	 * G�n�re les contraintes li�es � la r�sidence d'une journ�e de service  
	 * @throws IloException 
	 */
	public void contraintesResidence() throws IloException{
		IloLinearNumExpr res[]= new IloLinearNumExpr[nbJourneesMax]; //nombre total d'arcs sortant de r�sidences par journ�e
		IloLinearNumExpr resDepart[][]= new IloLinearNumExpr[nbJourneesMax][nbTaches]; 
		IloLinearNumExpr resArrivee[][]= new IloLinearNumExpr[nbJourneesMax][nbTaches];		
		IloLinearNumExpr voiture[]= new IloLinearNumExpr[nbJourneesMax]; // nombre de trajets en voiture par journ�e
		
		for(int j=0; j<nbJourneesMax; j++){
			 res[j]=Cplex.linearNumExpr();			
			 voiture[j]=Cplex.linearNumExpr();     	

		
			for(int r=0 ; r< nbTaches; r++){	
				
				/*Compteurs d'arcs entrants/sortants utilis�s*/
				resDepart[j][r]=Cplex.linearNumExpr();	
				resArrivee[j][r]=Cplex.linearNumExpr();
				
				/*Pour g�rer les assignations de r�sidence*/
				boolean residenceCompatible=true; 
	        	if(ResAssignee){	
	        		if(ListeTache.get(r) instanceof Residence ){            			
	        			Residence aux = new Residence((Residence) ListeTache.get(r));
	        			residenceCompatible=ListeJournee.get(j).compatibiliteResidence(aux);	            			
	        		}
	        	}
	        	
	        	/*Calcul des journ�es cr��es*/
				for(int m=0; m<nbTaches; m++){				
						
						/*R�sidence D�part conducteur*/
						if(x[r][m][j]!=null && ListeTache.get(r) instanceof Residence 
											&& ListeTache.get(m) instanceof Mission){
							res[j].addTerm(x[r][m][j],1);
							resDepart[j][r].addTerm(x[r][m][j],1);
						}		
						
						/* R�sidence D�part passager*/
						if(p[r][m][j]!=null && ListeTache.get(r) instanceof Residence 
											&& ListeTache.get(m) instanceof Mission){
							res[j].addTerm(p[r][m][j],1);
							resDepart[j][r].addTerm(p[r][m][j],1);			
						}		
						
						/*R�sidence d�part voiture*/
						if(v[r][m][j]!=null && ListeTache.get(r) instanceof Residence 
											&& ListeTache.get(m) instanceof Mission){
							voiture[j].addTerm(v[r][m][j],1);
							res[j].addTerm(v[r][m][j],1);
							resDepart[j][r].addTerm(v[r][m][j],1);
						}
						
						/* R�sidence Arriv�e conducteur*/
						if(x[m][r][j]!=null && ListeTache.get(r) instanceof Residence 
											&& ListeTache.get(m) instanceof Mission){
							resArrivee[j][r].addTerm(x[m][r][j],1);
						}		
						
						/* R�sidence Arriv�e passager */
						if(p[m][r][j]!=null && ListeTache.get(r) instanceof Residence 
											&& ListeTache.get(m) instanceof Mission){
							resArrivee[j][r].addTerm(p[m][r][j],1);
						}	
						
						/* R�sidence Arriv�e passager */
						if(v[m][r][j]!=null && ListeTache.get(r) instanceof Residence 
											&& ListeTache.get(m) instanceof Mission){
							resArrivee[j][r].addTerm(v[m][r][j],1);
							voiture[j].addTerm(v[m][r][j],1);
						}	
						
					}//Fin 3
				
				/*Contrainte : La journ�e commence et finit � la m�me r�sidence*/				
				Cplex.addEq(resDepart[j][r], resArrivee[j][r]);

				
				/*Si la r�sidence est fixe, mais non compatible avec la journ�e, alors on ne peut pas les associer */
				if (!residenceCompatible){	
					Cplex.addEq(resDepart[j][r],0);
				}
				
			}//Fin 2
			/*La journ�e commence � une seule r�sidence (ou n'est pas utilis�e) */
			Cplex.addLe(res[j], 1);	//option 1 : contrainte 			
			

			//model.constraint(model.leq(voiture[j], 2));
		
		}// Fin 1

	}

/**
 * G�n�re les contraintes de conservation du flot
 * @return le nombre de trajets passagers/ en taxi
 * @throws IloException 
 */
	public IloNumExpr contraintesFlot(int wPass, int wTaxi) throws IloException{
		
		IloLinearIntExpr[][] entree= new IloLinearIntExpr[nbTaches][nbJourneesMax];
		IloLinearIntExpr[][] sortie= new IloLinearIntExpr[nbTaches][nbJourneesMax];
		
		IloLinearNumExpr objPassager;
		objPassager = Cplex.linearNumExpr();  
		IloLinearNumExpr objTaxi;
		objTaxi = Cplex.linearNumExpr(); 	

		
		for(int m=0; m< nbTaches; m++){
			for(int j=0; j<nbJourneesMax; j++){
			
			entree[m][j] = Cplex.linearIntExpr(); 
			sortie[m][j] = Cplex.linearIntExpr(); 
			
		
				for(int n=0; n<nbTaches; n++){	
						if(x[m][n][j]!=null){
							sortie[m][j].addTerm(x[m][n][j],1);
						} 
						
						if(p[m][n][j]!=null){
							sortie[m][j].addTerm(p[m][n][j],1);
							objPassager.addTerm(p[m][n][j],1);
						}
						
						if(v[m][n][j]!=null){
							sortie[m][j].addTerm(v[m][n][j],1);
							objTaxi.addTerm(v[m][n][j],1); 
						}
					}//Fin 3
					
	
					for(int n=0; n<nbTaches; n++){
						if(x[n][m][j]!=null){
							entree[m][j].addTerm(x[n][m][j],1);
						} 
						
						if(p[n][m][j]!=null){
							entree[m][j].addTerm(p[n][m][j],1);
							objPassager.addTerm(p[n][m][j],1);	
						}
						
						if(v[n][m][j]!=null){
							entree[m][j].addTerm(v[n][m][j],1);
							objTaxi.addTerm(v[n][m][j],1);
						}
						
					}//Fin 3
					Cplex.addEq(entree[m][j],sortie[m][j]);

			}//Fin2						
	
		}//Fin 1
		
		/*Pond�ration de l'objectif*/
		IloNumExpr objectif =Cplex.numExpr(); 
		objectif= Cplex.sum(Cplex.prod(objPassager,wPass),Cplex.prod(objTaxi,wTaxi)) ;
		return objectif ; 
	}

	/**
	 * 
	 * Cr�e la contrainte de couverture des taches
	 * @throws IloException 
	 */
	public IloLinearNumExpr[] contraintesTaches() throws IloException{
		//System.out.println("Couverture des t�ches: ");  //debug
		IloLinearNumExpr[] couverture =new IloLinearNumExpr[nbTaches]; 
				
		for (int m=0; m<nbTaches; m++){
			couverture[m] =Cplex.linearNumExpr() ; 
			
			if (ListeTache.get(m) instanceof Mission){
				
				
					/* Coupe sur les arcs sortants */
					for(int n=0; n<nbTaches; n++){
						if (arc[m][n]!=null){
							for(int j=0; j<nbJourneesMax; j++){	
								if(x[m][n][j]!=null){
									couverture[m].addTerm(x[m][n][j],1);									
								}
							} //Fin 2	
						}//Fin if
					} //Fin 1
					
					
					/*On force la conduite des trains commerciaux, pas des W*/
						if(!ListeTache.get(m).id.equals("maux")){					
							Cplex.addEq(couverture[m], 1, "Couv-eq" + ListeTache.get(m).getId());
						}
				
			}//Fin if Mission 
			
			
		}//Fin for
		return couverture; 
		
	
	}

	/**
	 * 
	 * Cr�e la contrainte d'amplitude de la journ�e
	 * @throws IloException 
	 */
	//TODO ajouter le tableau des trajets en taxi 
	public IloLinearNumExpr[] contraintesAmplitude() throws IloException{
		IloLinearNumExpr[] duree=new IloLinearNumExpr[nbJourneesMax];
		
		long amax= amplitudeMax - (2*Tache.getPsfs()); 
		for(int j=0; j<nbJourneesMax; j++){
			duree[j] =Cplex.linearNumExpr() ;

			
			for(int m=0 ; m<nbTaches; m++){
				for(int n=0; n<nbTaches; n++){
					
					if(x[m][n][j]!=null){
						duree[j].addTerm(  arc[m][n].duree(), x[m][n][j]);

					} 
					if(p[m][n][j]!=null){
						duree[j].addTerm( arc[m][n].duree(), p[m][n][j]);

					}
					
					/* Dur�e arbitraire: taxi = 20min*/
					if(v[m][n][j]!=null){
						duree[j].addTerm(1200, v[m][n][j]);
					}
					
				}//Fin 3
			}//Fin 2

					
				Cplex.addLe(duree[j], amax);
			

		}//Fin 1
		return duree; 
	}
	
	
	/**
	 * contrainte sur l'amplitude de la journ�e si 1h30 pendant la nuit de 23h � 6h
	 * l'amplitude est 8h
	 * amplitude de 8h si 1h30 entre 23h et 6h
	 */
	
	public IloLinearNumExpr[] contraintesAmplitudeNuit() throws IloException{
		IloLinearNumExpr[] dureeNuit=new IloLinearNumExpr[nbJourneesMax];
		long amaxsiNuit=28800;
		
		for(int j=0; j<nbJourneesMax; j++){
			dureeNuit[j] =Cplex.linearNumExpr() ;
			
		for(int m=0 ; m<nbTaches; m++){
			for(int n=0; n<nbTaches; n++){
				
				if(x[m][n][j]!=null && arc[m][n].DureeNuit() >= 5400){
					
				    dureeNuit[j].addTerm(arc[m][n].duree(), x[m][n][j]);					

				} 
				if(p[m][n][j]!=null && arc[m][n].DureeNuit() >= 5400){
					dureeNuit[j].addTerm( arc[m][n].duree(), p[m][n][j]);

				}
				
				/* Dur�e arbitraire: taxi = 20min*/
				if(v[m][n][j]!=null && arc[m][n].DureeNuit() >= 5400){
					dureeNuit[j].addTerm(1200, v[m][n][j]);
				}
				
			}//Fin 3
		}//Fin 2

				// ici: plus petit que 8h
			Cplex.addLe(dureeNuit[j], amaxsiNuit );
		

	}//Fin 1
	return dureeNuit; 
		
	}



	/**
	 * Contrainte sur la moyenne des journ�es non vides
	 * @throws IloException
	 */
	public void contraintesAmplitudeMoyenne(IloLinearNumExpr[] duree)throws IloException{
		IloIntVar[] jNonVide  = new IloIntVar[nbJourneesMax] ; 
		IloLinearIntExpr [] sumX = new IloLinearIntExpr[nbJourneesMax]; 
	
		/*On regarde si les journ�es sont vides ou non */
		for(int j = 0 ; j <nbJourneesMax ; j++){
			sumX[j]= Cplex.linearIntExpr(); 
			jNonVide[j]= Cplex.boolVar();
			/*On somme les x[m][n][j]*/
			for (int m=0; m<nbTaches; m++){
				for(int n=0; n<nbTaches; n++){
					if(x[m][n][j]!=null){
						sumX[j].addTerm(1, x[m][n][j]);
					}
				}
			}
			/* jNonVide= x ou x ou x ... */
			Cplex.addLe(jNonVide[j], sumX[j]); // si tous les x sont nuls, alors jNonVide est nul
			Cplex.addLe( sumX[j], Cplex.prod(jNonVide[j], nbTaches));  //si l'un des x vaut 1, alors jNonVide vaut 1
		}
		
		/*On borne la dur�e moyenne des journ�es non vides*/
		IloLinearNumExpr dTotale = Cplex.linearNumExpr(); 
		IloLinearIntExpr nJ= Cplex.linearIntExpr(); 
		for (int j=0; j<nbJourneesMax ; j++){
			nJ.addTerm(1, jNonVide[j]);
			dTotale.add(duree[j]); 
		}
		
		Cplex.addLe(dTotale, Cplex.prod(nJ, amplitudeMoyenneMax)); 
	}

	/**
	 * pauses impos�es
	 * @throws IloException
	 */
	//TODO: adapater : ici pauses de 15 minutes
	public void contraintesPause(int pTot) throws IloException{
		
		IloLinearNumExpr[] pause=new IloLinearNumExpr[nbJourneesMax];
		IloLinearIntExpr [] sumX = new IloLinearIntExpr[nbJourneesMax]; 
		IloIntVar[] jNonVide  = new IloIntVar[nbJourneesMax] ; 
		for(int j=0; j<nbJourneesMax; j++){
			pause[j] =Cplex.linearNumExpr() ;
			sumX[j]= Cplex.linearIntExpr(); 
			jNonVide[j]= Cplex.boolVar(); 
			
			for(int m=0 ; m<nbTaches; m++){
				for(int n=0; n<nbTaches; n++){
					
					if(x[m][n][j]!=null){
						sumX[j].addTerm(1, x[m][n][j]);
						if(arc[m][n].coupure((long)900)){ //pauses de 15 min 
							pause[j].addTerm(  1, x[m][n][j]);							
						}
					} else if(p[m][n][j]!=null){
						if(arc[m][n].coupure((long)900)){ //pauses de 15 min 							
							pause[j].addTerm( 1, p[m][n][j]);
						}
							sumX[j].addTerm(1, p[m][n][j]);

					}
					
				}//Fin 3
			}//Fin 2
			
			Cplex.addLe(jNonVide[j], sumX[j]); // si tous les x sont nuls, alors jNonVide est nul
			Cplex.addLe( sumX[j], Cplex.prod(jNonVide[j], nbTaches));  //si l'un des x vaut 1, alors jNonVide vaut 1
			Cplex.addLe(Cplex.prod(pTot/900, jNonVide[j]), pause[j]); 
	
		}//Fin 1

		
	}


//		//TODO attention : valable si nJourneesMax>=nbTaches
//		/**
//		 * Donne une solution intitiale au solveur  : mission j attribu�e au conducteur j, avec passage par maux et retour en voiture
//		 */
//		public void init(){
//			var sol = new IloOplCplexVectors(); 
//			int t=0; 
//			boolean res=false; 
//			
//			/*On cherche une r�sidence dans la liste de taches*/
//			while(t<nbTaches && !res){
//				if (ListeTache.get(t) instanceof Residence){
//					res=true ; 
//				}else{
//					t++;
//				}
//			}		
//		
//			for (int j=1; j<nbTaches; j++){ //debug j=1 pour ne pas couvrir maux
//				
//				/* Si c'est une mission on prend l'arc vers maux : x[m][0][j]  */
//				if(ListeTache.get(j) instanceof Mission){
//					sol.setValue(x[j][0][j],1); //conducteur sur l'arc j-maux
//					sol.setValue(v[t][j][j],1); // prise de service en voiture
//					sol.setValue(v[0][t][j],1);	//retour en r�sidence en voiture
//										
//				} // fin if mission 
//				
//			}// fin for j
//		}
//
//
//
//		//TOOO : cf autre init. valable si nb max de journ�e 
//		/**
//		 * Donne une solution initiale : toutes les missions d'un m�me train par conducteur 
//		 * @param releve : tableau des booleens de rel�ve. 
//		 */
//		public void init(int[] releve){
//			LSSolution sol = ls.getSolution() ; 
//			int t=0 ; 
//			boolean res=false ; 
//			/*On cherche une r�sidence*/
//			while (t<nbTaches && !res){
//				if (ListeTache.get(t) instanceof Residence){
//					res=true ; 
//				}else{
//					t++; 
//				}
//			} 
//			
//			boolean attribue[]= new boolean[nbTaches]; //initialis� a false automatiquement
//			
//			for(int j=1 ; j<nbTaches; j++){//on commence a un car on ne couvre pas maux
//		
//				/*Si c'est une mission pas encore attribu�e*/
//				if(ListeTache.get(j) instanceof Mission && !attribue[j]){
//					int indice = j;	
//					sol.setValue(v[t][indice][j],1); //sortie de r�sidence en voiture 
//			
//					/*Si c'est une rel�ve on attribue tout le train au m�me conducteur*/
//					while(releve[indice]!=-1 && indice <nbTaches){
//						sol.setValue(x[indice][releve[indice]][j],1); //conduite	
//						attribue[indice]= true; 	
//						indice=releve[indice] ;
//					}
//					sol.setValue(x[indice][0][j],1); // conducteur sur l'arc fin de train - maux
//					sol.setValue(v[0][t][j],1);		// retour en r�sidence depuis maux
//					attribue[indice]=true ; 
//				} // fin if 
//		
//			}// fin boucle j 
//			System.out.println("Solution initialis�e"); //debug
//			
//		}// fin init
//


		/**
		 * Calcule le nombre de rel�ves
		 * @param r : tableau calcul� dans releves
		 * @return nombre de rel�ves
		 * @throws IloException 
		 */
		public IloLinearNumExpr chgt(int r[]) throws IloException{
			IloLinearNumExpr penalite = Cplex.linearNumExpr(); 
			
			for(int m=0; m<nbTaches; m++){
				/*Si il y a une rel�ve possible, on regarde si le conducteur change de train */
				if(r[m]!=-1){
					for (int n=0; n<nbTaches ; n++){
						for(int j=0 ; j<nbJourneesMax ; j++){				
							if(! ListeTache.get(n).id.equals(ListeTache.get(m).id)
									&& x[m][n][j]!=null){
								penalite.addTerm(x[m][n][j],1); 
							}
						}
					}
				}
			}
			
			return penalite ; 
		}



		/**
		 * 
		 * Calcule les arcs de rel�ve
		 * @param arc
		 * @return un tableau tab tel que si m et n sont deux missions cons�cutives sur un meme train, tab[m]=n. si pas de rel�ve, tab[m]=-1
		 */
		public int[] releves(Arc[][] arc){
			int[] r= new int [nbTaches];
			
			for (int m=0 ; m<nbTaches ; m++){ 
				int releve=-1; 
				int n=0 ; 
				while(releve==-1 && n< nbTaches){
					if(arc[m][n]!=null && ListeTache.get(m).id.equals(ListeTache.get(n).id)){
						releve=n ;
						//System.out.println("Rel�ve :" +m +"->" +n + "///" +ListeTache.get(m).toString() + "->" + ListeTache.get(n).toString()); 
					} else{					
						n++;					
					} 
				} 
				r[m]=releve;
			} 		
			return r; 
		}



	

	//TODO attention : valable si nJourneesMax>=nbTaches
	/**
	 * Donne une solution intitiale au solveur  : mission j attribu�e au conducteur j, avec passage par maux et retour en voiture
	 */
//	public void init(){
//		var sol = new IloOplCplexVectors(); 
//		int t=0; 
//		boolean res=false; 
//		
//		/*On cherche une r�sidence dans la liste de taches*/
//		while(t<nbTaches && !res){
//			if (ListeTache.get(t) instanceof Residence){
//				res=true ; 
//			}else{
//				t++;
//			}
//		}		
//	
//		for (int j=1; j<nbTaches; j++){ //debug j=1 pour ne pas couvrir maux
//			
//			/* Si c'est une mission on prend l'arc vers maux : x[m][0][j]  */
//			if(ListeTache.get(j) instanceof Mission){
//				sol.setValue(x[j][0][j],1); //conducteur sur l'arc j-maux
//				sol.setValue(v[t][j][j],1); // prise de service en voiture
//				sol.setValue(v[0][t][j],1);	//retour en r�sidence en voiture
//									
//			} // fin if mission 
//			
//		}// fin for j
//	}
//	
//	//TOOO : cf autre init. valable si nb max de journ�e 
//	/**
//	 * Donne une solution initiale : toutes les missions d'un m�me train par conducteur 
//	 * @param releve : tableau des booleens de rel�ve. 
//	 */
//	public void init(int[] releve){
//		LSSolution sol = ls.getSolution() ; 
//		int t=0 ; 
//		boolean res=false ; 
//		/*On cherche une r�sidence*/
//		while (t<nbTaches && !res){
//			if (ListeTache.get(t) instanceof Residence){
//				res=true ; 
//			}else{
//				t++; 
//			}
//		} 
//		
//		boolean attribue[]= new boolean[nbTaches]; //initialis� a false automatiquement
//		
//		for(int j=1 ; j<nbTaches; j++){//on commence a un car on ne couvre pas maux
//
//			/*Si c'est une mission pas encore attribu�e*/
//			if(ListeTache.get(j) instanceof Mission && !attribue[j]){
//				int indice = j;	
//				sol.setValue(v[t][indice][j],1); //sortie de r�sidence en voiture 
//		
//				/*Si c'est une rel�ve on attribue tout le train au m�me conducteur*/
//				while(releve[indice]!=-1 && indice <nbTaches){
//					sol.setValue(x[indice][releve[indice]][j],1); //conduite	
//					attribue[indice]= true; 	
//					indice=releve[indice] ;
//				}
//				sol.setValue(x[indice][0][j],1); // conducteur sur l'arc fin de train - maux
//				sol.setValue(v[0][t][j],1);		// retour en r�sidence depuis maux
//				attribue[indice]=true ; 
//			} // fin if 
//
//		}// fin boucle j 
//		System.out.println("Solution initialis�e"); //debug
//		
//	}// fin init
	
	
	/**
	 * Appel�e APRES la r�solution par le solveur
	 * Mise en forme des journ�es en enchainement coh�rent de missions
	 * @return renvoie res, un tableu contenant les journ�es de chaque agent
	 * @throws IloException 
	 * @throws UnknownObjectException 
	 */
	public Tache[][][] getRes() throws UnknownObjectException, IloException {
	    Tache [][][] res= new Tache[nbJourneesMax][nbTaches][3];
	     
		for(int j=0; j<nbJourneesMax; j++){	
			int c=0 ; //ligne dans le tableau
			int m=0; // indice de tache 
			int n=0; // indice de tache 
			boolean resTrouvee=false; 
			boolean journeeFinie=false ; 
			boolean nsize ; 
			boolean msize; 
			 
			/*On cherche le 1e arc de la journee (res-mission)*/
			while(!resTrouvee ){
			
	
				/*On cherche m=indice d'une r�sidence dans la liste*/				
				while(m<ListeTache.size()&& !(ListeTache.get(m) instanceof Residence)){
					m++ ;
				} //debug : en sortie m =indice de r�sidence dans la liste de tache, ou si n'existe pas, =size
				
				n=0 ; 
				nsize=(n<ListeTache.size()) ;		
				msize=(m<ListeTache.size());
					while( nsize && msize && !(ListeTache.get(n)instanceof Mission 
							       					&& arc[m][n]!=null 
							       					&&( (v[m][n][j]!=null && Cplex.getValue(v[m][n][j])!=0)
							       						||(p[m][n][j]!=null && Cplex.getValue(p[m][n][j])!=0 )
							       						||(x[m][n][j]!=null && Cplex.getValue(x[m][n][j])!=0 ))  )  ){
						
						n++;
						nsize=(n<ListeTache.size());
					}
				
					/*La journee est vide */
					if (!msize){
						resTrouvee=true; 
						journeeFinie=true;						
					}	
					
						
					/*Si on a trouv� un arc m-n pour la journee*/
					if(msize&&nsize){
						resTrouvee=true;			
					}else{
						m++;				
					}
					
				/*Sinon on continue � chercher la r�sidence*/
			}	//fin while		
			
			/*Si la journee est pas vide, alors on stocke la r�sidence de d�part */
				if(!journeeFinie){
					/*On stocke la r�sidence de d�part dans res[j][0][..]*/
					if(x[m][n][j]!=null && Cplex.getValue(x[m][n][j])==1){
						res[j][c][0]=ListeTache.get(m);
						c++;
					} else if (p[m][n][j]!=null && Cplex.getValue(p[m][n][j])==1){
						res[j][c][1]=ListeTache.get(m);
						c++;
					}else if(v[m][n][j]!=null && Cplex.getValue(v[m][n][j])==1){
						res[j][c][2]=ListeTache.get(m);
						c++;			
						
					}
				} //fin if
				
				
			/*On cherche des missions qui s'enchainent*/		
			while (!journeeFinie){
				m=n;//arc suivant 
				n=0;		
				nsize=(n<ListeTache.size());
				while(nsize && (x[m][n][j]==null || Cplex.getValue(x[m][n][j])==0) 
							&& (p[m][n][j]==null || Cplex.getValue(p[m][n][j])==0) 
							&& (v[m][n][j]==null || Cplex.getValue(v[m][n][j])==0) ){
					n++ ; 
					nsize=(n<ListeTache.size());
				
				} //debug sortie : si n=size, pas d'arc, journ�e finie. sinon arc trouv� 
				
				/*Si arc trouv� on stocke la mission */
				if(n<ListeTache.size()&& m<ListeTache.size()){	

					if(x[m][n][j]!=null &&Cplex.getValue(x[m][n][j])==1){	
						res[j][c][0]=ListeTache.get(m);
						c++;
					} else if (p[m][n][j]!=null &&Cplex.getValue(p[m][n][j])==1){
						res[j][c][1]=ListeTache.get(m);
						c++;
					} else if (v[m][n][j]!=null &&Cplex.getValue(v[m][n][j])==1){
						res[j][c][2]=ListeTache.get(m);
						c++;
						journeeFinie=true ; 
					}
				} else{ /*On n' a pas trouv� d'enchainement donc la journ�e est finie*/
					journeeFinie=true ;					
				}			
					
				/*Si l'extr�mit� de l'arc est une r�sidence */
				if (n<ListeTache.size() && ListeTache.get(n) instanceof Residence){
					res[j][c][2]=ListeTache.get(n);
					journeeFinie=true;					
				} 	
			}
			
			
	   	}//fin 1
		System.out.println("Sortie de getRes");//debug
		 return res ;
	}
	
	//Fonction de debugage
	//TODO : a enlever 
	public Arc[][][] printSol() throws UnknownObjectException, IloException{
	     Arc [][][] res= new Arc[ListeJournee.size()][ListeTache.size()][3];
		 for(int j=0; j<ListeJournee.size(); j++){	
	    	 int c = 0;
				for(int m=0; m<ListeTache.size(); m++){
					for( int n=0; n<ListeTache.size(); n++){
							
							if(x[m][n][j]!=null && Cplex.getValue(x[m][n][j])==1 ){
								res[j][c][0]=arc[m][n];
								System.out.println("j="+(j+1) +", c= "+ c + " role :0 " );//debug
								c++ ;
								
							}//aux
							if(p[m][n][j]!=null&& Cplex.getValue(p[m][n][j])==1){
								res[j][c][1]=arc[m][n];
								System.out.println("j="+(j+1) +", c= "+ c + " role: 1 " );//debug
								c++ ;								
							}//aux
							
							if(v[m][n][j]!=null&& Cplex.getValue(v[m][n][j])==1){
								res[j][c][2]=arc[m][n];
								System.out.println("j="+(j+1) +", c= "+ c + " role :2 " );//debug
								c++ ;								
							}//aux
						}// fin 3
					}// fin 2
				}//fin 1
		 return res ;
	}
	
	

	
	/**
	 * 
	 * Calcule la dur�e effective de travail 
	 * @throws IloException 
	 */
	public void dureeTravaillee() throws IloException{ //TODO POSER DES CONTRAINTES SUR LA DUREE TRAVAILLEE (v�rifier ratio temps passager) + si pas de contraintes passer en float
		IloLinearNumExpr[] duree=new IloLinearNumExpr[nbJourneesMax];
		for(int j=0; j<nbJourneesMax; j++){
			duree[j] =Cplex.linearNumExpr() ;

			for(int m=0 ; m<nbTaches; m++){
				for(int n=0; n<nbTaches; n++){
					
					if(x[m][n][j]!=null){
						duree[j].addTerm(  arc[m][n].duree(), x[m][n][j]);
					} else if(p[m][n][j]!=null){
						duree[j].addTerm(  arc[m][n].duree(), p[m][n][j]); //trajet voiture =0.5
					}
					
				}//Fin 3
			}//Fin 2
		}//Fin 3
	}
	
	//Fonction de debugage : print solution finale et arc cr��s
	//TODO : a enlever
	public void printGeneration() throws UnknownObjectException, IloException{		
		//System.out.println("Arc cr��s : ");  //debug
		for (int n=0; n<nbTaches; n++){
			for(int m=0; m<nbTaches; m++){
				if (arc[n][m]!=null){
					//System.out.print(arc[n][m].getM1().id+" " + arc[n][m].getM2().id); //debug
					
					for (int j=0; j<nbJourneesMax; j++){
						//if (x[n][m][j] != null){System.out.print("  x." + j); }
						//if (p[n][m][j] != null){System.out.print("  p." + j); }
						//if (v[n][m][j] != null){System.out.print("  v." + j); }
					}
					//System.out.println(); 
				}//Fin if
			}//Fin 2
		}//Fin 1
		
		
		 /*Print de la solution finale */
		System.out.println("Solution finale");
		for(int j=0; j<ListeJournee.size(); j++){	
				for(int m=0; m<ListeTache.size(); m++){
					for( int n=0; n<ListeTache.size(); n++){
				
							if(x[m][n][j]!=null && Cplex.getValue(x[m][n][j])==1 ){
								System.out.println( "agent" + j + " conducteur " + arc[m][n].getM1().id + " - " + arc[m][n].getM2().id  );							
							}//aux
							if(p[m][n][j]!=null&& Cplex.getValue(p[m][n][j])==1){
								System.out.println( "agent" + j + " passager " + arc[m][n].getM1().id + " - " + arc[m][n].getM2().id  );		
							}//aux
							if(v[m][n][j]!=null&& Cplex.getValue(v[m][n][j])==1){
								System.out.println( "agent" + j + " voiture " + arc[m][n].getM1().id + " - " + arc[m][n].getM2().id  );		
							}//aux
						}// fin 3
					}// fin 2
				}//fin 1
	}

	/**
	 * Print en console l'�tat de la solution 
	 * @throws IloException 
	 */
	public void printSolState() throws IloException{
		System.out.println("Obj final : " + Cplex.getObjValue());
		System.out.println("Obj releves: " + Cplex.getValue(releves));
		System.out.println("Obj journee: " + Cplex.getValue(objJournee));
		System.out.println("Obj transferts: " + Cplex.getValue(objTransfert));
		System.out.println("nbJourn�es: " + ListeJournee.size());
		
	}
	
//			LSSolution lssol = ls.getSolution(); 
//			LSSolutionStatus lss=lssol.getStatus();
//			System.out.println("Solution status :" +lss); 
//			
//			LSInconsistency lsi=ls.computeInconsistency();
//			int cI=lsi.getNbCauses();
//			for(int i =0; i< cI; i++){
//				//System.out.print(" Nom=" + lsi.getCause(i).getName() );
//				System.out.print(" Info=" + lsi.getCause(i).getInfo() );
//				System.out.print(lsi.getCause(i).toString() );
//				System.out.print(" Value=" + lsi.getCause(i).getValue() );
//				System.out.print(" Operand=" + lsi.getCause(i).getOperand(1) );				
//				System.out.println();
//			}
//	
//			System.out.println("Obj final : " + Cplex.getObjValue(0));
//	
//			
//		}

	public void printTab() throws IloException{
		double[] duree = new double[nbJourneesMax] ; 
		double aTot = 0 ; 
		
		for(int j=0; j<nbJourneesMax; j++){				
			for(int m=0 ; m<nbTaches; m++){
				for(int n=0; n<nbTaches; n++){
					
					if(x[m][n][j]!=null){
						duree[j]= duree[j]+  (arc[m][n].duree()* (long)Cplex.getValue(x[m][n][j]));
						aTot=aTot + (arc[m][n].duree()* (long)Cplex.getValue(x[m][n][j]));
			
				
					} 
					if(p[m][n][j]!=null){
						duree[j]=duree[j] + ( arc[m][n].duree() *(long)Cplex.getValue(p[m][n][j]));
						aTot=aTot + (arc[m][n].duree()* (long)Cplex.getValue(p[m][n][j]));
			
					}
					
				}//Fin 3
			}//Fin 2	
			
			System.out.println("Jour"+ j +"dure : "  + duree[j] ) ; 
			
		}//Fin 1
		System.out.println("amplitude totale : " + aTot); 
	}
	
/////////////////////////////////////////////////////////////////////////////////////////////////	
/////////////////////////////////////////////////////////////////////////////////////////////////	
 //GETTERS AND SETTERS	
	
/**
 * 
 	* Retourne le nombre total de taches a effectuer
 * 
 * @return
 */
	public  int getNbTaches() {
		return nbTaches;
	}

	/**
	 * 
	 * Retourne le nombre total de journ�es
	 * 
	 * @return
	 */
	public  int getNbJourneesMax() {
		return nbJourneesMax;
	}

	/**
	 * 
	 * Retourne la liste des journ�es
	 * 
	 * @return
	 */
	public Vector<Journee> getListeJournee() {
		return ListeJournee;
	}

	/**
	 * 
	 * Met � jour la liste des journ�es
	 * 
	 * @param listeJournee
	 */
	public void setListeJournee(Vector<Journee> listeJournee) {
		ListeJournee = listeJournee;
	}

	/**
	 * 
	 * Retourne la liste des taches a effectuer
	 * 
	 * @return
	 */
	public Vector<Tache> getListeTache() {
		return ListeTache;
	}

	/**
	 * 
	 * Met � jour la liste des t�ches 
	 * 
	 * @param listeTache
	 */
	public void setListeTache(Vector<Tache> listeTache) {
		ListeTache = listeTache;
	}

	public IloIntVar[][][] getX() {
		return x;
	}

	public void setX(IloIntVar[][][] x) {
		this.x = x;
	}

	public Arc[][] getArc() {
		return arc;
	}

	public void setArc(Arc[][] arc) {
		this.arc = arc;
	}

	public int getAmplitudeMax() {
		return amplitudeMax;
	}

	public int getAmplitudeNuit() {
		return amplitudeNuit;
	}

	public int getAmplitudeCoupure() {
		return amplitudeCoupure;
	}

	public IloIntVar[][][] getP() {
		return p;
	}

	public void setP(IloIntVar[][][] p) {
		this.p = p;
	}

	

}
