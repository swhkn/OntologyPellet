package OntologyPellet;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.vocab.PrefixOWLOntologyFormat;

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

public class tiga {
	private static final String NS1 = "http://www.semanticweb.org/riri/ontologies/2015/3/untitled-ontology-4";
	private static final String file_OWL = "D:\\COLLEGE\\TA\\EventlogsDebug\\TMax.owl";
	private static final String output_file = "D:\\COLLEGE\\TA\\Dataset\\fileO.xlsx";

	private static Integer ANOMALI = 10;
	private static Integer CASE = 100; 
	
	//Array untuk menyimpan daftar nama anomali
	private static String[] anomaliNama = {"SkipDecision", "SkipSequence", "ThroughputTimeMax", "ThroughputTimeMin", "WrongDecision",
											"WrongDutyCombine", "WrongDutyDecision", "WrongDutySequence", "WrongPattern", "WrongResource"};
	
	/*
	 * MAIN
	 */
	public static void main(String[] args) throws OWLOntologyCreationException, OWLOntologyStorageException, FileNotFoundException {
		/*
		 * Daftar Variabel
		 */
		int i=0;
		int j = 0;

		int[][] caseJumlahPelanggaran = new int[CASE][ANOMALI];
		String[][] dataCT = new String[25][4];
		
		ArrayList<String>[] listOfAnomaledCaseTT = (ArrayList<String>[]) new ArrayList[6];
		for(int x=0; x<6; x++){
			listOfAnomaledCaseTT[x] = new ArrayList<String>();
		}
		
		List<List<Integer>> duplicates = new ArrayList();
		
		ArrayList<String>[] listCaseActivityDuration = (ArrayList<String>[]) new ArrayList[3];
		for(int x=0; x<3; x++){
			listCaseActivityDuration[x] = new ArrayList<String>();
		}

		
		/*
		 * Start
		 */
		System.out.println("Start");		
		
		//create an ontology manager
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();
		
		//Lokasi File Ontology OWL
		File file1 = new File(file_OWL);
		
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(file1);
		
		System.out.println("Model " + ontology + " Loaded...");
		IRI documentIRI = manager.getOntologyDocumentIRI(ontology);
		System.out.println("  from: " + documentIRI);
		
		//Create + Load PelletReasoner
		PelletReasoner reasonerPellet = PelletReasonerFactory.getInstance().createReasoner(ontology);
		
		OWLClass Anomali = factory.getOWLClass(IRI.create(NS1 + "#Anomali"));
		OWLObjectProperty hasCase = factory.getOWLObjectProperty(IRI.create(NS1 + "#hasCase"));
		
		
		//ambil semua individu yang ada di kelas tersebut
		Set<OWLNamedIndividual> individuals = reasonerPellet.getInstances(Anomali, false).getFlattened();
				
		
		for(OWLNamedIndividual ind: individuals){
			NodeSet<OWLNamedIndividual> has_cases = reasonerPellet.getObjectPropertyValues(ind, hasCase);
			
			//panjang string individu
			Integer indLength = ind.toString().length();
			String individu = ind.toString().substring(71,(indLength-1));
			
			//Print nama individu
			System.out.println(individu);
			
			i++;
			
			int indexAnomali=0;
			
			//menyimpan index anomali yang sedang dibandingkan
			for(int a=0; a<ANOMALI; a++){
				if(individu.equals(anomaliNama[a])){
					indexAnomali = a;
					//System.out.println(anomaliNama[a] + " " + indexAnomali);
				}					
			}
			
			String event;
			int caseNo = 0;
			
			//Print anomali yang ada
			if(has_cases.isEmpty())
				System.out.println(" hasCase: tidak ada");
			else{
				for(Node<OWLNamedIndividual> has_case : has_cases){
					String teks = has_case.getRepresentativeElement().getIRI().toString();
					caseNo = Integer.parseInt(teks.substring(76,teks.indexOf("_", 76)));
					event = teks.substring(teks.indexOf("#") + 1);
					
					System.out.println(" hasCase: " + event + " " + j);
					
					if(caseNo>0)
						caseJumlahPelanggaran[caseNo-1][indexAnomali]++;
					
					//simpan sebagian ke daftar anomali case TT
					if(caseNo>0 && ( individu.equals(anomaliNama[2]) || individu.equals(anomaliNama[3]))){
						listOfAnomaledCaseTT[0].add(event);
						listOfAnomaledCaseTT[1].add(String.valueOf(caseNo));
						listOfAnomaledCaseTT[2].add(teks.substring(79));
						//listOfAnomaledCaseTT[3].add("");
						//listOfAnomaledCaseTT[4].add("");
						//listOfAnomaledCaseTT[5].add("");
					}
					j++;
					
					
					
				}
			}	
			System.out.println();
		}		
		
		//printListOfAnomaledCase(listOfAnomaledCaseTT);
		//printDataCT(dataCT);
		//printMatrixAnomaledCase(matriks);
		//saveToExcel(matriks);

		//untuk itung TT
		
		getDurationPelanggaran(factory, reasonerPellet, listOfAnomaledCaseTT);
		printListOfAnomaledCase(listOfAnomaledCaseTT);
		cekDuplikasi(listOfAnomaledCaseTT, duplicates);
		countDurationEachActivity(listOfAnomaledCaseTT, duplicates, listCaseActivityDuration);		

		
		//Cleaning memory
		caseJumlahPelanggaran = null;
		dataCT = null;
		listOfAnomaledCaseTT = null;
		duplicates = null;
		listCaseActivityDuration = null;
		
		System.out.println("Memory cleaned...");
	}
	
