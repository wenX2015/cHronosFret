package scheduling;

/**
 * Cette classe d�finit un arc
 * 
 * @author 9505389V
 *
 */
public class Arc {
	
	/* T�ches li�es par l'arc*/
	private Tache m1, m2; 
	
	/* Indique si il y a une rel�ve */
	private boolean releve;
	
	/*Poids de l'arc*/
	private boolean nuit ; 
	
	/*Dur�e minimale d'une coupure*/
	private final static int coupureMin=3600 ; //1h
	
	/* Horaires de d�but et fin de nuit*/
	private final static int debutNuit=0; //
	private final static int finNuit=18000; //5h

	
	/**
	 * 
	 * Constructeur de la classe Arc
	 * 
	 * @param m1
	 * @param m2
	 * @param releve
	 */
	public Arc(Tache m1, Tache m2, boolean releve) {
		this.m1 = m1;
		this.m2 = m2;
		this.releve = releve;
		this.nuit=false; 
		if (m1 instanceof Mission){
			Mission aux =new Mission((Mission)m1);
			this.nuit=this.nuit ||(aux.getHeureDepart()<finNuit);
		}
		if (m2 instanceof Mission){
			Mission aux =new Mission((Mission)m2);
			this.nuit=this.nuit ||(aux.getHeureDepart()>debutNuit+86400);
		}	
	}
		
	/**
	 * Constructeur par recopie
	 * 
	 * @param a
	 */
	public Arc(Arc a){
		this.m1=a.m1; 
		this.m2=a.m2; 
		this.releve=a.releve; 
		this.nuit=a.nuit ; 
	}
	
	
	/**
	 * Calcule la duree entre le debut de la 1e mission et le debut de la 2e
	 * 
	 * @return
	 */
	public long duree(){
		long res ; 		
		/*Si mission-mission = de d�part 1 a d�part 2 */
		if (m1 instanceof Mission && m2 instanceof Mission && !m2.id.equals("maux")){
			Mission aux1= new Mission((Mission)m1);
			Mission aux2= new Mission((Mission)m2);
			res=aux2.getHeureDepart()-aux1.getHeureDepart();
			
		} /*mission-maux : dur�e de la mission */
		else if(m1 instanceof Mission && m2.id.equals("maux")){
			Mission aux1= new Mission((Mission)m1);
			res=aux1.getHeureArrivee()-aux1.getHeureDepart() ;
		}/*mission residence*/
		else if (m1 instanceof Mission){
			Mission aux1= new Mission((Mission)m1);
			Residence aux2= new Residence((Residence)m2);
			res=aux1.getHeureArrivee()-aux1.getHeureDepart()+aux2.getDuree(); // +Residence.getPsfs(); 
		}/*residence-mi//ssion*/
		else if (m2 instanceof Mission){
			Residence aux1= new Residence((Residence)m1);
			res=aux1.getDuree(); // +Residence.getPsfs();
		} /*residence residence*/
		else{
			Residence aux1= new Residence ((Residence) m1);
			res=aux1.getDuree()+Residence.getIntervalle(); 
		}
		
		return res ;
	}
	
	/**
	 * calcule le duree pendant la nuit (0h � 6h)
	 */
	
