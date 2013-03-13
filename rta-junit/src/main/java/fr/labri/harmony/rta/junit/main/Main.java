package fr.labri.harmony.rta.junit.main;

import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import com.esotericsoftware.kryo.io.Output;

import fr.labri.harmony.rta.junit.HashElement;
import fr.labri.harmony.rta.junit.jdt.JDTGeneratorRTA;
import fr.labri.harmony.rta.junit.output.OutputWriter;
import fr.labri.harmony.rta.junit.output.XMLOutputWriter;

public class Main {
	
	public static void main(String[] args) {
//		
//		String f = "src/main/test";
//		JDTGeneratorRTA g = new JDTGeneratorRTA(f);
//		g.updateConfiguration();
//		g.scanDirectory();
//		
//		Map<HashElement, Set<HashElement>> closure = g.computeClosure();
//		
//		OutputWriter writer = new XMLOutputWriter();
//		
//		writer.saveOutput(closure,"coverage.xml");
		
		File out = Runner.runTwoTrace(RunTest.class.getCanonicalName(), Arrays.asList("fr.labri.harmony.rta.junit.main.RunTest","fr.labri.harmony.rta.junit.main.ClassTest","test1"), new String[]{"fr.labri.harmony.rta.junit.main.ClassTest"});
		System.out.println(out);
	}
	
}
