package scheduling;

import java.util.Vector;

/**
 * Cette classe d�finit une journ�e de service pour un agent
 * 
 * @author 9505389V
 *
 */
public class Journee {

	private Vector<String> competences ; 
	
	private String residence;
	
	/**
	 * 
	 * Constructeur de la classe Journee
	 * 
	 * @param competences
	 * @param residence
	 */
	public Journee(Vector<String> competences, String residence) {
		this.competences = competences;
		this.residence = residence;
	}

	
	
	/**
	 * 
	 * Teste la compatibilit� de comp�tences avec une mission
	 * 
	 * @param t
	 * 
	 * @return un bool�en indiquant la compatibilit� entre la mission et la journ�e
	 */
	public boolean compatibilite_competence(Tache t) {

		boolean res = false;
		int tmp = 0;
		/* On it�re sur le nombre de comp�tences requises */
		for (int i = 0; i < t.competences.size(); i++) {
			if (this.competences.contains(t.competences.get(i))) {
				tmp++;
			} // End if
		} // End for

	
		if (tmp == t.competences.size()) {
			res = true;
		}	
		
		return res;

	}

	/**
	 * 
	 * V�rifie la compatibilit� d'une journ�e avec une r�sidence 
	 * @param r
	 * @param resAssignee
	 * @return
	 */
	public boolean compatibiliteResidence(Residence r){
		boolean c=(r.getLieu()== this.getResidence());
		//System.out.println("Compatibilit� J-R = "+ r.getLieu() + this.getResidence() + "::" +c);//debug
		return c;
		
	}
	
	/**
	 * Retourne la liste de comp�tences
	 * @return
	 */
	public Vector<String> getCompetences() {
		return competences;
	}


	/**
	 * 
	 * Met � jour les comp�tences de la journ�e
	 * @param competences
	 */
	public void setCompetences(Vector<String> competences) {
		this.competences = competences;
	}


	/**
	 * 
	 * Renvoie la residence
	 * @return
	 */
	public String getResidence() {
		return residence;
	}


	/**
	 * 
	 * Met � jour les comp�tences
	 * @param residence
	 */
	public void setResidence(String residence) {
		this.residence = residence;
	}



	@Override
	public String toString() {
		return "Journee [competences=" + competences + ", residence="
				+ residence + "]";
	}
	
	

}