	/*
	 *****************************************************************************
	 *****************************************************************************
	 */
	
	/*
	 * ThroughputTime
	 */
	public static void countDurationEachActivity(ArrayList<String>[] listOfAnomaledCaseTT, List<List<Integer>> duplicates, ArrayList<String>[] listCaseActivityDuration)
	{
		System.out.println("countDurationEachActivity " + duplicates.size());
		int i=0;
		int sum2;
		while(i<duplicates.size()){
			int sum=0;
			int size=0;
			for(int j=0; j<duplicates.get(i).size(); j++){
				sum+=Integer.parseInt(listOfAnomaledCaseTT[4].get(duplicates.get(i).get(j)));
				size++;
			}
			sum2 = sum/size;
			listCaseActivityDuration[0].add(listOfAnomaledCaseTT[1].get(duplicates.get(i).get(0)));
			listCaseActivityDuration[1].add(listOfAnomaledCaseTT[3].get(duplicates.get(i).get(0)));
			listCaseActivityDuration[2].add(String.valueOf(sum2));
			i++;
		}
		System.out.println("\nNew Duration for each activity");
		for(int j=0; j<listCaseActivityDuration[0].size(); j++){
			System.out.println("case :" + listCaseActivityDuration[0].get(j));
			System.out.println(" activity: " + listCaseActivityDuration[1].get(j));
			System.out.println(" duration: " + listCaseActivityDuration[2].get(j));
		}
		System.out.println();
		/*int index=-1;
		for(int i=0 ; i<duplicates.size();i++){
			//if(listOfAnomaledCaseTT[5].size()!=0 && listOfAnomaledCaseTT[5].size() > duplicates.get(i).get(0)){
			if(listOfAnomaledCaseTT[5].size()!=0){
				if(!(listOfAnomaledCaseTT[5].get(duplicates.get(i).get(0)).equals("d"))){
					//memasukkan nomor case
					listCaseActivityDuration[0].add(listOfAnomaledCaseTT[1].get(duplicates.get(i).get(0)));
					
					//memasukkan nama activity
					listCaseActivityDuration[1].add(listOfAnomaledCaseTT[3].get(duplicates.get(i).get(0)));
					
					//memasukkan total duration yang baru
					int sum = 0;
					if(listCaseActivityDuration[2].size() != 0 && listCaseActivityDuration[2].size() > index)
						sum = Integer.parseInt(listCaseActivityDuration[2].get(index));
					if(sum!=0){
						sum += Integer.parseInt(listOfAnomaledCaseTT[4].get(duplicates.get(i).get(0)));
						sum = sum/2;
					}
					else{
						sum += Integer.parseInt(listOfAnomaledCaseTT[4].get(duplicates.get(i).get(0)));
					}
					sum += Integer.parseInt(listOfAnomaledCaseTT[4].get(duplicates.get(i).get(1)));
					sum = sum/2;
					listCaseActivityDuration[2].add(String.valueOf(sum));
					
					//menandai yang sudah dihitung
					if(listOfAnomaledCaseTT[5].size() < duplicates.get(i).get(0))
						for(int x=listOfAnomaledCaseTT[5].size(); x<duplicates.get(i).get(0); x++)
							listOfAnomaledCaseTT[5].add(x, "");
		
					listOfAnomaledCaseTT[5].add(duplicates.get(i).get(0), "d");
					index++;
				}
			}
			else{
				//memasukkan nomor case
				listCaseActivityDuration[0].add(listOfAnomaledCaseTT[1].get(duplicates.get(i).get(0)));
				
				//memasukkan nama activity
				listCaseActivityDuration[1].add(listOfAnomaledCaseTT[3].get(duplicates.get(i).get(0)));
				
				//memasukkan total duration yang baru
				int sum = 0;
				if(listCaseActivityDuration[2].size() != 0 && listCaseActivityDuration[2].size() > index)
					sum = Integer.parseInt(listCaseActivityDuration[2].get(index));
				if(sum!=0){
					sum += Integer.parseInt(listOfAnomaledCaseTT[4].get(duplicates.get(i).get(0)));
					sum = sum/2;
				}
				else{
					sum += Integer.parseInt(listOfAnomaledCaseTT[4].get(duplicates.get(i).get(0)));
				}
				sum += Integer.parseInt(listOfAnomaledCaseTT[4].get(duplicates.get(i).get(1)));
				sum = sum/2;
				listCaseActivityDuration[2].add(String.valueOf(sum));
				
				 //menandai yang sudah dihitung
				if(listOfAnomaledCaseTT[5].size() < duplicates.get(i).get(0))
					for(int x=listOfAnomaledCaseTT[5].size(); x<duplicates.get(i).get(0); x++)
						listOfAnomaledCaseTT[5].add(x, null);
				
				listOfAnomaledCaseTT[5].add(duplicates.get(i).get(0), "d");
				index++;
			}
		}*/
	}
	
