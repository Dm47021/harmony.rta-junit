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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.internal.corext.dom.Bindings;



import fr.labri.harmony.utils.MD5Generator;



public class JDTVisitorRTA extends ASTVisitor {

	public static Map<String,Set<String>> method_inheritance = new HashMap<String, Set<String>>();

	public static Set<JavaFile> files = new  HashSet<JavaFile>();

	public static Map<String,JavaClass> classes = new  HashMap<String, JavaClass>();

	public static Map<String,JavaMethod> methods = new  HashMap<String, JavaMethod>();

	public static JavaFile currentFile;

	private Map<String,String> hashes;

	private ArrayList<String> classCreated = new ArrayList<String>();

	private Set<String> fieldClassCreated = new HashSet<String>();

	private JavaMethod currentJavaMethod;

	private int cptAnonymous =1;

	private int cptStaticBlock =1;

	private int previousHashCode=-1;

	private int currentTypeHashcode=0;

	private JavaClass currentClass;

	private boolean superInvocation=false;

	private Map<Integer,JavaClass> currentClasses = new HashMap<Integer, JavaClass>();

	private boolean isTestFile;

//	private boolean isTestMethod=false;
//
//	private boolean isBeforeMethod=false;
//
//	private boolean isBeforeClassMethod=false;

	Set<TypeDeclaration> toVisit = new HashSet<TypeDeclaration>();

	public JDTVisitorRTA(Map<String,String> hashes, boolean isTestFile) {
		super();
		this.hashes = hashes;
		this.isTestFile = isTestFile;
	}

	public boolean visit(EnumDeclaration td)  {
		ITypeBinding tb = td.resolveBinding();


		if(tb!=null) {

			String id = tb.getQualifiedName();
			currentClass = getJavaClass(id);
			if(currentTypeHashcode!=-1)
				previousHashCode=currentTypeHashcode;
			currentTypeHashcode = td.toString().hashCode();
			if(!currentClasses.containsKey(currentTypeHashcode))
				currentClasses.put(currentTypeHashcode, currentClass);

			currentClass.getChildrenClass().clear();
			currentClass.getParentClass().clear();
			currentClass.getClassPrivateFieldInstancied().clear();
			currentClass.getClassPublicFieldInstancied().clear();
			currentClass.getMethods().clear();

		}


		if(tb.getSuperclass()!=null && !tb.getSuperclass().getQualifiedName().equals("java.lang.Object")) {
			JavaClass jc = getJavaClass(tb.getSuperclass().getQualifiedName());
			if(jc!=null ){
				jc.getChildrenClass().add(currentClass);
				currentClass.getParentClass().add(jc);
			}

		}
		for(ITypeBinding t : tb.getInterfaces()){
			JavaClass jc = getJavaClass(t.getQualifiedName());
			jc.getChildrenClass().add(currentClass);
			currentClass.getParentClass().add(jc);
		}

		return true;
	}

	public boolean visit(TypeDeclaration td)  {

		ITypeBinding tb = td.resolveBinding();

		if(tb!=null) {

			String id = tb.getQualifiedName();
			
			currentClass = getJavaClass(id);
		
			if(currentTypeHashcode!=-1)
				previousHashCode=currentTypeHashcode;
			currentTypeHashcode = td.toString().hashCode();
			if(!currentClasses.containsKey(currentTypeHashcode))
				currentClasses.put(currentTypeHashcode, currentClass);

			//currentClass.getChildrenClass().clear();
			currentClass.getParentClass().clear();
			currentClass.getClassPublicFieldInstancied().clear();
			currentClass.getClassPrivateFieldInstancied().clear();
			currentClass.getMethods().clear();
			if(tb.isInterface())
				currentClass.setInterface(true);
			else
				currentClass.setInterface(false);
		}


		if(tb.getSuperclass()!=null && !tb.getSuperclass().getQualifiedName().equals("java.lang.Object")) {
			JavaClass jc = getJavaClass(tb.getSuperclass().getQualifiedName());
			
			if(jc!=null ){
				jc.getChildrenClass().add(currentClass);
				currentClass.getParentClass().add(jc);
			}

		}
		for(ITypeBinding t : tb.getInterfaces()){
			JavaClass jc = getJavaClass(t.getQualifiedName());
			jc.getChildrenClass().add(currentClass);
			currentClass.getParentClass().add(jc);
		}

		if(isTestFile) {
			if(tb != null) {
				for(ITypeBinding t : Bindings.getAllSuperTypes(tb)) {
					for(IMethodBinding mb : t.getDeclaredMethods()) {
						//is it a test class ?
						if(mb != null) {
							for(IAnnotationBinding a : mb.getAnnotations()) {
								if(a.getName().toString().contains("Test")){
									//System.out.println(tb.getQualifiedName()+" inherits "+getMethodBindingId(mb));
									if(!method_inheritance.containsKey(tb.getQualifiedName()))
										method_inheritance.put(tb.getQualifiedName(), new HashSet<String>());
									method_inheritance.get(tb.getQualifiedName()).add(getMethodBindingId(mb));
								}
							}	
						}
					}
				}
			}
		}

		return true;
	}

