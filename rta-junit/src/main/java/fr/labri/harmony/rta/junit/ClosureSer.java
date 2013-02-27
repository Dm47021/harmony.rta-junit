package fr.labri.harmony.rta.junit;


import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public class ClosureSer implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Map<HashElement,Set<HashElement>> closure;
	
	public ClosureSer(Map<HashElement,Set<HashElement>> c) {
		this.closure = c;
	}

	public Map<HashElement,Set<HashElement>> getClosure() {
		return closure;
	}

	public void setClosure(Map<HashElement,Set<HashElement>> closure) {
		this.closure = closure;
	}

}