	/*
	 * menyimpan duration dari pelanggaran
	 */
	public static void getDurationPelanggaran (OWLDataFactory factory, PelletReasoner reasonerPellet, ArrayList<String>[] listOfAnomaledCaseTT)
	{
		OWLClass Process = factory.getOWLClass(IRI.create(NS1 + "#Process"));
		OWLObjectProperty hasActivity = factory.getOWLObjectProperty(IRI.create(NS1 + "#hasActivity"));
		OWLDataProperty Start_Time = factory.getOWLDataProperty(IRI.create(NS1 + "#Start_Time"));
		OWLDataProperty Complete_Time = factory.getOWLDataProperty(IRI.create(NS1 + "#Complete_Time"));

		Set<OWLNamedIndividual> individualz = reasonerPellet.getInstances(Process, false).getFlattened();
				
		for(int x=0; x<listOfAnomaledCaseTT[0].size(); x++){
			System.out.println("anomali " + listOfAnomaledCaseTT[0].get(x));
			for(OWLNamedIndividual ind:individualz){
				NodeSet<OWLNamedIndividual> process_names = reasonerPellet.getObjectPropertyValues(ind, hasActivity);
				String individu = ind.toString().substring(71,(ind.toString().length()-1));
				Set<OWLLiteral> start = reasonerPellet.getDataPropertyValues(ind, Start_Time);
				Set<OWLLiteral> complete = reasonerPellet.getDataPropertyValues(ind, Complete_Time);
				if(individu.startsWith("Ev")){
					System.out.println(" individu " + individu);
					if(individu.equals(listOfAnomaledCaseTT[0].get(x))){
						System.out.println("  ketemu");
						for(Node<OWLNamedIndividual> name : process_names){
							String teks = name.getRepresentativeElement().getIRI().toString();
							listOfAnomaledCaseTT[3].add(x, teks.substring(teks.indexOf("#")+1));							
							int a = Integer.parseInt(complete.iterator().next().getLiteral()) - Integer.parseInt(start.iterator().next().getLiteral());
							listOfAnomaledCaseTT[4].add(x, String.valueOf(a));
							System.out.println("  durasi "+ listOfAnomaledCaseTT[4]);
							listOfAnomaledCaseTT[5].add(x, "n");
						}
						break;
					}
				}
			}
			System.out.println();
		}
		listOfAnomaledCaseTT[0].add("Event_81_999");
		listOfAnomaledCaseTT[1].add("81");
		listOfAnomaledCaseTT[2].add("999");
		listOfAnomaledCaseTT[3].add("complete_verification");
		listOfAnomaledCaseTT[4].add("2500");
		listOfAnomaledCaseTT[5].add("n");
		listOfAnomaledCaseTT[0].add("Event_81_9999");
		listOfAnomaledCaseTT[1].add("81");
		listOfAnomaledCaseTT[2].add("9999");
		listOfAnomaledCaseTT[3].add("complete_verification");
		listOfAnomaledCaseTT[4].add("500");
		listOfAnomaledCaseTT[5].add("n");
	}
	
