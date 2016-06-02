/*______________________________________________________________________________
 *
 * Macker   http://innig.net/macker/
 *
 * Copyright 2002-2003 Paul Cantrell
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License version 2, as published by the
 * Free Software Foundation. See the file LICENSE.html for more information.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, including the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the license for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc. / 59 Temple
 * Place, Suite 330 / Boston, MA 02111-1307 / USA.
 *______________________________________________________________________________
 */

package de.andrena.tools.macker.structure;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtField;
import javassist.Modifier;
import de.andrena.tools.macker.util.ClassNameTranslator;
import de.andrena.tools.macker.util.collect.CompositeMultiMap;
import de.andrena.tools.macker.util.collect.InnigCollections;
import de.andrena.tools.macker.util.collect.MultiMap;

/**
 * Class info retrieved from a class file using Javassist.
 * 
 * @author Ben Romberg
 */
public class ParsedClassInfo extends AbstractClassInfo {

	private String fullClassName;
	private boolean isInterface;
	private boolean isAbstract;
	private boolean isFinal;
	private AccessModifier accessModifier;
	private ClassInfo extendsClass;
	private Set<ClassInfo> implementsClasses;
	private MultiMap<ClassInfo, Reference> references;

	ParsedClassInfo(final ClassManager classManager, final File classFile) throws IOException, ClassParseException {
		super(classManager);
		parse(ClassPool.getDefault().makeClass(new FileInputStream(classFile)));
	}

	ParsedClassInfo(final ClassManager classManager, final InputStream classFileStream) throws IOException,
			ClassParseException {
		super(classManager);
		parse(ClassPool.getDefault().makeClass(classFileStream));
	}

	public String getFullName() {
		return this.fullClassName;
	}

	public boolean isComplete() {
		return true;
	}

	public boolean isInterface() {
		return this.isInterface;
	}

	public boolean isAbstract() {
		return this.isAbstract;
	}

	public boolean isFinal() {
		return this.isFinal;
	}

	public AccessModifier getAccessModifier() {
		return this.accessModifier;
	}

	public ClassInfo getExtends() {
		return this.extendsClass;
	}

	public Set<ClassInfo> getImplements() {
		return this.implementsClasses;
	}

	public MultiMap<ClassInfo, Reference> getReferences() {
		return this.references;
	}

	private void parseClassName(CtClass classFile) {
		this.fullClassName = classFile.getName();
	}

	private void parseFlags(CtClass classFile) {
		this.isInterface = classFile.isInterface();
		this.isAbstract = Modifier.isAbstract(classFile.getModifiers());
		this.isFinal = Modifier.isFinal(classFile.getModifiers());
	}

	private void parseAccess(CtClass classFile) {
		setAccessModifier(translateAccess(classFile.getModifiers()));
	}

	private void setAccessModifier(final AccessModifier accessModifier) {
		this.accessModifier = accessModifier;
	}

	private AccessModifier translateAccess(int modifiers) {
		if (Modifier.isPublic(modifiers)) {
			return AccessModifier.PUBLIC;
		}
		if (Modifier.isProtected(modifiers)) {
			return AccessModifier.PROTECTED;
		}
		if (Modifier.isPrivate(modifiers)) {
			return AccessModifier.PRIVATE;
		}
		return AccessModifier.PACKAGE;
	}

	private void parseExtends(CtClass classFile) throws ClassParseException {
		String superClassStr = classFile.getClassFile().getSuperclass();
		// avoid NullPointerException if classFile is java.lang.Object
		//System.out.println("class=" + classFile + ", classFile=" + classFile.getClassFile() + ", superClass=" + superClassStr);
		this.extendsClass = superClassStr!=null ? getSafeClassInfo(superClassStr) : null;
	}

