package fr.labri.rta.junit.test.model;

import org.junit.Test;

public class A {
	

	
	public A(){
		
	}
	
	public void foo(){
		AbstractClass ac = new AbstractClass("toto") {
			@Override
			public void toto() {
				Util u = new Util();
				u.function();
			}
		};
	}
	
}
