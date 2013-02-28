package fr.labri.harmony.rta.junit.main;

import java.util.Map;
import java.util.Set;

import com.esotericsoftware.kryo.io.Output;

import fr.labri.harmony.rta.junit.HashElement;
import fr.labri.harmony.rta.junit.jdt.JDTGeneratorRTA;
import fr.labri.harmony.rta.junit.output.OutputWriter;
import fr.labri.harmony.rta.junit.output.XMLOutputWriter;

public class Main {
	
	public static void main(String[] args) {
		
		String f = "src/main/test";
		JDTGeneratorRTA g = new JDTGeneratorRTA(f);
		g.updateConfiguration();
		g.scanDirectory();
		
		Map<HashElement, Set<HashElement>> closure = g.computeClosure();
		
		OutputWriter writer = new XMLOutputWriter();
		
		writer.saveOutput(closure,"coverage.xml");
		
	}
	
}
