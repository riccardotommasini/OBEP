package sr.obep;

import org.semanticweb.owlapi.formats.ManchesterSyntaxDocumentFormat;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class TestUtils {

    public static void saveOntology(OWLOntology ontology, OWLOntologyManager manager, String suffix) {
        String location = "./";
        try {
            File file = new File(location + "savedontology" + suffix + ".owl");
            if (!file.canExecute()) {
                File mkdir = new File(location);
                mkdir.mkdirs();
            }
            file.createNewFile();
            manager.saveOntology(ontology, new ManchesterSyntaxDocumentFormat(), new FileOutputStream(file));
        } catch (OWLOntologyStorageException | IOException e) {
            e.printStackTrace();
        }

    }
}
