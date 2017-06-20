package Data;

/**
 * 
 * Lecture de donn�es pour Keolis 
 * 
 * @author 9505389V
 *
 */



import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


import scheduling.Mission;
import scheduling.Tache;


public class Input {

	public Input(){}; 
	
	/**
	 * Lecture des taches  //!!!! Attention pour l'instant seulement adapt�e � OWL/ENP RE82.xlsx !!!
	 * 
	 * @param workbook : classeur excel � lire
	 * @return
	 * @throws Exception
	 */
	public Vector<Tache> getTaches(XSSFWorkbook workbook, String sheet) throws Exception{
		
		XSSFSheet spreadsheet=workbook.getSheet(sheet); 
		
		Vector<Tache> listeTache = new Vector<Tache>() ;
		Vector<String> competences=new Vector<String>(); 
		boolean conducteur =true ;
		
		int colStart=2 ; int colEnd=26; //colonnes correspondat au 1e et dernier train 
		int stationStart= 6; int stationEnd=21; // lignes correspondant a 1e et derniere gare 
		int rowid= 3; //ligne o� l'on trouve les id des trains
		//int 0=0 ;  // colonne avec les noms de gares
		
		/*Mission auxiliaire fictive (non conducteur) en position 0*/
		//Mission maux= new Mission("maux", "", "", competences, 0,0, false); 
		//listeTache.add(0,maux);
		
		for (int c=colStart; c<=colEnd; c++){
					
			int l= stationStart ; 
			String id = null, origine = null, destination ; 
			double hd = 0 ; double ha=0 ;
			double h0=0 ;
											
				/*On effectue la lecture si la colonne indique bien un train et non pas un quai*/
			if( ! getCell(spreadsheet,rowid+1, c).getStringCellValue().equals("Gleis")){
				
				while(l<=stationEnd){
						
						boolean start =false;
						
						/*On cherche la gare de d�part du train*/
						while(!start && l<=stationEnd){		
							
							getCell(spreadsheet, l,c).setCellType(1);						
							if (getCell(spreadsheet, l,c).getStringCellValue().isEmpty() 
									|| getCell(spreadsheet, l,c).getStringCellValue().equals("|")){
								l++;
							}else{
								start=true ; 
								getCell(spreadsheet, rowid, c).setCellType(1);
								getCell(spreadsheet, l,0).setCellType(1);		
								getCell(spreadsheet, l,c).setCellType(1); 
								
								id = getCell(spreadsheet, rowid, c).getStringCellValue();
								origine= getCell(spreadsheet, l,0).getStringCellValue();								
								hd = Double.parseDouble(getCell(spreadsheet, l,c).getStringCellValue())*86400;
								
								if (hd<h0){hd=hd+86400;} // si on a pass� minuit depuis le dernier ha, on convertit 
								h0=hd ; // on garde l'heure du d�part dans h0
							}
						} //while start 
						
						/* On lit la suite du train */
						if(l<=stationEnd){
							
							/* On cherche la gare d'arriv�e ou 1e rel�ve*/
							getCell(spreadsheet, l, 0+1).setCellType(1); // ab ou an, direction du train 
							getCell(spreadsheet,l,c).setCellType(1);
							getCell(spreadsheet,l+1,c).setCellType(1);
							getCell(spreadsheet, l,0).setCellType(1); 

							
							while(	getCell(spreadsheet, l, 0+1).getStringCellValue().equals("ab") 
									|| getCell(spreadsheet,l,c).getStringCellValue().equals("|")									
									|| ( 
												getCell(spreadsheet, l, 0+1).getStringCellValue().equals("an")  
												&& getCell(spreadsheet,l+1,c)!=null 
												&& (! getCell(spreadsheet,l+1,c).getStringCellValue().replace(" ","").isEmpty())  
												&& (Double.parseDouble(getCell(spreadsheet,l+1,c).getStringCellValue())*86400- Double.parseDouble(getCell(spreadsheet,l,c).getStringCellValue())*86400) <240
										)
									){									
									l++ ;						
									//System.out.println("l="+l+", c="+c);		//debug
									if(l<stationEnd){
									getCell(spreadsheet,l,c).setCellType(1);		
									getCell(spreadsheet,l+1,c).setCellType(1);	
									}
									
							}													
						
							destination= getCell(spreadsheet, l,0).getStringCellValue();
							ha =Double.parseDouble(getCell(spreadsheet, l,c).getStringCellValue())*86400 ;
							
							if(ha<h0){ha=ha+86400 ;}// si on a pass� minuit depuis le d�part
							h0=ha ; //on garde l'heure d'arriv�e dans h0
							
							/*Cr�ation de la mission*/
							Mission m =new Mission(id, origine, destination, competences, (long)hd, (long)ha, conducteur);
							listeTache.add(m);
							l++;
							
								System.out.println(m.toString()); //debug 
							
						}									
				
				}// while l<=stationEnd			
				
			}// if train 			
			
		} // Boucle for c, sur tous les trains de la journ�e
		return listeTache;		
		

	}
	
	/**
	 * 
	 * Lit les temps de retournement propres � chaque gare 
	 * @param workbook
	 * @param sheet
	 * @return
	 */
	public Map<String, Integer> getRetournement(XSSFWorkbook workbook, String sheet){
		Map<String, Integer> retournement = new HashMap<String, Integer>(); 
		XSSFSheet spreadsheet=workbook.getSheet(sheet); 
		
		
		int stationStart= 0; int stationEnd=10; // lignes correspondant a 1e et derniere gare 
		//int 0=0 ;  // colonne avec les noms de gares
		int tpsStation =1 ; //colonne avec temps de retournement 
		
		/*On parcourt toutes les gares*/
		for(int l=stationStart; l<= stationEnd; l++){
			getCell(spreadsheet, l,0).setCellType(1);			
			String station = getCell(spreadsheet, l,0).getStringCellValue();
			
			getCell(spreadsheet, l,tpsStation).setCellType(1);			
			String tps = getCell(spreadsheet, l, tpsStation).getStringCellValue();
			
			retournement.put(station, Integer.parseInt(tps)); 
			
		}		
		return retournement ; 
	} 
	
	/**
	 * R�cup�re la sortie d'une pr�c�dente simulation et la passe en solution initiale
	 * @param workbook
	 * @param sheet
	 * @return
	 * @throws Exception
	 */
	public Vector<Tache> init(XSSFWorkbook workbook, String sheet) throws Exception{
		
		XSSFSheet spreadsheet=workbook.getSheet(sheet); 
		
		Vector<Tache> ListeTache = new Vector<Tache>() ;
		Vector<String> competences=new Vector<String>(); 
		
		return ListeTache ; 
	}
	
	/**
	 * Lecture d'une cellule
	 * @param ligne
	 * @param numCell
	 * @return
	 */
	public XSSFCell getCell(XSSFSheet feuille, int l, int c) {		
		XSSFRow ligne = feuille.getRow(l);
		XSSFCell Cellule = ligne.getCell(c);
		return Cellule; 
	}
	
	/**
	 * Convertit une string "hh:mm:ss" en entier (nombre de secondes)
	 * 
	 * @param s
	 * @return
	 */
	public int heureToInt(String s) {

		String b = s.substring(0, 2);

		int heure = Integer.parseInt(b,10);

		String c = s.substring(3, 5);
		int minute = Integer.parseInt(c,10);

		String d= s.substring(6,8);
		int seconde= Integer.parseInt(d,10);

		int heureTotal = heure * 3600 + minute*60 +seconde;

		return heureTotal;
	}
	

}