	public boolean visit(ClassInstanceCreation cic) {

		IMethodBinding mb = cic.resolveConstructorBinding();

		if ( currentJavaMethod != null && mb != null) {	

			if(mb.getMethodDeclaration().getDeclaringClass().isAnonymous()) {
				if(mb.getMethodDeclaration().getDeclaringClass().getTypeDeclaration()!=null)
					if(mb.getMethodDeclaration().getDeclaringClass().getTypeDeclaration().getSuperclass()!=null) {
						String id = mb.getMethodDeclaration().getDeclaringClass().getTypeDeclaration().getSuperclass().getQualifiedName();
						String callSite = id+"::"+mb.getMethodDeclaration().getDeclaringClass().getTypeDeclaration().getSuperclass().getName()+"(";
						int i = 0;
						for(ITypeBinding tb : mb.getMethodDeclaration().getParameterTypes()) {
							callSite += tb.getTypeDeclaration().getQualifiedName();

							if ( i < mb.getParameterTypes().length - 1 )
								callSite += ",";

							i++;
						}
						callSite+="):void::true";
						//System.err.println(callSite);
						if(!currentJavaMethod.getCallsSite().contains(callSite))
							currentJavaMethod.getCallsSite().add(callSite);
					}
			}
			else {
				String signature = getShortDeclaration(mb);
				String type = getStaticType(mb);
				String inIntern = "false";
				if(Modifier.isStatic(mb.getModifiers()) || Modifier.isPrivate(mb.getModifiers()) || Modifier.isFinal(mb.getModifiers()) || mb.isConstructor())
					inIntern = "true";
				
				//System.err.println(mb.getDeclaringClass().getTypeDeclaration().getQualifiedName());
				if(!currentJavaMethod.getCallsSite().contains(type+"::"+signature+"::"+inIntern))
					currentJavaMethod.getCallsSite().add(type+"::"+signature+"::"+inIntern);
				if(!classCreated.contains(mb.getDeclaringClass().getTypeDeclaration().getQualifiedName()));
					classCreated.add(mb.getDeclaringClass().getTypeDeclaration().getQualifiedName());
			}
		}

		return true;
	}

	public boolean visit(MethodDeclaration md) {

		IMethodBinding b = md.resolveBinding(); 

		superInvocation = false;

		if(currentClass==null) {
			return false;
		}

		// Test required in case of default constructors that have null bindings
		if ( b != null) {

			String id = "";
			if(!b.getDeclaringClass().isAnonymous()) {
				id = getMethodBindingId(b);
			}
			else {
				id = getAnonymousMethodBindingId(b,currentClass);
			}

			currentJavaMethod = getJavaMethod(id);
			currentJavaMethod.setQualifiedName(id);
			currentJavaMethod.setShortSignature(getShortDeclaration(b));

			currentJavaMethod.setIntern(false);
			if(Modifier.isPrivate(b.getModifiers()) || Modifier.isStatic(b.getModifiers()) || Modifier.isFinal(b.getModifiers()) || b.isConstructor())
				currentJavaMethod.setIntern(true);

			if(b.isConstructor())
				currentJavaMethod.setConstructor(true);
			else
				currentJavaMethod.setConstructor(false);

			currentJavaMethod.setClassName(getJavaClass(b.getDeclaringClass().getQualifiedName()));

			if(isTestFile) {	
				if(md.getName().toString().startsWith("test")) {
					currentJavaMethod.setTestMethod(true);
				}
				if(md.resolveBinding() != null) {
					for(IAnnotationBinding a : md.resolveBinding().getAnnotations()) {
						if(a.getName().toString().startsWith("Test")){
							currentJavaMethod.setTestMethod(true);
						}
						else if(a.getName().toString().startsWith("BeforeClass")){
							currentJavaMethod.setBeforeClassMethod(true);
						}
						else if(a.getName().toString().startsWith("Before")){
							currentJavaMethod.setBeforeMethod(true);
						}
					}	
				}
			}

			currentJavaMethod.getDirectCalls().clear();
			currentJavaMethod.getCallsSite().clear();	
			currentJavaMethod.getInstantiatedClass().clear();

			if(md.getBody()==null)
				hashes.put(id,MD5Generator.md5(md.toString().trim()));
			else
				hashes.put(id,MD5Generator.md5(md.getBody().toString().trim()));
			//
			//			IMethodBinding mim = Bindings.findOverriddenMethod(b, true);
			//			if(mim!=null) 
			//				addAction(getMethodBindingId(mim),id);
		}
		else 
			return false;

		return true;
	}

