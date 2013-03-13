package fr.labri.harmony.rta.junit.main;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fr.labri.harmony.Differ;

public class Runner {
	static String DEFAULT_VM = System.getProperty("runner.testvm", "java");
	static String DEV_NULL =  System.getProperty("runner.null", "/dev/null");

	static List<String> DEFAULT_VM_ARGS = Arrays
			.asList(new String[] { "-XX:+TraceByteCodes" });

	static int spawnVM(String className, List<String> args, File output)
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
		Process p = pb.start();
		return p.waitFor();
	}

	static File runTwoTrace(String className, List<String> args,
			String[] patterns) {
		int r = 1;
		File out = null, outtmp = null;
		FileReader read = null, readtmp = null;
		try {
			out = File.createTempFile("trace", ".log");
			outtmp = File.createTempFile("trace", null);
			r = spawnVM(className, args, out);
			int rr = spawnVM(className, args, outtmp);
			if (r == rr && r == 0) {
				if (new Differ(read = new FileReader(out),
						readtmp = new FileReader(outtmp), patterns).diffTrace()) {
					return out;
				}
				r = 1;
			}
			return null;
		} catch (IOException | InterruptedException e) {
			return null;
		} finally {
			if (read != null)
				try {
					read.close();
				} catch (IOException e) {
				}
			if (r != 0)
				out.delete();
			if (readtmp != null)
				try {
					read.close();
				} catch (IOException e) {
				}
			if (outtmp != null)
				outtmp.delete();
		}
	}
}