package Data;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import scheduling.Mission;
import scheduling.Tache;

public class InputGen {

	//XSSFWorkbook workbook;
	//XSSFSheet spreadsheet;
	//String sheet;
	int colStart ; 	int colEnd; //colonnes correspondat au 1e et dernier train 
	int stationStart;	int stationEnd;// lignes correspondant a 1e et derniere gare 
	int rowid; //ligne où l'on trouve les id des trains
	int colStation; // colonne avec les noms de gares
	
	
	public InputGen(	//XSSFWorkbook workbook,  XSSFSheet spreadsheet, String sheet,
						int colStart , int colEnd, int stationStart, int stationEnd, int rowid, int colStation){
		
		
		//this.workbook = workbook;
		//this.spreadsheet =spreadsheet;
		//this.sheet=sheet;
		this.colStart=colStart ; 	
		this.colEnd=colEnd; 
		this.stationStart=stationStart;	
		this.stationEnd=stationEnd;
		this.rowid=rowid; 
		this.colStation=colStation; 
		
	}; 
	
	/**
	 *
	 * 
	 * @param workbook : classeur excel à lire
	 * @return
	 * @throws Exception
	 */
	public Vector<Tache> getTaches(XSSFWorkbook workbook, String sheet) throws Exception{
		
		XSSFSheet spreadsheet=workbook.getSheet(sheet); 
		
		Vector<Tache> listeTache = new Vector<Tache>() ;
		Vector<String> competences=new Vector<String>(); 
		boolean conducteur =true ;		
		
		/*Mission auxiliaire fictive (non conducteur) en position 0*/
		Mission maux= new Mission("maux", "-", "-", competences, 0,0, false); 
		listeTache.add(0,maux);
		
		for (int c=colStart; c<=colEnd; c++){
					
			int l= stationStart ; 
			String id = null, origine = null, destination ; 
			double hd = 0 ; double ha=0 ;
			double h0=0 ;
											
				/*On effectue la lecture si la colonne indique bien un train et non pas un quai*/
			getCell(spreadsheet, rowid,c).setCellType(1);	
			if( ! getCell(spreadsheet,rowid, c).getStringCellValue().equals("Gleis")){
				
				while(l<=stationEnd){
						
						boolean start =false;
						
						/*On cherche la gare de départ du train*/
						while(!start && l<=stationEnd && getCell(spreadsheet, l,c)!=null){		
							
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
								
								if (hd<h0){hd=hd+86400;} // si on a passé minuit depuis le dernier ha, on convertit 
								h0=hd ; // on garde l'heure du départ dans h0
							}
						} //while start 
						
						/* On lit la suite du train */
						if(l<=stationEnd){
							
							/* On cherche la gare d'arrivée ou 1e relève*/
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
									if(getCell(spreadsheet,l+1,c)!=null){
									getCell(spreadsheet,l+1,c).setCellType(1);	}
									}
									
							}							
							//System.out.println((l+1)+getNumCellule(c)); //debug
							
							destination= getCell(spreadsheet, l,0).getStringCellValue();
							ha =Double.parseDouble(getCell(spreadsheet, l,c).getStringCellValue())*86400 ;
							
							if(ha<h0){ha=ha+86400 ;}// si on a passé minuit depuis le départ
							h0=ha ; //on garde l'heure d'arrivée dans h0
							
							/*Création de la mission*/
	
							Mission m =new Mission(id, origine, destination, competences, (long)hd, (long)ha, conducteur);
							listeTache.add(m);
							l++;
							
								System.out.println(m.toString()); //debug 
							
						}									
				
				}// while l<=stationEnd			
				
			}// if train 			
			
		} // Boucle for c, sur tous les trains de la journée
		return listeTache;		
		

	}
	
	
	protected String getNumCellule(int col)
	{
		char letters[] = {'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};
		String column = "";
		int rest;
		
		while(col > 0)
		{
			rest = (col-1) % 26;
			column = letters[rest] + column;
			col = (col-1) / 26;
		}
		return column;
	}
	
	
	
	
	/**
	 * 
	 * Lit les temps de retournement propres à chaque gare 
	 * @param workbook
	 * @param sheet
	 * @return
	 */
	public Map<String, Integer> getRetournement(XSSFWorkbook workbook, String sheet){
		Map<String, Integer> retournement = new HashMap<String, Integer>(); 
		XSSFSheet spreadsheet=workbook.getSheet(sheet); 
		
		
		int stationStart= 0; int stationEnd=10; // lignes correspondant a 1e et derniere gare 
		int colStation=0 ;  // colonne avec les noms de gares
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
	 * Récupère les correspondances  : gare - acronyme
	 * @param workbook
	 * @param sheet
	 * @return la map 
	 */
	public Map<String, String> getAcronyms(XSSFWorkbook workbook, String sheet){
		Map<String, String> acronyms = new HashMap<String, String>(); 
		XSSFSheet spreadsheet=workbook.getSheet(sheet); 
		
		int last = spreadsheet.getLastRowNum() ; 
		
		for(int l=0; l<=last; l++){
	
			spreadsheet.getRow(l).getCell(0).setCellType(1);			
			String station = spreadsheet.getRow(l).getCell(0).getStringCellValue();
			
			spreadsheet.getRow(l).getCell(1).setCellType(1);			
			String acronym = spreadsheet.getRow(l).getCell(1).getStringCellValue();
			
			
			acronyms.put(station, acronym); 
		}
					
		return acronyms ; 
	}
	/**
	 * Récupère la sortie d'une précédente simulation et la passe en solution initiale
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
		if (ligne==null){
			return null ;
		} else {
		XSSFCell Cellule = ligne.getCell(c);
	 //System.out.println(Cellule.toString()); //debug 
		return Cellule;
		}
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
