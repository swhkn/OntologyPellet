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
import java.util.Collections;
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
	private static Integer JUMLAH_ACT = 25;
	
	//Array untuk menyimpan daftar nama anomali
	private static String[] anomaliNama = {"SkipDecision", "SkipSequence", "ThroughputTimeMax", "ThroughputTimeMin", "WrongDecision",
											"WrongDutyCombine", "WrongDutyDecision", "WrongDutySequence", "WrongPattern", "WrongResource"};


	
	public static void main(String[] args) throws OWLOntologyCreationException, OWLOntologyStorageException, FileNotFoundException {
		/*
		 * Daftar Variabel
		 */
		int[][] caseJumlahPelanggaran = new int[CASE][ANOMALI];
		String[][] dataCT = new String[25][4];
		
		
		//Variabel Punya TT
		ArrayList<String>[] listOfAnomaledCaseTT = (ArrayList<String>[]) new ArrayList[6];
		for(int x=0; x<6; x++){
			listOfAnomaledCaseTT[x] = new ArrayList<String>();
		}
		
		List<List<Integer>> duplicates = new ArrayList();
		
		ArrayList<String>[] listCaseActivityDuration = (ArrayList<String>[]) new ArrayList[3];
		for(int x=0; x<3; x++){
			listCaseActivityDuration[x] = new ArrayList<String>();
		}
		
		Integer[][] hasilHitungToleransi = new Integer [5][JUMLAH_ACT];

		/*
		 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  
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
		
		
		// Uncomment few of this section to try
		getTotalPelanggaranEachCase(factory, reasonerPellet, caseJumlahPelanggaran, listOfAnomaledCaseTT);
		printCaseJumlahPelanggaran(caseJumlahPelanggaran);
		//printDataCT(dataCT);
		//saveToExcel(matriks);

		//ini semua untuk itung TT
		getDurasiEachEvent(factory, reasonerPellet, caseJumlahPelanggaran, listOfAnomaledCaseTT);
		printListOfAnomaledCase(listOfAnomaledCaseTT);
		getDurationPelanggaran(factory, reasonerPellet, listOfAnomaledCaseTT);
		cekDuplikasi(listOfAnomaledCaseTT, duplicates);
		countDurationEachActivity(listOfAnomaledCaseTT, duplicates, listCaseActivityDuration);
		perhitunganToleransi(listCaseActivityDuration, dataCT);

		
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
	 */
	
	/*
	 * Mendapatkan total pelanggaran tiap anomali untuk tiap case
	 * Contoh Hasil output:
	 * Case_no | SkipSeq | SkipDec | ... | WrongRes
	 * ----------------------------------------------
	 *     1   |    0	 |	1	   | ... |	15
	 *     2   |    0	 |	1	   | ... |	15
	 *    ...  |    0	 |	1	   | ... |	15
	 *    18   |    0	 |	1	   | ... |	15        
	 */
	public static void getTotalPelanggaranEachCase(OWLDataFactory factory, PelletReasoner reasonerPellet, int[][] caseJumlahPelanggaran, ArrayList<String>[] listOfAnomaledCaseTT) {
		OWLClass Anomali = factory.getOWLClass(IRI.create(NS1 + "#Anomali"));
		OWLObjectProperty hasCase = factory.getOWLObjectProperty(IRI.create(NS1 + "#hasCase"));		
		
		//ambil semua individu yang ada di kelas tersebut
		Set<OWLNamedIndividual> individuals = reasonerPellet.getInstances(Anomali, false).getFlattened();
				
		for(OWLNamedIndividual ind: individuals) {
			NodeSet<OWLNamedIndividual> has_cases = reasonerPellet.getObjectPropertyValues(ind, hasCase);
			
			//panjang string individu
			Integer indLength = ind.toString().length();
			String individu = ind.toString().substring(71,(indLength-1));
			
			//Print nama individu
			System.out.println(individu);
			
			int indexAnomali=0;
			
			//menyimpan index anomali yang sedang dibandingkan
			for(int a=0; a<ANOMALI; a++) {
				if(individu.equals(anomaliNama[a])) {
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
				for(Node<OWLNamedIndividual> has_case : has_cases) {
					String teks = has_case.getRepresentativeElement().getIRI().toString();
					caseNo = Integer.parseInt(teks.substring(76,teks.indexOf("_", 76)));
					event = teks.substring(teks.indexOf("#") + 1);
					
					System.out.println(" hasCase: " + event);
					
					if(caseNo>0)
						caseJumlahPelanggaran[caseNo-1][indexAnomali]++;
				}
			}	
			System.out.println();
		}
	}
	
	/*
	 * 
	 ********************
	 *  ThroughputTime  *
	 ********************
	 */
	public static void getDurasiEachEvent(OWLDataFactory factory, PelletReasoner reasonerPellet, int[][] caseJumlahPelanggaran, ArrayList<String>[] listOfAnomaledCaseTT) {
		OWLClass Anomali = factory.getOWLClass(IRI.create(NS1 + "#Anomali"));
		OWLObjectProperty hasCase = factory.getOWLObjectProperty(IRI.create(NS1 + "#hasCase"));		
		
		//ambil semua individu yang ada di kelas tersebut
		Set<OWLNamedIndividual> individuals = reasonerPellet.getInstances(Anomali, false).getFlattened();
				
		for(OWLNamedIndividual ind: individuals){
			NodeSet<OWLNamedIndividual> has_cases = reasonerPellet.getObjectPropertyValues(ind, hasCase);
			
			//panjang string individu
			Integer indLength = ind.toString().length();
			String individu = ind.toString().substring(71,(indLength-1));
			
			if(individu.equals(anomaliNama[2]) || individu.equals(anomaliNama[3])) {
				if(has_cases.isEmpty())
					System.out.println(" tidak ada anomali " + individu);
				else{
					System.out.println(" ada anomali " + individu);
					for(Node<OWLNamedIndividual> has_case : has_cases) {
						String teks = has_case.getRepresentativeElement().getIRI().toString();
						int caseNo = Integer.parseInt(teks.substring(76,teks.indexOf("_", 76)));
						String event = teks.substring(teks.indexOf("#") + 1);
						
						//simpan ke daftar anomali case TT
						if(caseNo>0) {
							listOfAnomaledCaseTT[0].add(event);
							listOfAnomaledCaseTT[1].add(String.valueOf(caseNo));
							listOfAnomaledCaseTT[2].add(teks.substring(79));
						}		
					}
				}	
			}			
			System.out.println();
		}
	}
	
	/* 
	 * Menghitung rerata durasi untuk aktivitas yang sama di suatu case
	 * Misal: di Case 1 ada a-b-c-a, durasi a yang baru = (a+a)/2
	 */
	public static void countDurationEachActivity(ArrayList<String>[] listOfAnomaledCaseTT, List<List<Integer>> duplicates, ArrayList<String>[] listCaseActivityDuration) {
		System.out.println("countDurationEachActivity " + duplicates.size());
		int i=0;
		int sum2;
		while(i<duplicates.size()) {
			int sum=0;
			int size=0;
			for(int j=0; j<duplicates.get(i).size(); j++) {
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
		for(int j=0; j<listCaseActivityDuration[0].size(); j++) {
			System.out.println("case :" + listCaseActivityDuration[0].get(j));
			System.out.println(" activity: " + listCaseActivityDuration[1].get(j));
			System.out.println(" duration: " + listCaseActivityDuration[2].get(j));
		}
		System.out.println();
	}
	
	/*
	 * menyimpan duration dari pelanggaran untuk tiap event
	 * Event_8_91 durasinya berapa
	 */
	public static void getDurationPelanggaran (OWLDataFactory factory, PelletReasoner reasonerPellet, ArrayList<String>[] listOfAnomaledCaseTT) {
		OWLClass Process = factory.getOWLClass(IRI.create(NS1 + "#Process"));
		OWLObjectProperty hasActivity = factory.getOWLObjectProperty(IRI.create(NS1 + "#hasActivity"));
		OWLDataProperty Start_Time = factory.getOWLDataProperty(IRI.create(NS1 + "#Start_Time"));
		OWLDataProperty Complete_Time = factory.getOWLDataProperty(IRI.create(NS1 + "#Complete_Time"));

		Set<OWLNamedIndividual> individualz = reasonerPellet.getInstances(Process, false).getFlattened();
				
		for(int x=0; x<listOfAnomaledCaseTT[0].size(); x++) {
			System.out.println("anomali " + listOfAnomaledCaseTT[0].get(x));
			for(OWLNamedIndividual ind:individualz) {
				NodeSet<OWLNamedIndividual> process_names = reasonerPellet.getObjectPropertyValues(ind, hasActivity);
				String individu = ind.toString().substring(71,(ind.toString().length()-1));
				Set<OWLLiteral> start = reasonerPellet.getDataPropertyValues(ind, Start_Time);
				Set<OWLLiteral> complete = reasonerPellet.getDataPropertyValues(ind, Complete_Time);
				if(individu.startsWith("Ev")) {
					System.out.println(" individu " + individu);
					if(individu.equals(listOfAnomaledCaseTT[0].get(x))) {
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
		/*
		//sample test case
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
		listOfAnomaledCaseTT[5].add("n");*/
	}
	
	
	/*
	 * Untuk simpan nama aktivitas dan durasi dari CT
	 */
	public static void getDurationCT(OWLDataFactory factory, PelletReasoner reasonerPellet, String[][] dataCT) {
		OWLClass ConjointTask = factory.getOWLClass(IRI.create(NS1 + "#ConjointTask"));
		OWLDataProperty Duration = factory.getOWLDataProperty(IRI.create(NS1 + "#Duration"));
		OWLObjectProperty hasActivity = factory.getOWLObjectProperty(IRI.create(NS1 + "#hasActivity"));
		Set<OWLNamedIndividual> individualx = reasonerPellet.getInstances(ConjointTask, false).getFlattened();
		
		int x = 0;
		for(OWLNamedIndividual ind : individualx) {
			NodeSet<OWLNamedIndividual> process_names = reasonerPellet.getObjectPropertyValues(ind, hasActivity);
			String individu = ind.toString().substring(71,(ind.toString().length()-1));
			String cekCT = ind.toString().substring(71,73);
			if(cekCT.equals("CT")) {
				Set<OWLLiteral> duration = reasonerPellet.getDataPropertyValues(ind, Duration);
				for(Node<OWLNamedIndividual> name : process_names) {
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
	public static void cekDuplikasi(ArrayList<String>[] listOfAnomaledCaseTT, List<List<Integer>> duplicates) {
		System.out.println("\nCek duplikasi start " + listOfAnomaledCaseTT[0].size());
		int index_dup=-1;
		for(int h=0; h<listOfAnomaledCaseTT[0].size(); h++) {
			if(!listOfAnomaledCaseTT[5].get(h).equals("m")) {
				index_dup++;
				duplicates.add(new ArrayList<Integer>());
				duplicates.get(index_dup).add(h);
				listOfAnomaledCaseTT[5].set(h, "m");
			}
			for(int i=1; i<listOfAnomaledCaseTT[0].size(); i++) {
				if(h<i) {
					if(listOfAnomaledCaseTT[3].get(h).equals(listOfAnomaledCaseTT[3].get(i)) &&
					   listOfAnomaledCaseTT[1].get(h).equals(listOfAnomaledCaseTT[1].get(i)) &&
					   !listOfAnomaledCaseTT[5].get(i).equals("m")) {
						duplicates.get(index_dup).add(i);
						listOfAnomaledCaseTT[5].set(i, "m");
					}
				}
			}
		}
		System.out.println("Index yg ada aktivitas sama di satu case: ");
		for(int z=0; z<duplicates.size(); z++) {
			System.out.println(duplicates.get(z));
		}
		System.out.println("Cek duplikasi selesai");
		System.out.println();
	}
	
	public static void perhitunganToleransi(ArrayList<String>[] listCaseActivityDuration, String[][] dataCT) {
		
		//test case tambahan
		listCaseActivityDuration[0].add("9"); listCaseActivityDuration[1].add("complete_verification"); listCaseActivityDuration[2].add("1700");
		listCaseActivityDuration[0].add("9"); listCaseActivityDuration[1].add("loan_decision"); listCaseActivityDuration[2].add("2000");
		
		Collections.sort(listCaseActivityDuration[1]);
		
		for(int i = 0; i < listCaseActivityDuration[0].size(); i++) {	
			System.out.println(listCaseActivityDuration[1].get(i));
			
			// menghitung sum dari value tiap activity
			Float count = new Float (1.0);
			Float sum = Float.valueOf(listCaseActivityDuration[2].get(i));
			
			for(int j = 1; j < listCaseActivityDuration[0].size(); i++) {
				if( i < j &&
					listCaseActivityDuration[1].get(i).equals(listCaseActivityDuration[1].get(j))) {
					sum += Float.valueOf(listCaseActivityDuration[2].get(j));
					count++;
				}
			}
			
			//menghitung mean dari tiap activity
			Float mean = sum/count;
			
			//menghitung toleransi tiap activity
			Float durasi_CT = new Float (0.0);
			
			for(int x = 0; x < 25; x++) 
				if (listCaseActivityDuration[1].get(i).equals(dataCT[0][i]))
					durasi_CT = Float.valueOf(dataCT[3][i]);
			
			Float toleransi = Math.abs(durasi_CT - mean);
			Float toleransi_atas = mean + toleransi;
			Float toleransi_bawah = mean - toleransi;
			
			//belum selesai... pulang dulu..
						
			
		}
	}
	
	/*
	 ***********************************************************
	 */

	
	/*
	 * menyimpan ke Excel
	 */
	public static void saveToExcel(int[][] matrix) {
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet("fraud");
		
		int rownum = 0;
		for(int data[]:matrix) {
			Row row = sheet.createRow(rownum++);
			int cellnum = 0;
			for(int obj:data) {
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
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	
/*
 ******************************************************
 *    code untuk menampilkan sesuatu ke console       *
 ******************************************************
 */
	

	// menampilkan case beserta jumlah pelanggaran
	public static void printCaseJumlahPelanggaran(int[][] caseJumlahPelanggaran) {
		for(int m = 0 ; m<CASE; m++) {
			for(int n=0; n<ANOMALI; n++)
				System.out.print(caseJumlahPelanggaran[m][n] + " ");
			System.out.println();
		}
	}
	
	
	// menampilkan data conjoint task
	public static void printDataCT(String[][] dataCT) {
		for(int m=0; m<25; m++) {
			for(int n=0; n<4; n++)
				System.out.print(dataCT[m][n] + " ");
			System.out.println();
		}
	}
	
	
	// menampilkan detil pelanggaran (event id, activity name, duration)
	public static void printListOfAnomaledCase(ArrayList<String>[] list) {
		for(int n=0; n<list[0].size(); n++) {
			for(int m=0 ; m<3; m++)
				System.out.print(list[m].get(n) + " ");
			System.out.println();
		}
	}
}
