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
	private static int ANOMALI = 10;
	private static int CASE = 100; 
	private static final String file_OWL = "D:\\COLLEGE\\TA\\EventlogsDebug\\TMax.owl";
	private static final String output_file = "D:\\COLLEGE\\TA\\Dataset\\fileO.xlsx";
	
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
		
		int individualSize = individuals.size();
		System.out.println("Size of Individuals: " + individualSize + "\n");
		
		int i=0;		

		//ini diubah dinamis juga
		//!!!!!!!!!!!!!!!!!!!!!!!
		int[][] matriks = new int[CASE][ANOMALI];
		
		//Array untuk menyimpan daftar nama anomali
		String[] anomaliNama = {"SkipDecision", "SkipSequence", "ThroughputTimeMax", "ThroughputTimeMin", "WrongDecision",
				"WrongDutyCombine", "WrongDutyDecision", "WrongDutySequence", "WrongPattern", "WrongResource"};


		String[][] listOfAnomaledCase = new String[50][6];

		
		
		for(OWLNamedIndividual ind: individuals){
			//get the info about this specific individual
			NodeSet<OWLClass> types = reasonerPellet.getTypes(ind, true);
			NodeSet<OWLNamedIndividual> res_names = reasonerPellet.getObjectPropertyValues(ind, hasCase);
			
			//panjang string individu
			int indLength = ind.toString().length();
			String individu = ind.toString().substring(71,(indLength-1));
			
			//Print nama individu
			System.out.println(individu);
			
			//Print tipe kelas
			OWLClass type = types.iterator().next().getRepresentativeElement();
			System.out.println(" Type: " + type.toString().substring(71));
			i++;
			
			int k=0;
			//menyimpan index anomali yang sedang dibandingkan
			for(int a=0; a<ANOMALI; a++){
				if(individu.equals(anomaliNama[a])){
					k = a;
					System.out.println(anomaliNama[a] + " " + k);
				}
					
			}
			
			//nanti dibenerin bagian substring event, dibuat lebih dinamis di bagian indexOf ("_")
			
			//Print anomali yang ada
			String event;
			int caseNo = 0;
			int j = 0;
			
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
						matriks[caseNo-1][k]++;
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
		
		PrefixOWLOntologyFormat pm = (PrefixOWLOntologyFormat) manager.getOntologyFormat(ontology);
		
		pm.setDefaultPrefix(NS1 + "#");
		
		System.out.println(pm);
		for(int x=0; x<1;x++){
			//OWLNamedIndividual event_id= (OWLNamedIndividual) factory.getOWLAnonymousIndividual(NS1 + "#" + listOfAnomaledCase[x][0]);	
		}
		
		
		for(int m = 0 ; m<individualSize; m++)
		{
			for(int n=0; n<5; n++)
				System.out.print(listOfAnomaledCase[m][n] + " ");
			System.out.println();
		}
		
		//printMatrixAnomaledCase(matriks);
		System.out.println("done");
		//saveToExcel(matriks);
		
		matriks = null;
	}
	
	public static void saveToExcel(int[][] matrix)
	{
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet("fraud");
		
		int rownum = 0;
		for(int data[]:matrix){
			Row row = sheet.createRow(rownum++);
			int cellnum = 0;
			for(int obj:data)
			{
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

		for(int m = 0 ; m<CASE; m++)
		{
			for(int n=0; n<ANOMALI; n++)
				System.out.print(matrix[m][n] + " ");
			System.out.println();
		}
		
	}
}
