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
	
	public static void main(String[] args) throws OWLOntologyCreationException, OWLOntologyStorageException, FileNotFoundException {
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
		
		Integer individualSize = individuals.size();
		System.out.println("Size of Individuals: " + individualSize + "\n");
		
		int i=0;		

		int[][] caseJumlahPelanggaran = new int[CASE][ANOMALI];
		String[][] dataCT = new String[25][4];
		String[][] listOfAnomaledCase = new String[50][7];
		
		//Array untuk menyimpan daftar nama anomali
		String[] anomaliNama = {"SkipDecision", "SkipSequence", "ThroughputTimeMax", "ThroughputTimeMin", "WrongDecision",
				"WrongDutyCombine", "WrongDutyDecision", "WrongDutySequence", "WrongPattern", "WrongResource"};


		

		Integer j = 0;
		for(OWLNamedIndividual ind: individuals){
			//get the info about this specific individual
			NodeSet<OWLClass> types = reasonerPellet.getTypes(ind, true);
			NodeSet<OWLNamedIndividual> res_names = reasonerPellet.getObjectPropertyValues(ind, hasCase);
			
			//panjang string individu
			Integer indLength = ind.toString().length();
			String individu = ind.toString().substring(71,(indLength-1));
			
			//Print nama individu
			System.out.println(individu);
			
			//Print tipe kelas
			OWLClass type = types.iterator().next().getRepresentativeElement();
			System.out.println(" Type: " + type.toString().substring(71));
			i++;
			
			int indexAnomali=0;
			//menyimpan index anomali yang sedang dibandingkan
			for(int a=0; a<ANOMALI; a++){
				if(individu.equals(anomaliNama[a])){
					indexAnomali = a;
					System.out.println(anomaliNama[a] + " " + indexAnomali);
				}					
			}
			
			//nanti dibenerin bagian substring event, dibuat lebih dinamis di bagian indexOf ("_")
			
			//Print anomali yang ada
			String event;
			int caseNo = 0;
			
			if(res_names.isEmpty())
				System.out.println(" hasCase: tidak ada");
			else{
				for(Node<OWLNamedIndividual> name : res_names){
					String teks = name.getRepresentativeElement().getIRI().toString();
					caseNo = Integer.parseInt(teks.substring(76,teks.indexOf("_", 76)));
					event = teks.substring(teks.indexOf("#") + 1);
					
					System.out.println(" hasCase: " + event + " " + j);
					
					//simpan ke matriks anomali
					if(caseNo>0){
						caseJumlahPelanggaran[caseNo-1][indexAnomali]++;
						listOfAnomaledCase[j][0] = event;
						listOfAnomaledCase[j][1] = String.valueOf(caseNo);
						listOfAnomaledCase[j][2] = teks.substring(79);						
					}
					j++;
				}
				System.out.println(" hasCase: ada");
			}	
			System.out.println();
		}
		listOfAnomaledCase[10][1] = "81";
		listOfAnomaledCase[10][3] = "complete_verification";
		listOfAnomaledCase[10][4] = "2500";
		
		OWLClass Process = factory.getOWLClass(IRI.create(NS1 + "#Process"));
		OWLObjectProperty hasActivity = factory.getOWLObjectProperty(IRI.create(NS1 + "#hasActivity"));
		OWLDataProperty Start_Time = factory.getOWLDataProperty(IRI.create(NS1 + "#Start_Time"));
		OWLDataProperty Complete_Time = factory.getOWLDataProperty(IRI.create(NS1 + "#Complete_Time"));

		Set<OWLNamedIndividual> individualz = reasonerPellet.getInstances(Process, false).getFlattened();
		
		for(OWLNamedIndividual ind:individualz){
			NodeSet<OWLNamedIndividual> process_names = reasonerPellet.getObjectPropertyValues(ind, hasActivity);
			String individu = ind.toString().substring(71,(ind.toString().length()-1));
			Set<OWLLiteral> start = reasonerPellet.getDataPropertyValues(ind, Start_Time);
			Set<OWLLiteral> complete = reasonerPellet.getDataPropertyValues(ind, Complete_Time);
			for(int x=0; x<=j; x++){
				if(individu.equals(listOfAnomaledCase[x][0])){
					for(Node<OWLNamedIndividual> name : process_names){
						String teks = name.getRepresentativeElement().getIRI().toString();
						listOfAnomaledCase[x][3] = teks.substring(teks.indexOf("#")+1);
						int a = Integer.parseInt(complete.iterator().next().getLiteral()) - Integer.parseInt(start.iterator().next().getLiteral());
						listOfAnomaledCase[x][4] = String.valueOf(a);
					}
					break;	
				}				 
			}
		}
		
		//Untuk simpan nama aktivitas dan durasi dari CT
		OWLClass ConjointTask = factory.getOWLClass(IRI.create(NS1 + "#ConjointTask"));
		OWLDataProperty Duration = factory.getOWLDataProperty(IRI.create(NS1 + "#Duration"));
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
		
		printListOfAnomaledCase(listOfAnomaledCase, 11);
		//printDataCT(dataCT);
		//printMatrixAnomaledCase(matriks);
		//saveToExcel(matriks);
		
		caseJumlahPelanggaran = null;
		dataCT = null;
		listOfAnomaledCase = null;
		
		List<List<Integer>> duplicates = new ArrayList();
		
		for(int z=0; z<1; z++){
			for(int y=1; y<1; y++){
				if(z!=y &&
				   listOfAnomaledCase[z][1].equals(listOfAnomaledCase[y][1]) &&
				   listOfAnomaledCase[z][3].equals(listOfAnomaledCase[y][3])){
					duplicates.add(new ArrayList<Integer>());
					duplicates.get(0).add(z);
					duplicates.get(0).add(y);
				}
			}
		}
		List<List<Integer>> taskDuration = new ArrayList();
		
	}
	
	
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
	
	public static void printMatrixAnomaledCase(int[][] matrix)
	{
		for(int m = 0 ; m<CASE; m++){
			for(int n=0; n<ANOMALI; n++)
				System.out.print(matrix[m][n] + " ");
			System.out.println();
		}
	}
	
	public static void printDataCT(String[][] dataCT)
	{
		for(int m=0; m<25; m++){
			for(int n=0; n<4; n++)
				System.out.print(dataCT[m][n] + " ");
			System.out.println();
		}
	}
	
	public static void printListOfAnomaledCase(String[][] list, int idSize)
	{
		for(int m = 0 ; m<idSize; m++){
			for(int n=0; n<7; n++)
				System.out.print(list[m][n] + " ");
			System.out.println();
		}
	}
}