	private JavaMethod getJavaMethod(String qualifiedName) {
		if(!methods.containsKey(qualifiedName)) {
			methods.put(qualifiedName, new JavaMethod());
			methods.get(qualifiedName).setQualifiedName(qualifiedName);
		}
		return methods.get(qualifiedName);
	}

	private JavaClass getJavaClass(String qualifiedName) {

		if(!classes.containsKey(qualifiedName)) {
			classes.put(qualifiedName, new JavaClass());
			classes.get(qualifiedName).setQualifiedName(qualifiedName);
			//System.out.println("Add class "+qualifiedName);
		}
		else {
			//System.out.println("Return class "+qualifiedName+" "+ classes.get(qualifiedName).getChildrenClass().size());
			
		}
		return classes.get(qualifiedName);
	}

	public boolean visit(EnumConstantDeclaration fd ) {
		if(fd.resolveVariable()!=null) {
			if(fd.resolveVariable().getDeclaringClass()!=null){
				String field = fd.resolveVariable().getDeclaringClass().getQualifiedName()+"."+fd.resolveVariable().getName();
				hashes.put(field,MD5Generator.md5(fd.toString()));
				//Put the field in the database
				currentJavaMethod = getJavaMethod(field);
				currentJavaMethod.setClassName(getJavaClass(fd.resolveVariable().getDeclaringClass().getQualifiedName()));
			}
		}
		return true;
	}

	public boolean visit(FieldDeclaration fd ) {

		VariableDeclarationFragment f = (VariableDeclarationFragment) fd.fragments().get(0);
		IVariableBinding b  = f.resolveBinding();
		if ( b != null && fd!=null && currentClass!=null) {
			if(b.getDeclaringClass().isAnonymous())
				return false;
			String id = getVariableBindingId(b);
			hashes.put(id,MD5Generator.md5(fd.toString()));
			//Put the field in the database
			currentJavaMethod = getJavaMethod(id);
			currentJavaMethod.setClassName(getJavaClass(b.getDeclaringClass().getTypeDeclaration().getQualifiedName()));
			//Si la variable de classe est instanciée
			if(f.getInitializer() instanceof ClassInstanceCreation) {
				ClassInstanceCreation cc = (ClassInstanceCreation)f.getInitializer();
				IMethodBinding mb = cc.resolveConstructorBinding();
				if(mb!=null) {
					String isIntern = "false";
					if(Modifier.isPrivate(b.getModifiers()))
						isIntern = "true";
					fieldClassCreated.add(mb.getDeclaringClass().getQualifiedName()+"::"+isIntern);
				}
			}



		}

		return true;
	}

	public boolean visit(MethodInvocation mi) {
		IMethodBinding mb = mi.resolveMethodBinding();

		if (currentJavaMethod!=null && mb != null) {	

			String signature = getShortDeclaration(mb);
			String type = getStaticType(mb);
			String isIntern = "false";
			if(Modifier.isStatic(mb.getModifiers()) || Modifier.isPrivate(mb.getModifiers()) || Modifier.isFinal(mb.getModifiers()) || mb.isConstructor())
				isIntern = "true";
			//System.out.println("sig "+signature);

			if(!currentJavaMethod.getCallsSite().contains(type+"::"+signature+"::"+isIntern))
				currentJavaMethod.getCallsSite().add(type+"::"+signature+"::"+isIntern);
		}

		return true;
	}

	public boolean visit(Initializer b) {
		superInvocation = false;

		if(currentClass==null) {
			return false;
		}

		// Test required in case of default constructors that have null bindings
		if ( b != null) {


			String id = currentClass.getQualifiedName()+".Block$"+cptStaticBlock;

			currentJavaMethod = getJavaMethod(id);
			currentJavaMethod.setQualifiedName(id);
			currentJavaMethod.setShortSignature("Block$"+cptStaticBlock);
			cptStaticBlock++;

			currentJavaMethod.setIntern(true);
			currentJavaMethod.setConstructor(false);
			currentJavaMethod.setStaticBlock(true);

			currentJavaMethod.setClassName(currentClass);

			currentJavaMethod.getDirectCalls().clear();
			currentJavaMethod.getCallsSite().clear();	
			currentJavaMethod.getInstantiatedClass().clear();

			if(b.getBody()==null)
				hashes.put(id,MD5Generator.md5(b.toString().trim()));
			else
				hashes.put(id,MD5Generator.md5(b.getBody().toString().trim()));
			//
			//			IMethodBinding mim = Bindings.findOverriddenMethod(b, true);
			//			if(mim!=null) 
			//				addAction(getMethodBindingId(mim),id);
		}
		else 
			return false;

		return true;

	}

