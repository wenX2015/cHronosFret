package scheduling;

import java.util.Vector;

/**
 * Cette classe définit une journée de service pour un agent
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
	 * Teste la compatibilité de compétences avec une mission
	 * 
	 * @param t
	 * 
	 * @return un booléen indiquant la compatibilité entre la mission et la journée
	 */
	public boolean compatibilite_competence(Tache t) {

		boolean res = false;
		int tmp = 0;
		/* On itère sur le nombre de compétences requises */
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
	 * Vérifie la compatibilité d'une journée avec une résidence 
	 * @param r
	 * @param resAssignee
	 * @return
	 */
	public boolean compatibiliteResidence(Residence r){
		boolean c=(r.getLieu()== this.getResidence());
		//System.out.println("Compatibilité J-R = "+ r.getLieu() + this.getResidence() + "::" +c);//debug
		return c;
		
	}
	
	/**
	 * Retourne la liste de compétences
	 * @return
	 */
	public Vector<String> getCompetences() {
		return competences;
	}


	/**
	 * 
	 * Met à jour les compétences de la journée
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
	 * Met à jour les compétences
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