	/*
	 * Untuk simpan nama aktivitas dan durasi dari CT
	 */
	public static void getDurationCT(OWLDataFactory factory, PelletReasoner reasonerPellet, String[][] dataCT)
	{
		OWLClass ConjointTask = factory.getOWLClass(IRI.create(NS1 + "#ConjointTask"));
		OWLDataProperty Duration = factory.getOWLDataProperty(IRI.create(NS1 + "#Duration"));
		OWLObjectProperty hasActivity = factory.getOWLObjectProperty(IRI.create(NS1 + "#hasActivity"));
		Set<OWLNamedIndividual> individualx = reasonerPellet.getInstances(ConjointTask, false).getFlattened();
		
		int x = 0;
		for(OWLNamedIndividual ind : individualx){
			NodeSet<OWLNamedIndividual> process_names = reasonerPellet.getObjectPropertyValues(ind, hasActivity);
			String individu = ind.toString().substring(71,(ind.toString().length()-1));
			String cekCT = ind.toString().substring(71,73);
			if(cekCT.equals("CT")){
				Set<OWLLiteral> duration = reasonerPellet.getDataPropertyValues(ind, Duration);
				for(Node<OWLNamedIndividual> name : process_names){
					dataCT[x][0] = individu;
					dataCT[x][1] = individu.substring(3);
					String teks = name.getRepresentativeElement().getIRI().toString();
					dataCT[x][2] = teks.substring(teks.indexOf("#") + 1);
					dataCT[x][3] = duration.iterator().next().getLiteral();
				}
				x++;
			}
		}		
	}
	
