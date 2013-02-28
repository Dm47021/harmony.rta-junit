package fr.labri.rta.junit.test;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.labri.rta.junit.test.model.A;
import fr.labri.rta.junit.test.model.B;
import fr.labri.rta.junit.test.model.C;
import fr.labri.rta.junit.test.model.D;
import fr.labri.rta.junit.test.model.E;
import fr.labri.rta.junit.test.model.G;
import fr.labri.rta.junit.test.model.GenericClass;
import fr.labri.rta.junit.test.model.H;
import fr.labri.rta.junit.test.model.I;
import fr.labri.rta.junit.test.model.Interface;

public class ClassTest {
	
	static A a;
	B b;
	
	@BeforeClass
	public static void setUpClass() {
		a = new A();
	}
	
	@Before
	public void setUp() {
		b = new B();
	}
	
	@Test
	public void test1() {
		a.foo();
	}
	
	@Test
	public void test2() {
		
		GenericClass<Integer> integerBox = new GenericClass<>();
		integerBox.get();
		integerBox.set(10);
	}
	
	@Test
	public void test3() {
		//a.foo();
		GenericClass<Integer> integerBox = new GenericClass<>();
		integerBox.get();
		integerBox.set(10);
	}
	
	@Test
	public void test4() {
		C c = new C();
		c.foo();
	}

	@Test
	public void test5() {
		D d = new E("test");
		d.finalMethod();
		E.staticMethod();
	}
	
	@Test
	public void test6() {
		D d = new E("test");
	}
	
	@Test
	public void test7() {
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
	
	@Test
	public void test8() {
		Object[] oa = new Object[100];
		Collection<Object> co = new ArrayList<Object>();

		// T inferred to be Object
		GenericClass.fromArrayToCollection(oa, co); 
	}
	
	@Test
	public void test9() {
		new G(10);
	}
	
	@Test
	public void test10() {
		new G();
	}
	
	@Test
	public void test11() {
		new G(9,8);
	}
	
	@Test
	public void test12() {
		H i = new I();
		i.bar();
	}
}
