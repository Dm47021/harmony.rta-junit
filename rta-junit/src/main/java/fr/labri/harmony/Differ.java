package fr.labri.harmony;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

public class Differ {
	private static final String EOL = "";

	final static int _skipLines = Integer.parseInt(System.getProperty(
			"differ.skipLines", "2"));
	final static int _stripLines = Integer.parseInt(System.getProperty(
			"differ.stripLines", "9"));
	boolean _exlcude = Boolean.parseBoolean(System.getProperty(
			"differ.exclude", "false"));

	MyReader _left, _right;
	String _lcontext, _rcontext;

	ArrayList<Pattern> _patterns = new ArrayList<>();

	public Differ(Reader left, Reader right) {
		_left = new MyReader(left);
		_right = new MyReader(right);
	}

	public Differ(Reader left, Reader right, String[] patterns) {
		this(left, right);
		addPatterns(patterns);
	}

	void addPatterns(String[] patterns) {
		for (String p : patterns)
			addPattern(p);
	}

	void addPattern(String pattern) {
		_patterns.add(Pattern.compile(pattern));
	}

	public static void main(String[] args) throws IOException {
//		 if (true)
//		 args = new String[] { "/tmp/trace 2/trace1.log",
//		 "/tmp/trace 2/trace2.log", "." };

		if (args.length < 2) {
			System.err.println("Syntax: <traceFile1> <traceFile2> [patterns*]");
			System.err.println("Properties :");
			printProperty(System.err, "differ.skipLines", "Skip <n> first lines", "2");
			printProperty(System.err, "differ.exclude", "Consider exlclude pattern instead of include pattern", "false");
		} else {
			Differ differ = new Differ(openFile(args[0]), openFile(args[1]),
					(args.length > 2) ? Arrays
							.copyOfRange(args, 2, args.length)
							: new String[] { "." });
			boolean same = false;
			try {
				same = differ.diffTrace();
			} catch (Exception e) {	}
			if (same) {
				System.out.println("Trace are the same");
			} else {
				System.out.println(String.format(
						"Trace differs at lines %d %d%n\t%s%n\t%s", differ._left._lines,
						differ._right._lines, differ._lcontext, differ._rcontext));
			}
			System.exit(same ? 0 : 1);
		}
	}

	static void printProperty(OutputStream out, String prop, String desc, String dflt) {
		System.err.println(String.format("\t%s=%s: %s", prop, System.getProperty(prop, dflt), desc));
	}

	static Reader openFile(String f) throws FileNotFoundException {
		if (f.equals("-"))
			return new InputStreamReader(System.in);
		return new FileReader(new File(f));
	}

	private String diffLine() throws IOException {
		String l, r;
		try {
			l = _left.readLine();
		} catch (EOFException e) {
			try {
				r = _right.readLine();
				throw new IOException("Left is shorter than right");
			} catch (EOFException ee) {
				throw ee;
			}
		}
		try {
			r = _right.readLine();
		} catch (EOFException ee) {
			throw new IOException("Right is shorter than left");
		}
		return l.regionMatches(_stripLines, r, _stripLines, l.length() - _stripLines) ? l : null;
	}

	private boolean diffMethodContent() throws IOException {
		String l;
		do {
			l = diffLine();
			if (l == null)
				return false;
		} while (!l.equals(EOL));

		return true;
	}

	private void skipMethod(MyReader r) throws IOException {
		try {
			while (!r.readLine().equals(EOL))
				;
		} catch (EOFException e) {
		}
	}

	private String sync(MyReader r) throws IOException {
		String l = r.readLine();
		while (excludedMethod(l)) {
			skipMethod(r);
			l = r.readLine();
		}
		return l;
	}

	private boolean excludedMethod(String l) {
		for (Pattern p : _patterns) {
			if (p.matcher(l).find()) {
				return _exlcude;
			}
		}
		return !_exlcude;
	}

	public boolean diffTrace() throws IOException {
		_left.skipLines(_skipLines);
		_right.skipLines(_skipLines);

		while (true) {
			try {
				_lcontext = sync(_left);
			} catch (EOFException e) {
				try {
					sync(_right);
					return false;
				} catch (EOFException ee) {
					return true;
				}
			}
			try {
				_rcontext = sync(_right);
			} catch (EOFException e) {
				return false;
			}
			try {
				if (!(_lcontext.equals(_rcontext) && diffMethodContent()))
					return false;
			} catch (EOFException e) {
				return true;
			}
		}
	}

	final class MyReader {
		long _lines;
		BufferedReader _reader;

		MyReader(Reader in) {
			_reader = new BufferedReader(in);
		}

		String readLine() throws IOException {
			String line = _reader.readLine();
			if (line == null)
				throw new EOFException();
			_lines++;
			int p = line.indexOf(']');

			if (p >= 0)
				return line.substring(p + 1);
			return line;
		}

		void skipLines(int l) {
			while (l-- > 0)
				try {
					readLine();
				} catch (Exception e) {
				}
		}

		long getLines() {
			return _lines;
		}

		void close() throws IOException {
			_reader.close();
		}
	}
}
