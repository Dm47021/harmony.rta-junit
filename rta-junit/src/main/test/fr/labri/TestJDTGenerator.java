package fr.labri;

import fr.labri.harmony.rta.junit.jdt.JDTGeneratorRTA;

public class TestJDTGenerator {

	public static void main(String[] args) {

		String f = "src/main/test";
		JDTGeneratorRTA g = new JDTGeneratorRTA(f);
		g.updateConfiguration();
		g.scanDirectory();

		g.computeClosure();

	}
}
