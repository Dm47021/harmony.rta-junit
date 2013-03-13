package fr.labri.harmony.rta.junit.main;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fr.labri.harmony.Differ;

public class Runner {
	private static final boolean DEBUG = false;
	
	static String DEFAULT_VM = "/home/cedric/Documents/jdk1.7.0/fastdebug/bin/java";
	static String DEV_NULL =  System.getProperty("runner.null", "/dev/null");

	static List<String> DEFAULT_VM_ARGS = Arrays
			.asList(new String[] { "-XX:+TraceBytecodes" });

	static Process spawnVM(String className, List<String> args, File output)
			throws IOException, InterruptedException {

		ArrayList<String> pargs = new ArrayList<>();
		pargs.add(DEFAULT_VM);
		pargs.add("-cp");
		pargs.add(System.getProperty("java.class.path"));
		pargs.addAll(DEFAULT_VM_ARGS);
		pargs.add(className);
		if (args != null)
			pargs.addAll(args);
		
		ProcessBuilder pb = new ProcessBuilder(pargs).redirectError(
				new File(DEV_NULL)).redirectOutput(output);
		System.err.println(pb.command());
		return pb.start();
	}

	static File runTwoTrace(String className, List<String> args,
			String[] patterns) {
		int r = 1;
		File out = null, outtmp = null;
		FileReader read = null, readtmp = null;
		try {
			out = DEBUG ? new File("/tmp/trace2314127474039579051.log") :  File.createTempFile("trace", ".log");
			outtmp = DEBUG ? new File("/tmp/trace6268946964116096758.tmp") : File.createTempFile("trace", null);
			r = 0;
			Process p = spawnVM(className, args, out);
			Process pp = spawnVM(className, args, outtmp);
			if (((r = p.waitFor()) == pp.waitFor()) && r == 0) {
				System.out.println("The same ??");
				r = 1;
				if (new Differ(read = new FileReader(out),
						readtmp = new FileReader(outtmp), patterns).diffTrace()) {
					r = 0;
					return out;
				} else {
					System.out.println("Differents");
				}
			}
			else System.out.println("Not same exit");
			return null;
		} catch (IOException | InterruptedException e) {
			return null;
		} finally {
			if (read != null)
				try {
					read.close();
				} catch (IOException e) {
				}
			if (r != 0 && !DEBUG)
				out.delete();
			if (readtmp != null)
				try {
					read.close();
				} catch (IOException e) {
				}
			if (outtmp != null  && !DEBUG)
				outtmp.delete();
		}
	}
}