	@Override
	public void postVisit(ASTNode node) {
		if(node instanceof MethodDeclaration || node instanceof Initializer) {

			if(currentJavaMethod!=null) {
				for(String created : classCreated) {
					currentJavaMethod.getInstantiatedClass().add(getJavaClass(created));
				}

				if(!superInvocation && currentJavaMethod.isConstructor()) {

					//Add a call site to the super-class constructor
					if(!currentClass.getParentClass().isEmpty()) {
						for(JavaClass jc : currentClass.getParentClass()) {
							if(!jc.isInterface()) {
								//System.err.println("Add "+jc.getQualifiedName()+"::"+jc.getQualifiedName().substring(jc.getQualifiedName().lastIndexOf(".")+1)+"():void::true");
								currentJavaMethod.getCallsSite().add(jc.getQualifiedName()+"::"+jc.getQualifiedName().substring(jc.getQualifiedName().lastIndexOf(".")+1)+"():void::true");	
							}
						}
					}
				}

				JavaMethod jm = new JavaMethod();
				jm.setClassName(currentJavaMethod.getClassName());
				jm.setIntern(currentJavaMethod.isIntern());
				jm.setQualifiedName(currentJavaMethod.getQualifiedName());
				jm.setShortSignature(currentJavaMethod.getShortSignature());
				jm.getCallsSite().addAll(currentJavaMethod.getCallsSite());
				jm.setTestMethod(currentJavaMethod.isTestMethod());
				jm.setBeforeMethod(currentJavaMethod.isBeforeMethod());
				jm.setBeforeClassMethod(currentJavaMethod.isBeforeClassMethod());
				jm.setStaticBlock(currentJavaMethod.isStaticBlock());

				jm.getInstantiatedClass().addAll(currentJavaMethod.getInstantiatedClass());


				methods.put(jm.getQualifiedName(),jm);
				if(!currentClass.getMethods().contains(jm))
					currentClass.getMethods().add(jm);


			}
			currentJavaMethod = null;
			classCreated = new ArrayList<>();
			superInvocation = false;
		}
		if(node instanceof FieldDeclaration) {
			if(currentJavaMethod!=null) {
				JavaMethod jm = new JavaMethod();
				jm.setClassName(currentJavaMethod.getClassName());
				jm.setIntern(true);
				jm.setQualifiedName(currentJavaMethod.getQualifiedName());
				jm.setShortSignature("");

				methods.put(jm.getQualifiedName(),jm);
				if(!currentClass.getMethods().contains(jm))
					currentClass.getMethods().add(jm);
				currentJavaMethod = null;
			}
			classCreated = new ArrayList<>();
		}
		if(node instanceof TypeDeclaration) {
			currentClass = currentClasses.get(((TypeDeclaration)node).toString().hashCode());	
			if(currentClass!=null) {	

				//Pour chaque méthode de test, on ajoute comme classe créée les fields de classes instanciées
				for(JavaMethod jm : currentClass.getMethods()) {
					//System.out.println(jm.getQualifiedName());
					for(String created : fieldClassCreated) {
						String tk[]= created.split("\\:\\:");
						jm.getInstantiatedClass().add(getJavaClass(tk[0]));	
						if(tk[1].equals("false")) 
							currentClass.getClassPrivateFieldInstancied().add(getJavaClass(tk[0]));
						else
							currentClass.getClassPublicFieldInstancied().add(getJavaClass(tk[0]));
					}
				}

				//Chaque méthode va appeler les static blocks si ils existent
				for(JavaMethod jm : currentClass.getMethods()) {

					if(jm.isStaticBlock()) {
						String cl = currentClass.getQualifiedName()+"::"+jm.getShortSignature()+"::true";

						for(JavaMethod jm2 : currentClass.getMethods()) {
							if(!jm2.isStaticBlock()) {
								jm2.getCallsSite().add(cl);
								//System.out.println("Add "+cl+" to "+jm2.getQualifiedName());
							}
						}
					}
				}

				currentFile.getEnclosedClass().add(currentClass);
				currentClass.setJavaFile(currentFile);
			}
			currentClass = currentClasses.get(previousHashCode);
		}
		if(node instanceof EnumDeclaration) {
			currentClass = currentClasses.get(((EnumDeclaration)node).toString().hashCode());	
			if(currentClass!=null) {	

				//Pour chaque méthode de test, on ajoute comme classe créée les fields de classes instanciées
				for(JavaMethod jm : currentClass.getMethods()) {
					for(String created : fieldClassCreated) {

						String tk[]= created.split("\\:\\:");
						jm.getInstantiatedClass().add(getJavaClass(tk[0]));	
					}
				}
				currentFile.getEnclosedClass().add(currentClass);
				currentClass.setJavaFile(currentFile);
			}
			currentClass = currentClasses.get(previousHashCode);
		}
		if(node instanceof AnonymousClassDeclaration) {
			currentClass = currentClasses.get(((AnonymousClassDeclaration)node).toString().hashCode());	
			if(currentClass!=null) {	
				if(((AnonymousClassDeclaration)node).resolveBinding()!=null) {
					
					if(((AnonymousClassDeclaration)node).resolveBinding().getDeclaringMethod()!=null) {
						String id = getMethodBindingId(((AnonymousClassDeclaration)node).resolveBinding().getDeclaringMethod());
						JavaMethod m = getJavaMethod(id);
						for(JavaMethod m2 : currentClass.getMethods()) {
							m.getCallsSite().add(currentClass.getQualifiedName()+"::"+m2.getShortSignature()+"::"+m2.isInternString());
						}
						for(JavaMethod jm : currentClass.getMethods()) {

							for(String created : fieldClassCreated) {
								String tk[]= created.split("\\:\\:");
								jm.getInstantiatedClass().add(getJavaClass(tk[0]));	
								if(tk[1].equals("false")) 
									currentClass.getClassPrivateFieldInstancied().add(getJavaClass(tk[0]));
								else
									currentClass.getClassPublicFieldInstancied().add(getJavaClass(tk[0]));
							}
						}
						//on se replace dans l'ancienne méthode
						currentJavaMethod = m;
					}
				}
				currentFile.getEnclosedClass().add(currentClass);
				currentClass.setJavaFile(currentFile);
			}
			currentClass = currentClasses.get(previousHashCode);
		}
	}

