package fr.labri;


public class GenericClass<T> {
	// T stands for "Type"
	private T t;
	
	public GenericClass(){
		
	}
	
	public void set(T t) { this.t = t; }
	public T get() { return t; }
}


