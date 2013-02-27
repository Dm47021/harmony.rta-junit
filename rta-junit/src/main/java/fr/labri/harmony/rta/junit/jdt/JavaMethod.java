package fr.labri.harmony.rta.junit.jdt;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JavaMethod implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JavaClass className;

	private boolean isIntern;
	
	private boolean isConstructor;

	public boolean isConstructor() {
		return isConstructor;
	}

	public void setConstructor(boolean isConstructor) {
		this.isConstructor = isConstructor;
	}

	private boolean isTestMethod = false;
	
	public boolean isBeforeClassMethod() {
		return isBeforeClassMethod;
	}

	public void setBeforeClassMethod(boolean isBeforeClassMethod) {
		this.isBeforeClassMethod = isBeforeClassMethod;
	}

	public boolean isBeforeMethod() {
		return isBeforeMethod;
	}

	public void setBeforeMethod(boolean isBeforeMethod) {
		this.isBeforeMethod = isBeforeMethod;
	}

	private boolean isBeforeClassMethod = false;
	
	private boolean isBeforeMethod = false;
	
	private boolean isStaticBlock = false;

	public boolean isStaticBlock() {
		return isStaticBlock;
	}

	public void setStaticBlock(boolean isStaticBlock) {
		this.isStaticBlock = isStaticBlock;
	}

	private String qualifiedName;

	// Name+parameters
	private String shortSignature;

	// A string under the form A::b() representing a call site a.b() where A is
	// the static type of A
	private List<String> callsSite = new ArrayList<String>();
	
	private Set<JavaMethod> directCalls= new HashSet<JavaMethod>();

	private Set<JavaClass> instantiatedClass = new HashSet<JavaClass>();

	public Set<JavaMethod> getDirectCalls() {
		return directCalls;
	}

	public void setDirectCalls(Set<JavaMethod> directCalls) {
		this.directCalls = directCalls;
	}

	public JavaClass getClassName() {
		return className;
	}

	public void setClassName(JavaClass className) {
		this.className = className;
	}

	public void setIntern(boolean isIntern) {
		this.isIntern = isIntern;
	}

	public boolean isIntern() {
		return isIntern;
	}

	public JavaMethod() {
		callsSite = new ArrayList<String>();
		instantiatedClass = new HashSet<JavaClass>();
	}	

	public String getQualifiedName() {
		return qualifiedName;
	}

	public void setQualifiedName(String qualifiedName) {
		this.qualifiedName = qualifiedName;
	}

	public String getShortSignature() {
		return shortSignature;
	}

	public void setShortSignature(String shortSignature) {
		this.shortSignature = shortSignature;
	}
	
	public List<String> getCallsSite() {
		return callsSite;
	}

	public void setCallsSite(List<String> callsSite) {
		this.callsSite = callsSite;
	}

	public Set<JavaClass> getInstantiatedClass() {
		return instantiatedClass;
	}

	public void setInstantiatedClass(Set<JavaClass> instantiatedClass) {
		this.instantiatedClass = instantiatedClass;
	}

	public String isInternString() {
		if (isIntern)
			return "true";
		else
			return "false";
	}

	public boolean isTestMethod() {
		return isTestMethod;
	}

	public void setTestMethod(boolean isTestMethod) {
		this.isTestMethod = isTestMethod;
	}




	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((callsSite == null) ? 0 : callsSite.hashCode());
		result = prime * result + (isIntern ? 1231 : 1237);
		result = prime * result + (isTestMethod ? 1231 : 1237);
		result = prime * result
				+ ((qualifiedName == null) ? 0 : qualifiedName.hashCode());
		result = prime * result
				+ ((shortSignature == null) ? 0 : shortSignature.hashCode());
		//System.out.println("Method "+result);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JavaMethod other = (JavaMethod) obj;
		if (callsSite == null) {
			if (other.callsSite != null)
				return false;
		} else if (!callsSite.equals(other.callsSite))
			return false;
		if (isIntern != other.isIntern)
			return false;
		if (isTestMethod != other.isTestMethod)
			return false;
		if (qualifiedName == null) {
			if (other.qualifiedName != null)
				return false;
		} else if (!qualifiedName.equals(other.qualifiedName))
			return false;
		if (shortSignature == null) {
			if (other.shortSignature != null)
				return false;
		} else if (!shortSignature.equals(other.shortSignature))
			return false;
		return true;
	}


}