	public boolean visit(AnonymousClassDeclaration cd) {

		String id = currentClass.getQualifiedName()+"$"+cptAnonymous;

		cptAnonymous++;

		currentClass = getJavaClass(id);
		if(currentTypeHashcode!=-1)
			previousHashCode=currentTypeHashcode;
		currentTypeHashcode = cd.toString().hashCode();
		if(!currentClasses.containsKey(currentTypeHashcode))
			currentClasses.put(currentTypeHashcode, currentClass);

		currentClass.getChildrenClass().clear();
		currentClass.getParentClass().clear();
		currentClass.getClassPrivateFieldInstancied().clear();
		currentClass.getClassPublicFieldInstancied().clear();
		currentClass.getMethods().clear();

		return true;
	}

	public boolean visit(SuperMethodInvocation mi) {
		IMethodBinding mb = mi.resolveMethodBinding();

		if ( currentJavaMethod != null && mb != null) {	
			String signature = getShortDeclaration(mb);
			String type = getStaticType(mb);
			String isIntern = "false";
			if(Modifier.isStatic(mb.getModifiers()) || Modifier.isPrivate(mb.getModifiers()) || Modifier.isFinal(mb.getModifiers()) || mb.isConstructor())
				isIntern = "true";
			//System.err.println("Add "+type+"::"+signature+"::"+isIntern);
			if(!currentJavaMethod.getCallsSite().contains(type+"::"+signature+"::"+isIntern))
				currentJavaMethod.getCallsSite().add(type+"::"+signature+"::"+isIntern);
			if(!classCreated.contains(mb.getDeclaringClass().getQualifiedName()));
			classCreated.add(mb.getDeclaringClass().getQualifiedName());	
		}

		return true;
	}

	public boolean visit(ConstructorInvocation ci) {
		IMethodBinding mb = ci.resolveConstructorBinding();

		if ( currentJavaMethod != null && mb != null) {	
			String signature = getShortDeclaration(mb);
			String type = getStaticType(mb);
			String isIntern = "false";
			if(Modifier.isStatic(mb.getModifiers()) || Modifier.isPrivate(mb.getModifiers()) || Modifier.isFinal(mb.getModifiers()) || mb.isConstructor())
				isIntern = "true";
			//System.err.println("Add "+type+"::"+signature+"::"+isIntern);
			if(!currentJavaMethod.getCallsSite().contains(type+"::"+signature+"::"+isIntern))
				currentJavaMethod.getCallsSite().add(type+"::"+signature+"::"+isIntern);
		}

		return true;
	}

	public boolean visit(SuperConstructorInvocation ci) {
		IMethodBinding mb = ci.resolveConstructorBinding();
		if ( currentJavaMethod != null && mb != null) {	
			superInvocation = true;
			String signature = getShortDeclaration(mb);
			String type = getStaticType(mb);

			String isIntern = "false";
			if(Modifier.isStatic(mb.getModifiers()) || Modifier.isPrivate(mb.getModifiers()) || Modifier.isFinal(mb.getModifiers()) || mb.isConstructor())
				isIntern = "true";
			//System.err.println("Add "+type+"::"+signature+"::"+isIntern);
			if(!currentJavaMethod.getCallsSite().contains(type+"::"+signature+"::"+isIntern))
				currentJavaMethod.getCallsSite().add(type+"::"+signature+"::"+isIntern);
			if(!classCreated.contains(mb.getDeclaringClass().getQualifiedName()));
			classCreated.add(mb.getDeclaringClass().getQualifiedName());
		}

		return true;
	}

