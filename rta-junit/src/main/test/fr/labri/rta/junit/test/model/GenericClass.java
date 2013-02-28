package fr.labri.rta.junit.test.model;

import java.util.Collection;


public class GenericClass<T> {
	// T stands for "Type"
	private T t;
	
	public GenericClass(){
		
	}
	
	public void set(T t) { this.t = t; }
	public T get() { return t; }
	
	public static <T> void fromArrayToCollection(T[] a, Collection<T> c) {
	    for (T o : a) {
	        c.add(o); // Correct
	    }
	}
}


