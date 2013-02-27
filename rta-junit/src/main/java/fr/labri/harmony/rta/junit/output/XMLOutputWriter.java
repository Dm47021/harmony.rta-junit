package fr.labri.harmony.rta.junit.output;

import java.io.FileOutputStream;
import java.util.Map;
import java.util.Set;


import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;

import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;


import fr.labri.harmony.rta.junit.HashElement;

public class XMLOutputWriter implements OutputWriter {

	@Override
	public void saveOutput(Map<HashElement, Set<HashElement>> closure, String file) {
		//Sauvegarder config
		Element racine = new Element("tests-configurations");
		org.jdom2.Document document = new Document(racine);
		for(HashElement test : closure.keySet()) {
			Element test_config = new Element("test_configuration");
			Element id = new Element("test_id");
			Attribute res = new Attribute("result","true");
			Attribute testid = null;
			Attribute hash = null;
			try{
				testid = new Attribute("id",test.getId());
				if(test.getHash()==null)
					hash =new Attribute("hash","toto");
				else
					hash =new Attribute("hash",test.getHash());
			}
			catch(org.jdom2.IllegalDataException e) {
				testid = new Attribute("id",test.getId());
				hash =new Attribute("hash","toto");
			}
			id.setAttribute(res);
			id.setAttribute(testid);
			id.setAttribute(hash);

			test_config.addContent(id);
			Element depTest = new Element("dependencies");
			for(HashElement dep : closure.get(test)) {
				Element prog = new Element("element");
				Attribute idprog = null;
				Attribute idHash = null;
				try{
					idprog = new Attribute("id",dep.getId().trim());
					idHash = new Attribute("hash",dep.getHash().trim());
				}
				catch(org.jdom2.IllegalDataException e) {
					idprog = new Attribute("id",dep.getId().trim());
					idHash = new Attribute("hash","toto");
				}
				//System.out.println(idprog+" "+idHash);
				prog.setAttribute(idprog);
				prog.setAttribute(idHash);
				depTest.addContent(prog);
			}
			test_config.addContent(depTest);
			racine.addContent(test_config);
		}
		//Fermeture du xml
		try
		{
			//On utilise ici un affichage classique avec getPrettyFormat()
			XMLOutputter sortie = new XMLOutputter(Format.getPrettyFormat());
			//Remarquez qu'il suffit simplement de créer une instance de FileOutputStream
			//avec en argument le nom du fichier pour effectuer la sérialisation.
			FileOutputStream fos = new FileOutputStream(file);
			//GZIPOutputStream gzos = new GZIPOutputStream(fos);
			sortie.output(document, fos);

		}
		catch (java.io.IOException e){
			e.printStackTrace();
		}
	}


}
