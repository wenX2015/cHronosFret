package scheduling;

import java.util.Map;
import java.util.Vector;

/**
 * 
 * Cette classe définit les tâches horairisées
 * 
 */

public class Mission extends Tache {

	/* heures de départ et d'arrivée */
	private long heureDepart, heureArrivee; 
	
	/*lieux de départ et d'arrivée */
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
	 * Teste la compatibilité horaire/geographique entre deux missions
	 * 
	 * @param m deuxieme mission
	 * 
	 * @return un booleen indiquant si on peut enchainer la mission m après mission this
	 */
	@Override
	public boolean compatibilite(Mission m, Map<String, Integer> retournement){
		boolean compatible =false;		
		long intervalleR=00 ; //par défaut TODO mettre à 0  quand on aura les vraies données

		if(!(m.id.equals("maux") || this.id.equals("maux"))){
	
			/* Si la parité n'est pas la même : besoin de retourner le train */
			if(Integer.parseInt(this.id)%2 != Integer.parseInt(m.id)%2){
				if(retournement.containsKey(this.destination)){			
					intervalleR = (Integer)retournement.get(this.destination);
				}
			} /*Si la parité est la même ET on change de train */
			else if(Integer.parseInt(this.id) != Integer.parseInt(m.id)){
				intervalleR=Mission.getIntervalle(); 
			}else { /*On continue de conduire le même train */
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
	 * Teste la compatibilité géographique entre une mission et une résidence
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
	 * Retourne true si l'id, l'origine et la destination passés en paramètre sont les mêmes que ceux de la mission this
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
	 * Retourne l'heure de départ de la tâche
	 * 
	 * @return
	 */
	public long getHeureDepart() {
		return heureDepart;
	}


	/**
	 * 
	 * Met à jour l'heure de départ de la tâche
	 * 
	 * @param heureDepart
	 */
	public void setHeureDepart(long heureDepart) {
		this.heureDepart = heureDepart;
	}

	/**
	 * 
	 * Retourne l'heure d'arrivée de la tâche
	 * 
	 * @return
	 */
	public long getHeureArrivee() {
		return heureArrivee;
	}


	/**
	 * 
	 * Met à jour l'heure d'arrivée de la tâche
	 * 
	 * @param heureArrivee
	 */
	public void setHeureArrivee(long heureArrivee) {
		this.heureArrivee = heureArrivee;
	}

	/**
	 * 
	 * Indique si la tâche est effectuée en tant que passager ou conducteur
	 * 
	 * @return
	 */
	public boolean isConducteur() {
		return conducteur;
	}

	/**
	 * 
	 * Met à jour le booleen conducteur
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
	 * Met à jour l'origine de la mission
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
	 * Met à jour la destination de la mission
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
