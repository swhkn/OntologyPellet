package OntologyPellet;

import java.net.URI;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import com.clarkparsia.pellet.owlapiv3.Reasoner;

public class satu {
	private static String fname="file:///D:/COLLEGE/TA/Progress/Ontology/ModelUpdateRev3.owl";
	private static String NS ="http://www.semanticweb.org/riri/ontologies/2015/3/untitled-ontology-4";

	public static void main(String[] args) {
		//create an ontology manager
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = null;
		//read the ontology
		try {
			ontology = manager.loadOntology(IRI.create(fname));
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//create the pellet reasoner
		Reasoner reasoner = new Reasoner (manager);
		reasoner.loadOntology(ontology);
		
		//get the instances of a class
		IRI ConjointTask = IRI.create("#ConjointTask");
		OWLClass CT = manager.getOWLDataFactory().getOWLClass(ConjointTask);
		//Set inttances = reasoner.getIns
		
		/*
		OWLOntology inferredOntology; 
        // Create Reasoner
        OWLReasonerFactory reasonerFactory = new PelletReasonerFactory();
        OWLReasoner reasoner = reasonerFactory.createReasoner(manager);

        // Load the ontologies into the reasoner.
        Set<OWLOntology> importsClosure = manager.getImportsClosure(inferredOntology);
        reasoner.loadOntologies(importsClosure);

        // Reason!
        reasoner.classify();

        InferredOntologyGenerator iog = new InferredOntologyGenerator(reasoner);
        iog.fillOntology(manager, inferredOntology);
				 */

	}

}
