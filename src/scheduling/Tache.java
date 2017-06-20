package scheduling;
import java.util.Map;
import java.util.Vector;

/**
 * 
 * Cette classe abstraite définit une tâche
 * 
 */
public abstract class Tache {

	/* Nom de la tâche */
	protected String id ; 	

	/* compétences nécessaires pour effectuer la tâche */
	protected Vector<String> competences ;
	
	/* Temps d'attente max entre deux missions*/
	private static final long attenteMax=20000; //<3h
	
	/*Temps minimum entre deux taches*/
	private static final long intervalle=120; //2 min pour chgt de train sans retournement
	
	private static final long PSFS=600 ; // prise de service fin de service
	




	
	/**
	 * Teste la compatibilité d'une tache avec une mission 
	 * @param m
	 * @param retournement
	 * @return
	 */
	abstract boolean compatibilite(Mission m, Map<String, Integer> retournement);
	
	/**
	 * 
	 * Teste la compatibilité d'une tache avec une résidence
	 * 
	 * @param r
	 * @return
	 */
	abstract boolean compatibilite(Residence r);
	
	
	
	/**
	 * 
	 * Retourne l'id de la tâche
	 * 
	 * @return
	 */
	public String getId() {
		return id;
	}

	/**
	 * 
	 * Met à jour l'id de la tâche
	 * 
	 * @param id
	 */
	public void setId(String id) {
		this.id = id;
	}

	
	/**
	 * 
	 * Retourne la liste de compétences nécessaires pour effectuer la tâche
	 * 
	 * @return
	 */

	public Vector<String> getCompetences() {
		return competences;
	}


	/**
	 * 
	 * Met à jour les compétences nécessaires pour effectuer la tâche
	 * 
	 * @param competence
	 */
	public void setCompetences(Vector<String> competences) {
		this.competences = competences;
	}
	
	
	/**
	 * Retourne la constante indiquant l'attente maximale autorisée entre deux missions
	 * 
	 * @return
	 */
	public static long getAttenteMax() {
		return attenteMax;
	}


	/**
	 * 
	 * Retourne l'intervalle de temps minimum entre deux taches
	 * 
	 * @return
	 */
	public static long getIntervalle() {
		return intervalle;
	}

	
	
	public static long getAttentemax() {
		return attenteMax;
	}

	public static long getPsfs() {
		return PSFS;
	}
	
}
