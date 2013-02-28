package fr.labri.harmony.rta.junit.jdt;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import java.util.Set;


import fr.labri.harmony.utils.Pair;


public class ComputeRTA {

	public static Map<JavaClass,Pair<Set<JavaMethod>,Set<JavaClass>>> index = new HashMap<JavaClass, Pair<Set<JavaMethod>,Set<JavaClass>>>();

	public Set<String> computeRTA(JavaMethod entryPoint, Set<JavaClass> classes, Set<JavaMethod> methods) {
		
		Set<JavaMethod> reachableImplems = new HashSet<>();
		Set<JavaMethod> knownImplem = new HashSet<>();

		LinkedList<String> callSites = new LinkedList<>();
		Set<JavaClass> classCreated = new HashSet<>();
		
		
		classCreated.add(entryPoint.getClassName());
		if(index.containsKey(entryPoint.getClassName())) {
			reachableImplems.addAll(index.get(entryPoint.getClassName()).getFirst());
			classCreated.addAll(index.get(entryPoint.getClassName()).getSecond());
		}
		else {

			for(JavaMethod jm : entryPoint.getClassName().getMethods()) {
				if(jm.isBeforeClassMethod() || jm.isBeforeMethod()) {			
					callSites.addAll(jm.getCallsSite());
					classCreated.addAll(jm.getInstantiatedClass());
				}
			}

			//On chope les methods reachables et classes instanciées depuis les before et beforeclass
			while(!callSites.isEmpty()) {
				String classSite = callSites.removeFirst();
			
				String tk[] = classSite.split("\\:\\:");
				//Method or field ?
				if(tk.length==3) {
					String isIntern = tk[2];
					for(JavaClass c : classes) {
						if(c.getQualifiedName().equals(tk[0])) {
							if(isIntern.equals("true")) {
								JavaMethod jm = c.hasSignature(tk[1]);
								if(jm!=null) {
									if(!knownImplem.contains(jm)) {
										callSites.add(jm.getClassName().getQualifiedName()+"::"+jm.getShortSignature()+"::"+jm.isInternString());
										callSites.addAll(jm.getCallsSite());
										classCreated.addAll(jm.getInstantiatedClass());
										knownImplem.add(jm);
										reachableImplems.add(jm);								
									}
								}
							}
							else {
								c.findSignatureInHierarchy(tk[1],classCreated,reachableImplems);
								for(JavaMethod jvm : reachableImplems) {
									if(!knownImplem.contains(jvm)) {
										callSites.add(jvm.getClassName().getQualifiedName()+"::"+jvm.getShortSignature()+"::"+jvm.isInternString());
										callSites.addAll(jvm.getCallsSite());
										classCreated.addAll(jvm.getInstantiatedClass());
										knownImplem.add(jvm);	
									}

								}
							}
						}
					}
				}
				else {
					JavaMethod jc = new JavaMethod();
					jc.setQualifiedName(tk[0]);
					reachableImplems.add(jc);
					knownImplem.add(jc);
				}
			}
			Set<JavaMethod> jmSave = new HashSet<JavaMethod>(knownImplem);
			Set<JavaClass> jcSave = new HashSet<JavaClass>(classCreated);
			index.put(entryPoint.getClassName(), new Pair<Set<JavaMethod>, Set<JavaClass>>(jmSave, jcSave));
		}

		//Init
		classCreated.addAll(entryPoint.getInstantiatedClass());
		Set<JavaMethod> newReachableImplems = new HashSet<>();
		callSites.clear();
		callSites.addAll(entryPoint.getCallsSite());

		boolean directCallDone = false;

		while(!callSites.isEmpty()) {
			String classSite = callSites.removeFirst();
	
			//On a dépilé tous les calls directs de entrypoint
			if(!entryPoint.getCallsSite().contains(classSite) && !directCallDone) {
				entryPoint.getDirectCalls().clear();
				entryPoint.getDirectCalls().addAll(newReachableImplems);
				directCallDone = true;
			}

			String tk[] = classSite.split("\\:\\:");
			//Method or field ?
			if(tk.length==3) {
				String isIntern = tk[2];
				for(JavaClass c : classes) {
					if(c.getQualifiedName().equals(tk[0])) {
						if(isIntern.equals("true")) {
							JavaMethod jm = c.hasSignature(tk[1]);
							if(jm!=null) {
								if(!knownImplem.contains(jm)) {
									callSites.add(jm.getClassName().getQualifiedName()+"::"+jm.getShortSignature()+"::"+jm.isInternString());
									callSites.addAll(jm.getCallsSite());
									classCreated.addAll(jm.getInstantiatedClass());
									knownImplem.add(jm);
									newReachableImplems.add(jm);		
								}
							}

						}
						else {
							if(tk[0].contains("$")) {
								for(JavaClass internClasses : c.getJavaFile().getEnclosedClass()) {
									if(internClasses.getQualifiedName().equals(tk[0])){
										classCreated.add(internClasses);
									}
								}
							}
							
							c.findSignatureInHierarchy(tk[1],classCreated,newReachableImplems);
							for(JavaMethod jvm : newReachableImplems) {
								if(!knownImplem.contains(jvm)) {
									callSites.add(jvm.getClassName().getQualifiedName()+"::"+jvm.getShortSignature()+"::"+jvm.isInternString());
									callSites.addAll(jvm.getCallsSite());
									classCreated.addAll(jvm.getInstantiatedClass());
									knownImplem.add(jvm);	
								}
							}
						}
					}
				}
			}
			else {
				JavaMethod jc = new JavaMethod();
				jc.setQualifiedName(tk[0]);
				newReachableImplems.add(jc);
				knownImplem.add(jc);
			}
		}

		Set<String> toReturn = new HashSet<String>();

		newReachableImplems.addAll(reachableImplems);
		
		for(JavaMethod jm : newReachableImplems) {
			toReturn.add(jm.getQualifiedName());	
		}
		
		List<String> methodNames = new ArrayList<String>(toReturn);
		Collections.sort(methodNames);


		return toReturn;
	}

	public void visit () {

	}
}
