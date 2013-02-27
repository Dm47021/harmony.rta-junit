package fr.labri.harmony.rta.junit.jdt;

/*
 * Copyright 2011 Jean-Rémy Falleri
 * 
 * This file is part of Praxis.
 * Praxis is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Praxis is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Praxis.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.io.File;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;

import fr.labri.harmony.rta.junit.HashElement;
import fr.labri.seutils.java.JavaFileUtils;

public class JDTGeneratorRTA {

	private String localPath;

	private Map<String, String> hashes;

	public Map<String, String> getHashes() {
		return hashes;
	}

	private Set<String> modifiedUrls = new HashSet<String>();

	private String[] classPath;

	private String[] sourcePath;

	public JDTGeneratorRTA(String localPath) {
		this.localPath = localPath;
		this.hashes = new HashMap<String, String>();
		JDTVisitorRTA.classes = new HashMap<String, JavaClass>();
		JDTVisitorRTA.method_inheritance = new HashMap<String, Set<String>>();
		JDTVisitorRTA.methods = new HashMap<String, JavaMethod>();
		JDTVisitorRTA.currentFile = null;
		JDTVisitorRTA.files = new HashSet<JavaFile>();
	}


	private boolean generate(String file) {

		if (!this.modifiedUrls.contains(file) && !this.modifiedUrls.isEmpty())
			return false;

		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setEnvironment(classPath, sourcePath, null, true);
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true);
		parser.setStatementsRecovery(true);
		parser.setIgnoreMethodBodies(false);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);

		Map<String, String> pOptions = JavaCore.getOptions();
		pOptions.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_7);
		pOptions.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM,
				JavaCore.VERSION_1_7);
		pOptions.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_7);
		parser.setCompilerOptions(pOptions);

		JDTRequestorRTA req = null;
		String shortPath = file.substring(localPath.length());
		if (!shortPath.startsWith("/"))
			shortPath = "/" + shortPath;

		JavaFile jf = new JavaFile(file, shortPath, null);
		JDTVisitorRTA.currentFile = jf;

		if (file.contains("/test/") || file.contains("/tests/")
				|| file.contains("/test-sources/")
				|| file.contains("/testsrc/"))
			req = new JDTRequestorRTA(file, hashes, true);
		else
			req = new JDTRequestorRTA(file, hashes, false);
		parser.createASTs(new String[] { file }, null, new String[] {}, req,
				null);

		JavaFile j = new JavaFile(JDTVisitorRTA.currentFile.getFullPath(),
				JDTVisitorRTA.currentFile.getShortPath(),
				JDTVisitorRTA.currentFile.getEnclosedClass());
		JDTVisitorRTA.files.add(j);

		return true;
	}

	public boolean handleFile(String file) {
		return file.endsWith(".java");
	}

	public void scanDirectory() {

		Set<File> javaFiles = JavaFileUtils.searchJavaSourceFiles(localPath);
		for (File file : javaFiles) {
			generate(file.getAbsoluteFile().getAbsolutePath());
		}
	
	}

	public Map<HashElement, Set<HashElement>> computeClosure() {

		Map<HashElement, Set<HashElement>> closure = new HashMap<HashElement, Set<HashElement>>();

		ComputeRTA r = new ComputeRTA();
		Set<JavaClass> classes = new HashSet<JavaClass>(
				JDTVisitorRTA.classes.values());

		Set<JavaMethod> methods = new HashSet<JavaMethod>(
				JDTVisitorRTA.methods.values());

		System.out.println("Start computing transitive closure with "
				+ JDTVisitorRTA.files.size() + " files, " + classes.size()
				+ " classes and " + methods.size() + " methods");

		// Fix inheritence
		long t1 = System.currentTimeMillis();
		for (int i = 0; i < 8; i++) {
			for (JavaClass jc : classes) {
				for (JavaClass par : jc.getParentClass()) {
					for (JavaMethod jm : par.getMethods()) {
						if (jm.isInternString().equals("false")) {
							if (jc.hasSignature(jm.getShortSignature()) == null) {
								jc.getMethods().add(jm);
							}
						}
					}

					for (JavaClass inst : par.getClassPublicFieldInstancied()) {
						for (JavaMethod jm : jc.getMethods()) {
							jm.getInstantiatedClass().add(inst);
						}
						jc.getClassPublicFieldInstancied().add(inst);
					}
				}
			}
		}
		long t2 = System.currentTimeMillis();

		t1 = System.currentTimeMillis();

		for (JavaMethod jm : methods) {
			if (jm.isTestMethod()) {
				Set<String> elts = r.computeRTA(jm, classes, methods);
				HashElement test = new HashElement(jm.getQualifiedName(),
						hashes.get(jm.getQualifiedName()));
				closure.put(test, new HashSet<HashElement>());
				for (String elt : elts) {
					String hash = "";
					if (hashes.containsKey(elt))
						hash = hashes.get(elt);
					closure.get(test).add(new HashElement(elt, hash));
				}
			}
		}

		t2 = System.currentTimeMillis();

		System.out.println("Closure computed in " + (t2 - t1) + " milli");
		
		return closure;
	}

	public void updateConfiguration() {

		Set<File> jars = JavaFileUtils.searchJars(localPath);
		Set<File> javaSourceFolders = JavaFileUtils
				.searchJavaSourceFolders(localPath);
		Set<File> javaClassFolders = JavaFileUtils
				.searchJavaClassFolders(localPath);


		this.classPath = new String[jars.size() + javaClassFolders.size()];
		int i = 0;
		for (File jar : jars)
			this.classPath[i++] = jar.getAbsolutePath();
		for (File folder : javaClassFolders)
			this.classPath[i++] = folder.getAbsolutePath();

		this.sourcePath = new String[javaSourceFolders.size()];
		i = 0;
		for (File folder : javaSourceFolders) {
			this.sourcePath[i++] = folder.getAbsolutePath();
		}
	}

}
