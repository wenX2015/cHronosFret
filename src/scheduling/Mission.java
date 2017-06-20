package scheduling;

import java.util.Map;
import java.util.Vector;

/**
 * 
 * Cette classe d�finit les t�ches horairis�es
 * 
 */

public class Mission extends Tache {

	/* heures de d�part et d'arriv�e */
	private long heureDepart, heureArrivee; 
	
	/*lieux de d�part et d'arriv�e */
	private String origine, destination ;
	
	/* Indique si le trajet est fait en tant que conducteur ou passager */
	private boolean conducteur ;
	


	
	/**
	 * 
	 * Constructeur de la classe Mission
	 * 
	 * @param id
	 * @param origine
	 * @param destination
	 * @param competences
	 * @param heureDepart
	 * @param heureArrivee
	 * @param conducteur
	 */
	public Mission(String id, String origine, String destination, Vector<String> competences,
					long heureDepart, long heureArrivee, boolean conducteur) {
		this.id= id; 
		this.origine=origine;
		this.destination= destination; 
		this.competences= competences ; 
		this.heureDepart = heureDepart;
		this.heureArrivee = heureArrivee;
		this.conducteur = conducteur;
	}
	
	/**
	 * Constructeur par recopie
	 * 
	 * @param m
	 */
	
	public Mission(Mission m){
		this.id= m.id; 
		this.origine=m.origine;
		this.destination= m.destination; 
		this.competences= m.competences ; 
		this.heureDepart = m.heureDepart;
		this.heureArrivee = m.heureArrivee;
		this.conducteur = m.conducteur;
	}
	
	/**
	 * 
	 * Teste la compatibilit� horaire/geographique entre deux missions
	 * 
	 * @param m deuxieme mission
	 * 
	 * @return un booleen indiquant si on peut enchainer la mission m apr�s mission this
	 */
	@Override
	public boolean compatibilite(Mission m, Map<String, Integer> retournement){
		boolean compatible =false;		
		long intervalleR=00 ; //par d�faut TODO mettre � 0  quand on aura les vraies donn�es

		if(!(m.id.equals("maux") || this.id.equals("maux"))){
	
			/* Si la parit� n'est pas la m�me : besoin de retourner le train */
			if(Integer.parseInt(this.id)%2 != Integer.parseInt(m.id)%2){
				if(retournement.containsKey(this.destination)){			
					intervalleR = (Integer)retournement.get(this.destination);
				}
			} /*Si la parit� est la m�me ET on change de train */
			else if(Integer.parseInt(this.id) != Integer.parseInt(m.id)){
				intervalleR=Mission.getIntervalle(); 
			}else { /*On continue de conduire le m�me train */
				intervalleR= 0 ; 
			}
			
			/*Si les lieux sont compatibles, les horaires (avec retournement et atttente max)*/
			if ((this.heureArrivee + intervalleR) <= m.heureDepart &&  this.destination.equals(m.origine) &&(m.heureDepart-this.heureArrivee)<Mission.getAttenteMax()){
				compatible= true ;	
			}
		}
		return compatible;		
	}


	/**
	 * 
	 * Teste la compatibilit� g�ographique entre une mission et une r�sidence
	 * 
	 * @param r
	 * @return
	 */
	public boolean compatibilite(Residence r){
		boolean compatible=false ; 
		if (this.destination.equals(r.getLieu())){
			compatible=true; 
		}
		return compatible ; 
	}
	
	/**
	 * Retourne true si l'id, l'origine et la destination pass�s en param�tre sont les m�mes que ceux de la mission this
	 * @param id
	 * @param ori
	 * @param dest
	 * @return
	 */
	public boolean sameM(String id, String ori, String dest){
		 if (this.getId().equals(id) && this.getOrigine().equals(ori) &&this.getDestination().equals(dest)){
			 return true ; 
		 }else {
			 return false ; 
		 }
		
	}
	/**
	 * 
	 * Retourne l'heure de d�part de la t�che
	 * 
	 * @return
	 */
	public long getHeureDepart() {
		return heureDepart;
	}


	/**
	 * 
	 * Met � jour l'heure de d�part de la t�che
	 * 
	 * @param heureDepart
	 */
	public void setHeureDepart(long heureDepart) {
		this.heureDepart = heureDepart;
	}

	/**
	 * 
	 * Retourne l'heure d'arriv�e de la t�che
	 * 
	 * @return
	 */
	public long getHeureArrivee() {
		return heureArrivee;
	}


	/**
	 * 
	 * Met � jour l'heure d'arriv�e de la t�che
	 * 
	 * @param heureArrivee
	 */
	public void setHeureArrivee(long heureArrivee) {
		this.heureArrivee = heureArrivee;
	}

	/**
	 * 
	 * Indique si la t�che est effectu�e en tant que passager ou conducteur
	 * 
	 * @return
	 */
	public boolean isConducteur() {
		return conducteur;
	}

	/**
	 * 
	 * Met � jour le booleen conducteur
	 * 
	 * @param conducteur
	 */
	public void setConducteur(boolean conducteur) {
		this.conducteur = conducteur;
	}
	
	/**
	 * 
	 * Retourne l'origine de la mission
	 * 
	 * @return 
	 */
	public String getOrigine() {
		return origine;
	}

	/**
	 * Met � jour l'origine de la mission
	 * 
	 * @param origine
	 */
	public void setOrigine(String origine) {
		this.origine = origine;
	}

	
	/**
	 * 
	 * Retourne la destination de la mission 
	 * 
	 * @return 
	 */
	public String getDestination() {
		return destination;
	}

	/**
	 * 
	 * Met � jour la destination de la mission
	 * 
	 * @param destination 
	 */
	public void setDestination(String destination) {
		this.destination = destination;
	}

	
	@Override
	public String toString() {
		return "Mission [id= " + id + ",heureDepart=" + heureDepart + ", heureArrivee="
				+ heureArrivee + ", origine=" + origine + ", destination="
				+ destination + "]";
	}
	


	
	
	
	
}
