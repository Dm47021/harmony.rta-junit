package fr.labri;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ClassTest2 {

	@Test
	public void test3() {
		D d = new E("test");
		d.finalMethod();
		E.staticMethod();
	}
	
	@Test
	public void test4() {
		D d = new E("test");
		d.finalMethod();
		E.staticMethod();
	}
	
	@Test
	public void test5() {
		Interface i = new Interface() {
			
			@Override
			public void itf2() {
				D d = new D();
				d.finalMethod();
			}
			
			@Override
			public void itf1() {
				itf2();
			}
		};
		
	}
	
}
