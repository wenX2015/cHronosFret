package Data;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;


import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import scheduling.Journee;
import scheduling.Mission;
import scheduling.Residence;
import scheduling.Tache;

public class Output {
	
	String path ; 	
	Vector<Tache> ListeTache ; 
	Vector<Journee> ListeJournee ; 
	String date ; 
	Map<String, String> acronyms ;

 
	/**
	 * Constructeur de classe
	 * @param path
	 * @param ListeTache
	 * @param ListeJournee
	 * @param date
	 * @throws IOException
	 */
	public Output(String path, Vector<Tache> ListeTache, Vector<Journee> ListeJournee, String date,Map<String, String> acronyms ) throws IOException {
		this.ListeTache=ListeTache ; 
		this.ListeJournee=ListeJournee;
		this.path = path;
		this.date=date ; 
		this.acronyms=acronyms ; 

		/* On ouvre le fichier sur lequel où l'on va écrire */
		FileOutputStream out = new FileOutputStream(this.path);

		/* On crée un environnementde travail excel */
		XSSFWorkbook wb = new XSSFWorkbook();

		wb.write(out);
		wb.close();
		out.close();
	}


	/**
	 * Mise en forme de la feuille de résultats 
	 * @param nbJourneesMax
	 * @param nbTaches
	 * @param res
	 * @throws Exception
	 */
	public void ecritureResultats( int nbTaches, Tache[][][] res) throws Exception
	{
		/* On ouvre le fichier sur lequel où l'on va écrire */
		FileInputStream in = new FileInputStream(this.path);
		
		/* On récupère le classeur de travail excel */
		XSSFWorkbook wb = new XSSFWorkbook(in);
		
		/* Création d'une feuille */
		Sheet mySheet = wb.createSheet("Results");
		CellStyle style= wb.createCellStyle();
		style.setBottomBorderColor(IndexedColors.GREEN.getIndex());
		
		XSSFSheet serviceSheet=wb.createSheet("Services");
		
		Row r0 = mySheet.createRow(0);
		r0.createCell(0).setCellValue("Shift");
		r0.createCell(1).setCellValue("Mission") ;
		r0.createCell(2).setCellValue("Start") ;
		r0.createCell(3).setCellValue("Arrival") ;
		r0.createCell(4).setCellValue("Starting time") ;
		r0.createCell(5).setCellValue("Arrival time") ;	  
		r0.createCell(6).setCellValue("Role") ;
		//r0.createCell(7).setCellValue("Compétences") ;
		
		int line=1; //ligne dans laquelle écrire
		int nJ ; 
		nJ=ecritureTableauRes( mySheet, serviceSheet, res, line, style);
		System.out.println("Nombre de journées : " + nJ);
		
		/*Fermeture du classeur et fichier*/
		in.close();
		FileOutputStream out = new FileOutputStream(this.path);
		wb.write(out);
		wb.close();
		out.close();	  
	} // Fin ecritureResultats

