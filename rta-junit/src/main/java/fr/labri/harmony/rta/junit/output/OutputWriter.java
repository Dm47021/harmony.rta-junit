package fr.labri.harmony.rta.junit.output;

import java.util.Map;
import java.util.Set;

import fr.labri.harmony.rta.junit.HashElement;

public interface OutputWriter {
		
	public void saveOutput(Map<HashElement, Set<HashElement>> closure, String file);
	
}
