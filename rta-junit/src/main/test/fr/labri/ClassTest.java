package fr.labri;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

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
		//a.foo();
		GenericClass<Integer> integerBox = new GenericClass<>();
		integerBox.get();
		integerBox.set(10);
	}
	
	@Test
	public void test2() {
		a.foo();
		GenericClass<String> integerBox = new GenericClass<>();
		integerBox.get();
		integerBox.set("toto");
	}

	
}
