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
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

public class dua {
	private static String NS1 = "http://www.semanticweb.org/riri/ontologies/2015/3/untitled-ontology-4";
	private static int ANOMALI = 10;
	private static int CASE = 1200; 
	
	public static void main(String[] args) throws OWLOntologyCreationException, OWLOntologyStorageException, FileNotFoundException {
		System.out.println("Start");		
		
		//create an ontology manager
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();
		
		//Lokasi File Ontology OWL
		File file1 = new File("D:\\COLLEGE\\TA\\Progress\\Ontology\\ModelUpdateRev4.owl");
		
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(file1);
		
		System.out.println("Model " + ontology + " Loaded...");
		IRI documentIRI = manager.getOntologyDocumentIRI(ontology);
		System.out.println("  from: " + documentIRI);
		
		//Create + Load PelletReasoner
		PelletReasoner reasonerPellet = PelletReasonerFactory.getInstance().createReasoner(ontology);
		
		OWLClass Resource = factory.getOWLClass(IRI.create(NS1 + "#Anomali"));
		OWLObjectProperty hasCase = factory.getOWLObjectProperty(IRI.create(NS1 + "#hasCase"));
		//OWLDataProperty Role_Name = factory.getOWLDataProperty(IRI.create(NS1 + "#Role_Name"));
		
		
		//ambil semua individu yang ada di kelas tersebut
		Set<OWLNamedIndividual> individuals = reasonerPellet.getInstances(Resource, false).getFlattened();
	
		System.out.println("Size of Individuals: " + individuals.size() + "\n");
		int i=0;		

		int[][] matriks = new int[1200][10];
		
		//Array untuk menyimpan daftar nama anomali
		String[] anomaliNama = {"SkipDecision", "SkipSequence", "ThroughputTimeMax", "ThroughputTimeMin", "WrongDecision",
				"WrongDutyCombine", "WrongDutyDecision", "WrongDutySequence", "WrongPattern", "WrongResource"};

		int j = 1,
			k = 1;
		
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
			System.out.println(" Type: " + type);
			i++;
			
			//menyimpan index anomali yang sedang dibandingkan
			for(int a=0; a<ANOMALI; a++){
				if(individu.equals(anomaliNama[a]))
					k = a;
			}
			
			//Print anomali yang ada
			if(res_names.isEmpty())
				System.out.println(" hasCase: tidak ada");
			else{
				for(Node<OWLNamedIndividual> name : res_names){
					String teks = name.getRepresentativeElement().getIRI().toString();
					int caseNo = Integer.parseInt(teks.substring(79));
					System.out.println(" hasCase: " + caseNo);
						
					//simpan ke matriks anomali
					matriks[caseNo][k] = 1;
					j++;
				}
			}
			
			System.out.println();
		}
		System.out.println(j);
		
		for(int m = 1 ; m<=j; m++)
		{
			for(int n=0; n<ANOMALI; n++)
				System.out.print(matriks[m][n] + " ");
			System.out.println();
		}
		
		System.out.println("done");
		
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet("fraud");
		
		int rownum = 1;
		for(int data[]:matriks){
			Row row = sheet.createRow(rownum++);
			int cellnum = 1;
			for(int obj:data)
			{
				Cell cell = row.createCell(cellnum++);
				cell.setCellValue(obj);
			}
		}
		try{
			FileOutputStream out = new FileOutputStream(new File ("D:/fileO.xlsx"));
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
}
