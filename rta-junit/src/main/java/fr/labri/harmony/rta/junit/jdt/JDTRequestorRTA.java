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



import java.util.Map;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;


public class JDTRequestorRTA extends FileASTRequestor {

	
	private Map<String,String> hashes;
	
	private boolean isTestFile;
	
	public JDTRequestorRTA() {
		super(); 
	}
	
	public JDTRequestorRTA(String file, Map<String,String> hashes, boolean isTest) {

		this.hashes = hashes;
		this.isTestFile = isTest;
	}

	public void acceptAST(String sourceFilePath, CompilationUnit ast) {
		ast.accept(new JDTVisitorRTA(this.hashes,this.isTestFile));
	}


}