	public boolean visit(FieldAccess a) {
		IVariableBinding b = a.resolveFieldBinding();

		if ( b != null && currentJavaMethod != null ) {
			if ( b.getDeclaringClass() != null ) {
				if(!(a.getParent() instanceof Assignment)) {

					String fieldId = getVariableBindingId(b);
					//System.err.println("Add "+fieldId);
					currentJavaMethod.getCallsSite().add(fieldId);
				}
				else {
					if(((Assignment)a.getParent()).getLeftHandSide() instanceof FieldAccess) {
						if(((FieldAccess)((Assignment)a.getParent()).getLeftHandSide()).resolveFieldBinding()!=null) {
							if(!((FieldAccess)((Assignment)a.getParent()).getLeftHandSide()).resolveFieldBinding().getKey().equals(b.getKey())) {

								String fieldId = getVariableBindingId(b);
								//System.err.println("Add "+fieldId);
								currentJavaMethod.getCallsSite().add(fieldId);
							}
						}
					}
				}
			}
		}

		return true;
	}

	public boolean visit(final Assignment node) {
		if(node.getLeftHandSide() instanceof FieldAccess) {
			IVariableBinding b = ((FieldAccess)node.getLeftHandSide()).resolveFieldBinding();
			if ( b != null && currentJavaMethod != null ) {
				if ( b.getDeclaringClass() != null ) {

					String fieldId = getVariableBindingId(b);
					//System.err.println("Add "+fieldId);
					currentJavaMethod.getCallsSite().add(fieldId);
				}
			}
		}
		else if(node.getLeftHandSide() instanceof SuperFieldAccess) {
			IVariableBinding b = ((SuperFieldAccess)node.getLeftHandSide()).resolveFieldBinding();
			if ( b != null && currentJavaMethod != null ) {
				if ( b.getDeclaringClass() != null ) {

					String fieldId = getVariableBindingId(b);
					//System.err.println("Add "+fieldId);
					currentJavaMethod.getCallsSite().add(fieldId);
				}
			}
		}
		else if(node.getLeftHandSide() instanceof SimpleName) {
			IBinding ib = ((Name)node.getLeftHandSide()).resolveBinding();
			if(ib!=null){
				if(ib.getKind() == IBinding.VARIABLE) {

					IVariableBinding b = (IVariableBinding)((Name)node.getLeftHandSide()).resolveBinding();
					if (b.isField()) {

						if ( b != null && currentJavaMethod != null ) {
							if ( b.getDeclaringClass() != null) {

								String fieldId = getVariableBindingId(b);
								//System.err.println("Add "+fieldId);
								currentJavaMethod.getCallsSite().add(fieldId);
							}
						}
					}

				}
			}
		}
		return true;
	}


	public boolean visit(SuperFieldAccess a) {
		IVariableBinding b = a.resolveFieldBinding();

		if ( b != null && currentJavaMethod != null ) {
			if ( b.getDeclaringClass() != null ) {
				if(!(a.getParent() instanceof Assignment)) {

					String fieldId = getVariableBindingId(b);
					//System.err.println("Add "+fieldId);
					currentJavaMethod.getCallsSite().add(fieldId);
				}
				else {
					if(((Assignment)a.getParent()).getLeftHandSide() instanceof SuperFieldAccess) {
						if(!((SuperFieldAccess)((Assignment)a.getParent()).getLeftHandSide()).resolveFieldBinding().getKey().equals(b.getKey())) {

							String fieldId = getVariableBindingId(b);
							//System.err.println("Add "+fieldId);
							currentJavaMethod.getCallsSite().add(fieldId);
						}
					}
				}
			}
		}

		return true;
	}

	public boolean visit(SimpleName  n){
		IBinding ib = n.resolveBinding();
		if(ib!=null){
			if(ib.getKind() == IBinding.VARIABLE) {

				IVariableBinding b = (IVariableBinding) n.resolveBinding();
				if (b.isField()) {

					if ( b != null && currentJavaMethod != null ) {
						if ( b.getDeclaringClass() != null) {
							if(!(n.getParent() instanceof Assignment)) {
								String fieldId = getVariableBindingId(b);
								//System.err.println("Add "+fieldId);
								currentJavaMethod.getCallsSite().add(fieldId);
							}
							else {
								if(n.getParent().getParent() instanceof Assignment) {
									if(((Assignment)n.getParent().getParent()).getLeftHandSide() instanceof SimpleName)
										if(!(((SimpleName)((Assignment)n.getParent().getParent()).getLeftHandSide())).resolveBinding().getKey().equals(b.getKey())) {
											String fieldId = getVariableBindingId(b);
											//System.err.println("Add "+fieldId);
											currentJavaMethod.getCallsSite().add(fieldId);
										}
								}
							}
						}
					}
				}

			}
		}
		return true;
	}

