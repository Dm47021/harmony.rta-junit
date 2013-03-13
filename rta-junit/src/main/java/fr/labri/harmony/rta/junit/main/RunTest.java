package fr.labri.harmony.rta.junit.main;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;

public class RunTest {
	
		public static void main(String[] args) throws ClassNotFoundException, FileNotFoundException {
			System.setOut(new PrintStream(nullStream));
			System.setErr(new PrintStream(nullStream));
			if(args.length == 0)
				System.exit(1);
			Request req;
			if (args.length == 1)
				req = Request.aClass(Class.forName(args[0]));
			else
				req = Request.method(Class.forName(args[0]), args[1]);
			
			JUnitCore core = new JUnitCore();
			Result res = core.run(req);
			System.out.println(res.wasSuccessful());
			System.exit(res.wasSuccessful() ? 0 : 1);
		}
		
		final static OutputStream nullStream = new OutputStream() {
			@Override
			public void write(int b) throws IOException {
			}
		};
}