	/*
	 * menyimpan aktivitas yang sama di satu case
	 */
	public static void cekDuplikasi(ArrayList<String>[] listOfAnomaledCaseTT, List<List<Integer>> duplicates)
	{
		System.out.println("\nCek duplikasi start " + listOfAnomaledCaseTT[0].size());
		int index_dup=-1;
		for(int h=0; h<listOfAnomaledCaseTT[0].size(); h++){
			if(!listOfAnomaledCaseTT[5].get(h).equals("m")){
				index_dup++;
				duplicates.add(new ArrayList<Integer>());
				duplicates.get(index_dup).add(h);
				listOfAnomaledCaseTT[5].set(h, "m");
			}
			for(int i=1; i<listOfAnomaledCaseTT[0].size(); i++){
				if(h<i){
					if(listOfAnomaledCaseTT[3].get(h).equals(listOfAnomaledCaseTT[3].get(i)) &&
					   listOfAnomaledCaseTT[1].get(h).equals(listOfAnomaledCaseTT[1].get(i)) &&
					   !listOfAnomaledCaseTT[5].get(i).equals("m")){
						duplicates.get(index_dup).add(i);
						listOfAnomaledCaseTT[5].set(i, "m");
					}
				}
			}
		}
		/*//salah
		System.out.println("Cek duplikasi start " + listOfAnomaledCaseTT[1].size());
		int dup=0;
		for(int z=0; z<listOfAnomaledCaseTT[1].size(); z++){
			boolean flag = false;
			for(int y=1; y<listOfAnomaledCaseTT[1].size(); y++){
				if(z!=y &&
				   z<y &&
				   listOfAnomaledCaseTT[1].get(z).equals(listOfAnomaledCaseTT[1].get(y)) &&
				   listOfAnomaledCaseTT[3].get(z).equals(listOfAnomaledCaseTT[3].get(y))
				   ){
					if(listOfAnomaledCaseTT[5].size()==0 ||
					   listOfAnomaledCaseTT[5].size() > z){
						duplicates.add(new ArrayList<Integer>());
						duplicates.get(dup).add(z);
						duplicates.get(dup).add(y);
						listOfAnomaledCaseTT[5].set(z, "m");
						listOfAnomaledCaseTT[5].set(y, "m");
						flag = true;
					}
					else{
						if(!(listOfAnomaledCaseTT[5].get(z).equals("m")) &&
						   !(listOfAnomaledCaseTT[5].get(y).equals("m"))){
							duplicates.add(new ArrayList<Integer>());
							duplicates.get(dup).add(z);
							duplicates.get(dup).add(y);
							listOfAnomaledCaseTT[5].set(z, "m");
							listOfAnomaledCaseTT[5].set(y, "m");
							flag = true;
						}							
					}					   
				}
			}
			if(flag == true) dup++;
		}*/
		System.out.println("Index yg ada aktivitas sama di satu case: ");
		for(int z=0; z<duplicates.size(); z++){
			System.out.println(duplicates.get(z));
		}
		System.out.println("Cek duplikasi selesai");
		System.out.println();
	}

	/*
	 * menyimpan ke Excel
	 */
	public static void saveToExcel(int[][] matrix)
	{
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet("fraud");
		
		int rownum = 0;
		for(int data[]:matrix){
			Row row = sheet.createRow(rownum++);
			int cellnum = 0;
			for(int obj:data){
				Cell cell = row.createCell(cellnum++);
				cell.setCellValue(obj);
			}
		}
		
		try{
			FileOutputStream out = new FileOutputStream(new File (output_file));
			workbook.write(out);
			out.close();
			System.out.println("Successfully saved to excel ");
		}
		catch (FileNotFoundException e){
			e.printStackTrace();
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	
/*
 * menampilkan case beserta jumlah pelanggaran
 */
	public static void printMatrixAnomaledCase(int[][] matrix)
	{
		for(int m = 0 ; m<CASE; m++){
			for(int n=0; n<ANOMALI; n++)
				System.out.print(matrix[m][n] + " ");
			System.out.println();
		}
	}
	
	/*
	 * menampilkan data conjoint task
	 */
	public static void printDataCT(String[][] dataCT)
	{
		for(int m=0; m<25; m++){
			for(int n=0; n<4; n++)
				System.out.print(dataCT[m][n] + " ");
			System.out.println();
		}
	}
	
	/*
	 * menampilkan detil pelanggaran (event id, activity name, duration)
	 */
	public static void printListOfAnomaledCase(ArrayList<String>[] list)
	{
		for(int n=0; n<list[0].size(); n++){
			for(int m=0 ; m<6; m++)
				System.out.print(list[m].get(n) + " ");
			System.out.println();
		}
	}
}
