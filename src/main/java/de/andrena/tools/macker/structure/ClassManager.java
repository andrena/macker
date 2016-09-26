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
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import de.andrena.tools.macker.util.ClassNameTranslator;
import de.andrena.tools.macker.util.collect.InnigCollections;
import de.andrena.tools.macker.util.collect.MultiMap;
import de.andrena.tools.macker.util.collect.TreeMultiMap;

/**
 * The global collection of classes in Macker's rule-checking space.
 */
public class ClassManager {
	public ClassManager() {
		// Trees make nice sorted output
		allClasses = new TreeSet<ClassInfo>(ClassInfoNameComparator.INSTANCE);
		primaryClasses = new TreeSet<ClassInfo>(ClassInfoNameComparator.INSTANCE);
		classNameToInfo = new TreeMap<String, ClassInfo>();
		references = new TreeMultiMap<ClassInfo, ClassInfo>(ClassInfoNameComparator.INSTANCE,
				ClassInfoNameComparator.INSTANCE);
		classLoader = Thread.currentThread().getContextClassLoader();

		for (ClassInfo ci : PrimitiveTypeInfo.ALL)
			replaceClass(ci);
	}

	public ClassLoader getClassLoader() {
		return classLoader;
	}

	public void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	public ClassInfo readClass(File classFile) throws ClassParseException, IOException {
		ClassInfo classInfo = new ParsedClassInfo(this, classFile);
		addClass(classInfo);
		return classInfo;
	}

	private void addClass(ClassInfo classInfo) {
		ClassInfo existing = findClassInfo(classInfo.getFullName());
		if (existing != null && !(existing instanceof HollowClassInfo))
			throw new IllegalStateException("ClassManager already contains a class named " + classInfo);
		replaceClass(classInfo);
	}

	private void replaceClass(ClassInfo classInfo) {
		allClasses.add(classInfo);
		classNameToInfo.put(classInfo.getFullName(), classInfo);
	}

	public void makePrimary(ClassInfo classInfo) {
		if (!classInfo.isComplete())
			throw new IncompleteClassInfoException(classInfo + " cannot be a primary class, because the"
					+ " class file isn't on Macker's classpath");
		if (classInfo instanceof PrimitiveTypeInfo)
			throw new IllegalArgumentException(classInfo + " cannot be a primary class, because it is a primitive type");
		checkOwner(classInfo);
		classInfo = findClassInfo(classInfo.getFullName()); // in case of hollow
		primaryClasses.add(classInfo);
		references.putAll(classInfo, classInfo.getReferences().keySet());
		allClasses.addAll(classInfo.getReferences().keySet());
	}

	public Set<ClassInfo> getAllClasses() {
		return Collections.unmodifiableSet(allClasses);
	}

	public Set<ClassInfo> getPrimaryClasses() {
		return Collections.unmodifiableSet(primaryClasses);
	}

	public MultiMap<ClassInfo, ClassInfo> getReferences() {
		return InnigCollections.unmodifiableMultiMap(references);
	}

	public ClassInfo getClassInfo(String className) {
		ClassInfo classInfo = findClassInfo(className);
		if (classInfo != null)
			return classInfo;
		else {
			classInfo = new HollowClassInfo(this, className);
			replaceClass(classInfo);
			return classInfo;
		}
	}

	ClassInfo loadClassInfo(String className) {
		ClassInfo classInfo = findClassInfo(className);
		if (classInfo == null || classInfo instanceof HollowClassInfo) {
			classInfo = null; // don't use hollow!
			String resourceName = ClassNameTranslator.classToResourceName(className);
			InputStream classStream = classLoader.getResourceAsStream(resourceName);

			if (classStream == null) {
				showIncompleteWarning();
				System.out.println("WARNING: Unable to find class " + className + " in the classpath");
			} else
				try {
					classInfo = new ParsedClassInfo(this, classStream);
				} catch (Exception e) {
					if (e instanceof RuntimeException)
						throw (RuntimeException) e;
					showIncompleteWarning();
					System.out.println("WARNING: Unable to load class " + className + ": " + e);
				} finally {
					try {
						classStream.close();
					} catch (IOException ioe) {
						 // nothing we can do
					}
				}

			if (classInfo == null)
				classInfo = new IncompleteClassInfo(this, className);

			replaceClass(classInfo);
		}

		return classInfo;
	}

	private ClassInfo findClassInfo(String className) {
		return classNameToInfo.get(className);
	}

	private void checkOwner(ClassInfo classInfo) throws IllegalStateException {
		if (classInfo.getClassManager() != this)
			throw new IllegalStateException("classInfo argument (" + classInfo
					+ ") is not managed by this ClassManager");
	}

	private void showIncompleteWarning() {
		if (!incompleteClassWarning) {
			incompleteClassWarning = true;
			System.out.println("WARNING: Macker is unable to load some of the external classes"
					+ " used by the primary classes (see warnings below).  Rules which"
					+ " depend on attributes of these missing classes other than their" + " names will fail.");
		}
	}

	private boolean incompleteClassWarning;
	private ClassLoader classLoader;
	private Set<ClassInfo> allClasses, primaryClasses;
	private Map<String, ClassInfo> classNameToInfo;
	private MultiMap<ClassInfo, ClassInfo> references;
}
