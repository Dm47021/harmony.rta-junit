package fr.labri.harmony.rta.junit.jdt;


import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

//Supposed to contain static blocks of a class. 
public class JavaClass implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String qualifiedName;

	// Contains the hash of the static blocks
	private Set<String> staticBlocks = new HashSet<String>();

	private Set<JavaClass> childrenClass = new HashSet<JavaClass>();

	private Set<JavaClass> parentClass = new HashSet<JavaClass>();

	private Set<JavaMethod> methods = new HashSet<JavaMethod>();

	private Set<JavaClass> classPublicFieldInstanciated = new HashSet<JavaClass>();

	private Set<JavaClass> classPrivateFieldInstanciated = new HashSet<JavaClass>();

	private JavaFile javaFile;

	private boolean isInterface;

	public boolean isInterface() {
		return isInterface;
	}

	public void setInterface(boolean isInterface) {
		this.isInterface = isInterface;
	}

	public JavaClass(){

	}

	public JavaFile getJavaFile() {
		return javaFile;
	}

	public void setJavaFile(JavaFile javaFile) {
		this.javaFile = javaFile;
	}



	public Set<JavaClass> getClassPublicFieldInstancied() {
		return classPublicFieldInstanciated;
	}

	public void setClassPublicFieldInstanciend(
			Set<JavaClass> classPublicFieldInstanciend) {
		this.classPublicFieldInstanciated = classPublicFieldInstanciend;
	}

	public Set<JavaClass> getClassPrivateFieldInstancied() {
		return classPrivateFieldInstanciated;
	}

	public void setClassPrivateFieldInstancied(
			Set<JavaClass> classPrivateFieldInstancied) {
		this.classPrivateFieldInstanciated = classPrivateFieldInstancied;
	}

	public void setChildrenClass(Set<JavaClass> childrenClass) {
		this.childrenClass = childrenClass;
	}

	public void setMethods(Set<JavaMethod> methods) {
		this.methods = methods;
	}

	public Set<JavaClass> getParentClass() {
		return parentClass;
	}

	public void setParentClass(Set<JavaClass> parentClass) {
		this.parentClass = parentClass;
	}

	public String getQualifiedName() {
		return qualifiedName;
	}

	public void setQualifiedName(String qualifiedName) {
		this.qualifiedName = qualifiedName;
	}

	public Set<String> getStaticBlocks() {
		return staticBlocks;
	}

	public void setStaticBlocks(Set<String> staticBlocks) {
		this.staticBlocks = staticBlocks;
	}

	public Set<JavaClass> getChildrenClass() {
		return childrenClass;
	}

	public void setChildrenClass(HashSet<JavaClass> childrenClass) {
		this.childrenClass = childrenClass;
	}

	public Set<JavaMethod> getMethods() {
		return methods;
	}

	public void setMethods(HashSet<JavaMethod> methods) {
		this.methods = methods;
	}

	public JavaMethod hasSignature(String input) {
		
		for (JavaMethod method : methods) {
			//System.out.println("\t\t"+input+" "+method.getShortSignature());
			if(method.getShortSignature()!=null)
				if(!method.getShortSignature().isEmpty())
					if (method.getShortSignature().equals(input)) {
						
						return method;
					}
		}
		return null;
	}

	public void findSignatureInHierarchy(String methodSignature,
			Set<JavaClass> classCreated, Set<JavaMethod> reachables) {
		LinkedList<JavaClass> queue = new LinkedList<JavaClass>();
		LinkedList<JavaClass> visited = new LinkedList<JavaClass>();
		queue.add(this);
		queue.addAll(childrenClass);
//		System.out.println("----------------------");
//		System.out.println("Looking for "+methodSignature+" in hierarchy of "+this.qualifiedName);
//		System.out.println("Class Created");
//		for(JavaClass j : classCreated) {
//			System.out.println("\t * "+j.getQualifiedName());
//		}
//		System.out.println(queue.size());
//		System.out.println(childrenClass.size());
		while (!queue.isEmpty()) {
			JavaClass c = queue.removeFirst();
		
			if (classCreated.contains(c)) {
			
				JavaMethod jm = c.hasSignature(methodSignature);
				if (jm != null && !reachables.contains(jm)) {
					reachables.add(jm);
				}
			}

			visited.add(c);
			for (JavaClass child : c.getChildrenClass())
				if (!visited.contains(child)) {			
					queue.add(child);
				}
		}
		
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((javaFile == null) ? 0 : javaFile.hashCode());
		result = prime * result
				+ ((qualifiedName == null) ? 0 : qualifiedName.hashCode());
		//		if(this.javaFile!=null)
		//			System.out.println("Class "+this.qualifiedName+" "+this.javaFile.getFullPath());
		//		System.out.println("Class "+result);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		//System.out.println("JavaClass equals yoooo");
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		JavaClass other = (JavaClass) obj;
		if (javaFile == null) {
			if (other.javaFile != null) {
				return false;
			}
		} else if (!javaFile.equals(other.javaFile)) {
			return false;
		}
		if (qualifiedName == null) {
			if (other.qualifiedName != null) {
				return false;
			}
		} else if (!qualifiedName.equals(other.qualifiedName)) {
			return false;
		}
		return true;
	}




}
