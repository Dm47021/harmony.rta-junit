package fr.labri.harmony.rta.junit.jdt;


import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class JavaFile implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public JavaFile(){

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((fullPath == null) ? 0 : fullPath.hashCode());
		result = prime * result
				+ ((shortPath == null) ? 0 : shortPath.hashCode());
		//		if(this.enclosedClass!=null)
		//			System.out.println("JavaFile "+fullPath+" "+this.enclosedClass.getQualifiedName());
		//System.out.println("JavaFile "+result);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		//System.out.println("JavaFile equals yoooo");
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JavaFile other = (JavaFile) obj;
		if (fullPath == null) {
			if (other.fullPath != null)
				return false;
		} else if (!fullPath.equals(other.fullPath))
			return false;
		if (shortPath == null) {
			if (other.shortPath != null)
				return false;
		} else if (!shortPath.equals(other.shortPath))
			return false;
		return true;
	}

	private String fullPath;

	//Path on the VCS
	private String shortPath;

	private Set<JavaClass> enclosedClass;

	public JavaFile(String fullPath, String shortPath, Set<JavaClass> enclosedClass) {
		super();
		this.fullPath = fullPath;
		this.shortPath = shortPath;
		if(enclosedClass==null)
			this.enclosedClass = new HashSet<JavaClass>();
		else
			this.enclosedClass = new HashSet<JavaClass>(enclosedClass);
	}

	public String getFullPath() {
		return fullPath;
	}

	public void setFullPath(String fullPath) {
		this.fullPath = fullPath;
	}

	public String getShortPath() {
		return shortPath;
	}

	public void setShortPath(String shortPath) {
		this.shortPath = shortPath;
	}

	public Set<JavaClass> getEnclosedClass() {
		return enclosedClass;
	}

	public void setEnclosedClass(Set<JavaClass> enclosedClass) {
		this.enclosedClass = enclosedClass;
	}

	public boolean hasMethodSignature(String input) {
		for(JavaClass jc : this.enclosedClass)
			for(JavaMethod jm : jc.getMethods()) {
				if(jm.getQualifiedName().equals(input)) {
					return true;
				}
			}
		return false;
	}

	public Set<String> getIdMethods(String input) {
		Set<String> meths = new HashSet<String>();
		for(JavaClass jc : this.enclosedClass)
			for(JavaMethod jm : jc.getMethods()) {
				meths.add(jm.getQualifiedName());
			}
		return meths;
	}
}
