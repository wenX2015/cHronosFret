package scheduling;

import java.util.Map;
import java.util.Vector;

/**
 * 
 * Cette classe d�finit les t�ches non-horairis�es
 * 
 */
public class Residence extends Tache {

	/* Indique la dur�e de la t�che */
	private long duree ; 
	private String lieu ; 
	
	/**
	 * 
	 * Constructeur de la classe Residence
	 * 
	 * @param id
	 * @param lieu
	 * @param competences
	 * @param duree
	 */
	public Residence(String id, Vector<String> competences,
					long duree, String lieu) {
		this.id= id; 
		this.competences= competences ; 
		this.duree=duree ;
		this.lieu=lieu; 
	} 
	
	/**
	 * 
	 * Constructeur par recopie
	 * 
	 * @param r
	 * 
	 */
	
	public Residence(Residence r){
		this.id= r.id; 
		this.lieu=r.lieu;
		this.competences= r.competences ; 
		this.duree=r.duree ;		
	}


	/**
	 * Teste la compatibilit� entre deux residences
	 * 
	 * @param r
	 * @return un booleen de compatibilit�
	 */
	public boolean compatibilite(Residence r){
		boolean compatible=false ; 
		if (this.lieu.equals(r.lieu)){
			compatible=true ;
		}
		return compatible ; 
	}
	
	public boolean compatibilite(Mission m, Map<String, Integer> retournement){
		boolean compatible= false ; 
		if (this.lieu.equals(m.getOrigine())){
			compatible=true ; 
		}
		return compatible ; 
	}
	
	
	/**
	 * 
	 * Retourne la dur�e de la t�che
	 * 
	 * @return
	 */
	public long getDuree() {
		return duree;
	}

	/**
	 * 
	 * Met � jour la dur�e de la t�che
	 * 
	 * @param duree
	 */
	public void setDuree(long duree) {
		this.duree = duree;
	}

	/**
	 * 
	 * Retourne le lieu de la r�sidence
	 * 
	 * @return
	 */
	public String getLieu() {
		return lieu;
	}

	/**
	 * 
	 * Met � jour le lieu de la r�sidence
	 * 
	 * @param lieu
	 */
	public void setLieu(String lieu) {
		this.lieu = lieu;
	}

	@Override
	public String toString() {
		return "Residence [id =" + id +" ,duree=" + duree + ", lieu=" + lieu +  ", competences=" + competences + "]";
	}

	
	
	
	
}
