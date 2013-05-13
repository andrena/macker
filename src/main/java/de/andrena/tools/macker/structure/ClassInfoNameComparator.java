package de.andrena.tools.macker.structure;

import java.util.Comparator;

public class ClassInfoNameComparator implements Comparator<ClassInfo> {
	public static ClassInfoNameComparator INSTANCE = new ClassInfoNameComparator();

	public int compare(ClassInfo a, ClassInfo b) {
		return a.getFullName().compareTo(b.getFullName());
	}

	private ClassInfoNameComparator() {
	}
}