	private void parseImplements(CtClass classFile) throws ClassParseException {
		implementsClasses = new TreeSet<ClassInfo>(ClassInfoNameComparator.INSTANCE);
		for (String interfaze : classFile.getClassFile().getInterfaces()) {
			implementsClasses.add(getSafeClassInfo(interfaze));
		}
		implementsClasses = Collections.unmodifiableSet(implementsClasses);
	}

	private void parse(CtClass classFile) throws ClassParseException {
		parseClassName(classFile);
		parseFlags(classFile);
		parseAccess(classFile);
		parseExtends(classFile);
		parseImplements(classFile);
		parseReferences(classFile);
	}

	private void addReference(Reference ref) {
		getReferences().put(ref.getTo(), ref);
	}

	private void parseConstantPoolReferences(CtClass classFile) throws ClassParseException {
		@SuppressWarnings("unchecked")
		Set<String> classNames = classFile.getClassFile().getConstPool().getClassNames();
		for (String className : classNames) {
			addReference(new Reference(this, getSafeClassInfo(className), ReferenceType.CONSTANT_POOL, null, null));
		}
	}

	private void parseMethodReferences(CtClass classFile) throws ClassParseException {
		for (CtBehavior behavior : classFile.getDeclaredMethods()) {
			AccessModifier methodAccess = translateAccess(behavior.getModifiers());
			List<String> paramsAndReturn = ClassNameTranslator.signatureToClassNames(behavior.getSignature());
			for (Iterator<String> i = paramsAndReturn.iterator(); i.hasNext();) {
				String refTo = i.next();
				ClassInfo safeClassInfo = getSafeClassInfo(refTo);
				ReferenceType referenceType;
				if (i.hasNext()) {
					referenceType = ReferenceType.METHOD_PARAM;
				} else {
					referenceType = ReferenceType.METHOD_RETURNS;
				}

				addReference(new Reference(this, safeClassInfo, referenceType, behavior.getName(), methodAccess));
			}
			if (behavior.getMethodInfo().getExceptionsAttribute() != null) {
				String[] exceptionNames = behavior.getMethodInfo().getExceptionsAttribute().getExceptions();
				for (String exceptionName : exceptionNames) {
					addReference(new Reference(this, getSafeClassInfo(exceptionName, behavior.getSignature()),
							ReferenceType.METHOD_THROWS, behavior.getName(), methodAccess));
				}
			}
		}
	}

	private void parseReferences(CtClass classFile) throws ClassParseException {
		this.references = new CompositeMultiMap<ClassInfo, Reference>(new TreeMap<ClassInfo, Set<Reference>>(
				ClassInfoNameComparator.INSTANCE), HashSet.class);
		parseConstantPoolReferences(classFile);
		parseMethodReferences(classFile);
		parseFieldReferences(classFile);
		this.references = InnigCollections.unmodifiableMultiMap(getReferences());
	}

	private void parseFieldReferences(CtClass classFile) throws ClassParseException {
		CtField[] fields = classFile.getFields();
		for (CtField field : fields) {
			List<String> types = ClassNameTranslator.signatureToClassNames(field.getSignature());
			if (types.size() != 1) {
				throw new ClassParseException("expected one type for field " + getFullName() + '.' + field.getName()
						+ "; got: " + types + " (signature is \"" + field.getSignature() + "\")", classFile);
			}
			addReference(new Reference(this, getSafeClassInfo(types.get(0), field.getSignature()),
					ReferenceType.FIELD_SIGNATURE, field.getName(), translateAccess(field.getModifiers())));
		}
	}

	private ClassInfo getSafeClassInfo(final String className) throws ClassParseException {
		return getSafeClassInfo(ClassNameTranslator.typeConstantToClassName(className), className);
	}

	private ClassInfo getSafeClassInfo(final String className, final String unparsedClassName)
			throws ClassParseException {
		if (!ClassNameTranslator.isJavaIdentifier(className)) {
			throw new ClassParseException("unable to parse class name / signature: \"" + unparsedClassName
					+ "\" (got \"" + className + "\")");
		}
		return getClassManager().getClassInfo(className);
	}
}