	public boolean visit(QualifiedName  n){

		if(n.resolveBinding()!=null) {
			if(n.resolveBinding().getKind()==3) {
				if(((IVariableBinding)n.resolveBinding())!=null){
					if(((IVariableBinding)n.resolveBinding()).getDeclaringClass()!=null) {
						if(((IVariableBinding)n.resolveBinding()).getDeclaringClass().getQualifiedName()!=null) {
							String id = ((IVariableBinding)n.resolveBinding()).getDeclaringClass().getQualifiedName()+"."+n.getFullyQualifiedName();
							//System.err.println("Add "+id);
							if(currentJavaMethod!=null)
								currentJavaMethod.getCallsSite().add(id);
						}
					}
				}
			}
		}
		return true;
	}

	private static String getVariableBindingId(IVariableBinding b) {

		String realId = b.getDeclaringClass().getTypeDeclaration().getQualifiedName() + "." + b.getName() + ":" + b.getVariableDeclaration().getType().getQualifiedName();
		//System.out.println(realId);
		return realId;
	}

	private static String getMethodBindingId(IMethodBinding b) {
		String realId="";
		if(b.getDeclaringClass().isParameterizedType()) {
			realId += b.getDeclaringClass().getBinaryName();
			for(ITypeBinding tb : b.getDeclaringClass().getTypeDeclaration().getTypeParameters())
				realId+=tb.toString();
			realId +="(";
		}
		else if( !b.getDeclaringClass().getQualifiedName().isEmpty() )
			realId = b.getDeclaringClass().getQualifiedName() + "." + b.getName() + "(";
		else{
			if(b.getMethodDeclaration().getDeclaringClass().getDeclaringMethod()!=null)
				realId =  b.getMethodDeclaration().getDeclaringClass().getDeclaringClass().getQualifiedName()+ "." + 
						b.getMethodDeclaration().getDeclaringClass().getDeclaringMethod().getName()
						+"."+ b.getMethodDeclaration().getName() + "(";
			else 
				realId =  b.getMethodDeclaration().getDeclaringClass().getDeclaringClass().getQualifiedName()+ "."
						+"."+ b.getMethodDeclaration().getName() +"(";
		}

		int i = 0;
		for(ITypeBinding tb : b.getMethodDeclaration().getParameterTypes()) {
			realId += tb.getTypeDeclaration().getQualifiedName();
			if ( i < b.getParameterTypes().length - 1 )
				realId += ",";
			//System.out.println("6"+tb.getTypeDeclaration().getQualifiedName());
			i++;
		}


		if(!b.isConstructor()) {
			if(b.getReturnType().getDeclaringClass()!=null) {

				if(b.getReturnType().getDeclaringClass().isParameterizedType()) {
					realId += "):"+b.getReturnType().getDeclaringClass().getBinaryName();
					for(ITypeBinding tb : b.getReturnType().getDeclaringClass().getTypeDeclaration().getTypeParameters()) {
						realId+=tb.toString();
					}
				}
				else{
					realId += "):" + b.getMethodDeclaration().getReturnType().getTypeDeclaration().getQualifiedName();
				}
			}
			else if(b.getReturnType()!=null) {

				if(b.getReturnType().isParameterizedType() || b.isGenericMethod()) {

					realId += "):"+b.getMethodDeclaration().getReturnType().getTypeDeclaration().getQualifiedName();
					for(ITypeBinding tb : b.getReturnType().getTypeDeclaration().getTypeParameters()) {
						realId+=tb.toString();
					}
				}
				else {

					realId += "):" + b.getMethodDeclaration().getReturnType().getTypeDeclaration().getQualifiedName();
				}
			}
			else{

				realId += "):" + b.getMethodDeclaration().getReturnType().getTypeDeclaration().getQualifiedName();
			}
		}
		else {

			if(b.getDeclaringClass().isParameterizedType()) {
				realId += "):"+b.getDeclaringClass().getBinaryName();
				for(ITypeBinding tb : b.getDeclaringClass().getTypeDeclaration().getTypeParameters()) {
					realId+=tb.toString();
				}
			}
			else{
				realId += "):" + b.getMethodDeclaration().getReturnType().getTypeDeclaration().getQualifiedName();
			}
		}

		return realId;
	}