	public long DureeNuit(){
		long dureeNuit=0;
		/*Si mission-mission = de d�part 1 a d�part 2 */
		if (m1 instanceof Mission && m2 instanceof Mission && !m2.id.equals("maux")){
			Mission aux1= new Mission((Mission)m1);
			Mission aux2= new Mission((Mission)m2);
			if(this.nuit && aux2.getHeureDepart()<=finNuit)
				dureeNuit=aux2.getHeureDepart()-aux1.getHeureDepart();
			if(this.nuit && aux2.getHeureDepart()>finNuit && aux2.getHeureDepart()<debutNuit+86400 )
				dureeNuit=finNuit-aux1.getHeureDepart();
			
		} /*mission-maux : dur�e de la mission */
		else if(m1 instanceof Mission && m2.id.equals("maux")){
			Mission aux1= new Mission((Mission)m1);
			if(this.nuit)
			{
				if(aux1.getHeureArrivee()>finNuit)
					dureeNuit=finNuit-aux1.getHeureDepart();
				else
					dureeNuit=aux1.getHeureArrivee()-aux1.getHeureDepart();
			}


		}/*mission residence*/
		else if (m1 instanceof Mission){
			Mission aux1= new Mission((Mission)m1);
			Residence aux2= new Residence((Residence)m2);
			if(this.nuit)
			{
				if(this.duree()+aux1.getHeureDepart() > finNuit)
					dureeNuit=finNuit-aux1.getHeureDepart();
				else
					dureeNuit=this.duree();
			} 
		}/*residence-mi//ssion*/
		else if (m2 instanceof Mission){
			
			Residence aux1= new Residence((Residence)m1);
			dureeNuit=aux1.getDuree(); // � modifier
			
		} /*residence residence*/
		
		else{
			Residence aux1= new Residence ((Residence) m1);
			dureeNuit=aux1.getDuree()+Residence.getIntervalle(); 
		}
			
		return dureeNuit;
	}
	
	/**
	 * Indique si une coupure est possible sur l'arc
	 * 
	 * @return
	 */	
	public boolean coupure_possible(){
		boolean res=false; 		
		
		/* Une coupure dure au moins une heure et pas de coupure de nuit*/
		
		/*si mission-mission*/  //TODO gerer changements  calendaires
		if (m1 instanceof Mission && m2 instanceof Mission){
			Mission aux1= new Mission((Mission)m1);
			Mission aux2= new Mission((Mission)m2);
			boolean nuit =(aux1.getHeureArrivee()>debutNuit)||(aux2.getHeureDepart()<finNuit);
			
			res=((!nuit) &&(aux2.getHeureDepart()-aux1.getHeureArrivee())>=coupureMin); 
		}		
		return res ; 
	}
	
	/**
	 * Indique si une coupure de i secondes est possible sur un arc
	 * @param i
	 * @return
	 */
	public boolean coupure(long i){
		boolean res=false ; 
		
		if (m1 instanceof Mission && m2 instanceof Mission){
			res=(    ((Mission)m2).getHeureDepart()  - ((Mission)m1).getHeureArrivee()    >=i   )  ;
		}
		
		return res ; 
	}
	
	/**
	 * Indique la longueur de la coupure sur un arc
	 * @return
	 */
	public long coupure(){
		long i=0 ; 
		if (m1 instanceof Mission && m2 instanceof Mission){
			i=   ((Mission)m2).getHeureDepart()  - ((Mission)m1).getHeureArrivee() ;
		}
		return i ; 
	}
	
	/**
	 * 
	 * Retourne la t�che 1
	 * 
	 * @return
	 */
	public Tache getM1() {
		return m1;
	}
	
	/**
	 * 
	 * Met � jour la t�che 1
	 * 
	 * @param m1
	 */
	public void setM1(Tache m1) {
		this.m1 = m1;
	}

	/**
	 * 
	 * Retourne la t�che 2
	 * 
	 * @return
	 */
	public Tache getM2() {
		return m2;
	}

	/**
	 * 
	 * Met � jour la t�che 2
	 * 
	 * @param m2
	 */
	public void setM2(Tache m2) {
		this.m2 = m2;
	}

	/**
	 * 
	 * Retourne un bool�en indiquant si il y a une rel�ve
	 * 
	 * @return
	 */
	public boolean isReleve() {
		return releve;
	}

	/**
	 * 
	 * Met � jour le booleen de rel�ve
	 * 
	 * @param releve
	 */
	public void setReleve(boolean releve) {
		this.releve = releve;
	}

	/**
	 * 
	 * Retourne si c'est un arc de nuit
	 * 
	 * @return
	 */
	public boolean isNuit() {
		return nuit;
	}

	/**
	 * 
	 * Met � jour le poids
	 * 
	 * @param poids
	 */
	public void setPoids(boolean nuit) {
		this.nuit = nuit;
	}

	@Override
	public String toString() {
		return "Arc [m1=" + m1 + ", m2=" + m2 + ", releve=" + releve
				+ ", nuit=" + nuit + "]";
	} 
	
	

}
