package OntologyPellet;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
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
import org.semanticweb.owlapi.util.InferredAxiomGenerator;
import org.semanticweb.owlapi.util.InferredOntologyGenerator;
import org.semanticweb.owlapi.util.InferredPropertyAssertionGenerator;

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

public class dua {
	private static String NS1 = "http://www.semanticweb.org/riri/ontologies/2015/3/untitled-ontology-4";
	private static String NS = "untitled-ontology-4";
	
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
	

		System.out.println("Size of Individuals: " + individuals.size());
		int i=0;
		for(OWLNamedIndividual ind: individuals){	
			
			//get the info about this specific individual
			NodeSet<OWLClass> types = reasonerPellet.getTypes(ind, true);
			NodeSet<OWLNamedIndividual> res_names = reasonerPellet.getObjectPropertyValues(ind, hasCase);
			//Set<OWLLiteral> names = reasonerPellet.getDataPropertyValues(ind, Role_Name);
			
			/*Iterator nameIt = names.iterator();
			while(nameIt.hasNext())
				System.out.println("Resource Name: " + ((OWLLiteral) nameIt.next()).getLiteral());
			*/
			System.out.println(ind.toString());
			OWLClass type = types.iterator().next().getRepresentativeElement();
			System.out.println(" Type: " + type);
			i++;
			
			if(res_names.isEmpty())
				System.out.println(" hasCase: tidak ada");
			else{
				for(Node<OWLNamedIndividual> name : res_names){
					System.out.println(" hasCase: " + name.getRepresentativeElement().getIRI());
				}
			}
		}
		System.out.println(i);
		 
		
		boolean consistent = reasonerPellet.isConsistent();
		System.out.println("Consistent: " + consistent + "\n");

		//jalankan reasoner
		reasonerPellet.getKB().realize();
		//reasonerPellet.getKB().printClassTree();		
/*
		List<InferredAxiomGenerator<? extends OWLAxiom>> axiomGenerators = 
				new ArrayList<InferredAxiomGenerator<? extends OWLAxiom>>();
		axiomGenerators.add(new InferredPropertyAssertionGenerator());
		
		
		OWLOntology infOntology = manager.createOntology();
		InferredOntologyGenerator iog = new InferredOntologyGenerator(reasonerPellet, axiomGenerators);
		iog.fillOntology(manager, infOntology);
		
		
		//Save new ontology
		OutputStream owlOutputStream = new ByteArrayOutputStream();
		manager.saveOntology(infOntology, owlOutputStream);
		

		//File untuk menyimpan output
		File fileO = new File("D:/fileO.txt");

		//Writer untuk menuliskan ke file output 
		Writer output = null;
		try {
			output = new BufferedWriter(new FileWriter(fileO));
			output.write(owlOutputStream.toString());
			output.close();
			owlOutputStream.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		*/
		
		System.out.println("done");
	}
}