	private static String getAnonymousMethodBindingId(IMethodBinding b, JavaClass currentClass) {
		String realId = currentClass.getQualifiedName()+"."+ b.getName() + "(";

		int i = 0;
		for(ITypeBinding tb : b.getMethodDeclaration().getParameterTypes()) {
			realId += tb.getTypeDeclaration().getQualifiedName();
			if ( i < b.getParameterTypes().length - 1 )
				realId += ",";
			//System.out.println("6"+tb.getTypeDeclaration().getQualifiedName());
			i++;
		}


		if(!b.isConstructor()) {
			if(b.getReturnType().getDeclaringClass()!=null) {

				if(b.getReturnType().getDeclaringClass().isParameterizedType()) {
					realId += "):"+b.getReturnType().getDeclaringClass().getBinaryName();
					for(ITypeBinding tb : b.getReturnType().getDeclaringClass().getTypeDeclaration().getTypeParameters()) {
						realId+=tb.toString();
					}
				}
				else{
					realId += "):" + b.getMethodDeclaration().getReturnType().getTypeDeclaration().getQualifiedName();
				}
			}
			else if(b.getReturnType()!=null) {

				if(b.getReturnType().isParameterizedType() || b.isGenericMethod()) {

					realId += "):"+b.getMethodDeclaration().getReturnType().getTypeDeclaration().getQualifiedName();
					for(ITypeBinding tb : b.getReturnType().getTypeDeclaration().getTypeParameters()) {
						realId+=tb.toString();
					}
				}
				else {

					realId += "):" + b.getMethodDeclaration().getReturnType().getTypeDeclaration().getQualifiedName();
				}
			}
			else{

				realId += "):" + b.getMethodDeclaration().getReturnType().getTypeDeclaration().getQualifiedName();
			}
		}
		else {

			if(b.getDeclaringClass().isParameterizedType()) {
				realId += "):"+b.getDeclaringClass().getBinaryName();
				for(ITypeBinding tb : b.getDeclaringClass().getTypeDeclaration().getTypeParameters()) {
					realId+=tb.toString();
				}
			}
			else{
				realId += "):" + b.getMethodDeclaration().getReturnType().getTypeDeclaration().getQualifiedName();
			}
		}

		return realId;
	}

	private static String getShortDeclaration(IMethodBinding b) {
		String realId="";
		
		realId+=b.getMethodDeclaration().getName()+"(";

		int i = 0;

		for(ITypeBinding tb : b.getMethodDeclaration().getParameterTypes()) {
			realId += tb.getTypeDeclaration().getQualifiedName();

			if ( i < b.getParameterTypes().length - 1 )
				realId += ",";

			i++;
		}

		if(!b.isConstructor()) {
			if(b.getReturnType().getDeclaringClass()!=null) {
				if(b.getReturnType().getDeclaringClass().isParameterizedType()) {
					realId += "):"+b.getReturnType().getDeclaringClass().getBinaryName();
					for(ITypeBinding tb : b.getReturnType().getDeclaringClass().getTypeDeclaration().getTypeParameters()) {
						realId+=tb.toString();
					}
				}
				else {
					realId += "):" + b.getMethodDeclaration().getReturnType().getTypeDeclaration().getQualifiedName();
				}
			}
			else if(b.getReturnType()!=null) {

				if(b.getReturnType().isParameterizedType() || b.getReturnType().isGenericType()) {
					realId += "):"+b.getMethodDeclaration().getReturnType().getTypeDeclaration().getQualifiedName();
					for(ITypeBinding tb : b.getReturnType().getTypeDeclaration().getTypeParameters()) {
						realId+=tb.toString();
					}
				}
				else {
					realId += "):" + b.getMethodDeclaration().getReturnType().getTypeDeclaration().getQualifiedName();
				}
			}
			else{
				realId += "):" + b.getMethodDeclaration().getReturnType().getTypeDeclaration().getQualifiedName();
			}
			
		}
		else {
			realId += "):void";
		}


		return realId;
	}

	private static String getStaticType(IMethodBinding b) {
		String realId="";
		if(b.getMethodDeclaration().getDeclaringClass().isParameterizedType()) {
			realId += b.getMethodDeclaration().getDeclaringClass().getBinaryName();
			for(ITypeBinding tb : b.getMethodDeclaration().getDeclaringClass().getTypeDeclaration().getTypeParameters())
				realId+=tb.toString();
		}
		else if( !b.getMethodDeclaration().getDeclaringClass().getQualifiedName().isEmpty() )
			realId = b.getMethodDeclaration().getDeclaringClass().getQualifiedName();
		else{
			if(b.getMethodDeclaration().getMethodDeclaration().getDeclaringClass().getDeclaringMethod()!=null)
				realId =  b.getMethodDeclaration().getDeclaringClass().getDeclaringClass().getQualifiedName();

			else 
				realId =  b.getMethodDeclaration().getDeclaringClass().getDeclaringClass().getQualifiedName();
		}
		return realId;
	}

}