	/**
	 * Ecriture du tableau res dans une feuille excel
	 * @param nJ
	 * @param mySheet
	 * @param res
	 * @param line
	 */
	public int ecritureTableauRes(Sheet mySheet, XSSFSheet serviceSheet, Tache[][][] res, int line, CellStyle style){
		int currentRow=line; //ligne d'écriture 
		int nJf=0; //compteur du nombre de journées créées
		

		int lService=0; 
		
		for (int nJ=0; nJ< ListeJournee.size() ; nJ++){			
			int role=0; 
			int c=0 ; // ligne du tableau res
		
			if(res[nJ][c][0]!=null || res[nJ][c][1]!=null ||res[nJ][c][2]!=null)
			{
				XSSFRow row = serviceSheet.createRow(lService+1);
				row.createCell(0).setCellValue(lService);//id service
				lService++;
				
				//first service
				for(int i=0; i < res[nJ].length; i++)
				{
					if (res[nJ][i][0]!=null){role=0;} else if (res[nJ][i][1]!=null) {role=1;} else if(res[nJ][i][2]!=null) {role=2;}
					
					if (!( res[nJ][i][role] instanceof Residence)) // si 2 est pour taxi
					{
						row.createCell(1).setCellValue( (double)(((Mission)res[nJ][i][role]).getHeureDepart() %86400) / 86400);
						break;
					}
				}
				
				
				//last service
				for(int i=res[nJ].length-1; i >=0 ; i--)
				{
					role = -1;
					if (res[nJ][i][0]!=null){role=0;} else if (res[nJ][i][1]!=null) {role=1;} else if(res[nJ][i][2]!=null) {role=2;}
					
					if (role >= 0  && !( res[nJ][i][role] instanceof Residence) && !((Mission)res[nJ][i][role]).getId().equals("maux") ) // si 2 est pour taxi
					{
						row.createCell(2).setCellValue( (double)(((Mission)res[nJ][i][role]).getHeureArrivee()%86400) / 86400);
						break;
					}
				}
				
				if(date.equals("week")) {
					row.createCell(3).setCellValue("0,1,2,3,4");
				} else if(date.equals("saturday")){
					row.createCell(3).setCellValue("5");
				}else {
					row.createCell(3).setCellValue("6");
				}
			}
			
			
		
			/*Tant qu'il reste des taches à accomplir pour la journee nJ*/
			while( res[nJ][c][0]!=null || res[nJ][c][1]!=null ||res[nJ][c][2]!=null ){	
				
				Row cR = mySheet.createRow(currentRow);
				if (res[nJ][c][0]!=null){role=0;} else if (res[nJ][c][1]!=null) {role=1;} else if(res[nJ][c][2]!=null) {role=2;} else {role=3;}
		
				
				/*Prise de service*/
				if(res[nJ][c][role] instanceof Residence && c==0){
					nJf++;// compteur du nombre de journées générées 
					cR.createCell(0).setCellValue(nJf) ;	
					cR.createCell(1).setCellValue("PS") ;	
			    	cR.createCell(2).setCellValue(getAcro(((Residence)res[nJ][c][role]).getLieu())) ;			
			    				    	/*ServiceSheet*/
//			    	Row cS=serviceSheet.createRow(lService); 
//			    	cS.createCell(0).setCellValue(aux) ;	
//			    	cS.createCell(3).setCellValue(((Residence)res[nJ][c][role]).getLieu());
//			    	int role2 ; 
//			    	if (res[nJ][c+1][0]!=null){role2=0;} else if (res[nJ][c+1][1]!=null) {role2=1;} else  {role2=2;}
//		    		cS.createCell(1)setCellValue(  ((Mission)res[nJ][c][role]).getHeureDepart() )  ;
//			    	
			    	
			    	if (role==2){			    		
			    		/*On regarde si il y a un trajet en taxi */
			    		int role2 ; 
			    		if (res[nJ][c+1][0]!=null){role2=0;} else if (res[nJ][c+1][1]!=null) {role2=1;} else {role2=2;} 
			    		boolean taxi = ! ((Residence)res[nJ][c][role]).getLieu().equals(((Mission)res[nJ][c+1][role2]).getOrigine());
			    		
			    		/*Ecriture de la ligne de transfert*/
			    		if (taxi){
				    		currentRow++;
				    		 
				    		cR = mySheet.createRow(currentRow);
				    		cR.createCell(0).setCellValue(nJf) ;	
				    		cR.createCell(1).setCellValue("Taxi") ;	
				    	   	cR.createCell(2).setCellValue(getAcro(((Residence)res[nJ][c][role]).getLieu())) ;				    		
				    		cR.createCell(3).setCellValue(getAcro(((Mission)res[nJ][c+1][role2]).getOrigine()));				    		
				    		cR.createCell(6).setCellValue("Taxi") ;
			    		}
			    	 } // fin if taxi
					
				/*Fin de service*/
				} else if(res[nJ][c][role] instanceof Residence){ 
				
					cR.createCell(0).setCellValue(nJf) ;	
					cR.createCell(1).setCellValue("FS") ;	
			    	cR.createCell(2).setCellValue(getAcro(((Residence)res[nJ][c][role]).getLieu())) ;
			    	
			    /*Mission*/	
				}else{
					
					cR.createCell(0).setCellValue(nJf) ;	
					cR.createCell(1).setCellValue(getAcro(((Mission)res[nJ][c][role]).getId())) ;
			    	cR.createCell(2).setCellValue(getAcro(((Mission)res[nJ][c][role]).getOrigine())) ;
			    	cR.createCell(3).setCellValue(getAcro(((Mission)res[nJ][c][role]).getDestination())) ;
			    	cR.createCell(4).setCellValue( ((double) ((Mission)res[nJ][c][role]).getHeureDepart()%86400)/86400 )  ;
			    	cR.createCell(5).setCellValue(    ((double)((Mission)res[nJ][c][role]).getHeureArrivee() %86400) /86400) ; 
			    	/*Pour avoir les heures écrites en format hh:mm:ss*/
			    	//cR.createCell(4).setCellValue(  intToHeure(((Mission)res[nJ][c][role]).getHeureDepart()/86400 ) ) ;
			    	//cR.createCell(5).setCellValue(    intToHeure(((Mission)res[nJ][c][role]).getHeureArrivee()/86400 	
			    	if(role==0){
			    		cR.createCell(6).setCellValue("Driver") ;
			    	} else if(role==1){
			    		cR.createCell(6).setCellValue("Passenger") ;
			    	} else if(role==2){
			    		cR.createCell(1).setCellValue("Taxi") ;		    
			    		cR.createCell(6).setCellValue("Taxi") ;
				    	cR.createCell(4).setCellValue("") ;
				    	cR.createCell(5).setCellValue("") ; 
				    	
			    		int role2;
			    		if (res[nJ][c+1][0]!=null){role2=0;} else if (res[nJ][c+1][1]!=null) {role2=1;} else  {role2=2;}
			    		boolean taxi = ! ((Mission)res[nJ][c][role]).getDestination().equals(((Residence)res[nJ][c+1][role2]).getLieu());
			    		if (taxi){
			    			cR.createCell(3).setCellValue(getAcro(((Residence)res[nJ][c+1][role2]).getLieu())) ;
			    			Row cRprevious = mySheet.getRow(currentRow-1);
			    			cR.createCell(2).setCellValue(cRprevious.getCell(3).getStringCellValue()); 
			    		}
			    	}
			    	
	
				}//fin if ps fs mission
			currentRow++;
			c++;
			}//fin while
		
		} // fin For
		return nJf ; 
	}//fin ecritutre Res

	
	public String getAcro(String s){
		
		if (acronyms.containsKey(s)){
			return acronyms.get(s);
			
		}else{
			return s ; 
		}
	}
	
	public String intToHeure(long i){		
		boolean bH, mH, sH ; 
		long h = i/3600 ; 
		if (h>24){h=h-24;}		
		long m = (i%3600)/60 ;	
		long s= (i%3600)%60 ;
		String res = new String(); 
		if(h>=10){res = res+h ; }else{res= res +"0"+h ;}
		if(m>=10){res=res+":" +m; } else{res=res+":0"+m;}
		if(s>=10){res=res+":"+s ;}else{res=res+":0"+s;}
		
		
		
		return res ; 
	}
	
	
	
	
}
