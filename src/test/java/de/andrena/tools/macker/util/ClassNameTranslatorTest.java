package de.andrena.tools.macker.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

public class ClassNameTranslatorTest {

	@Test
	public void isJavaIdentifier_ValidJavaClass() {
		assertThat(ClassNameTranslator.isJavaIdentifier(ClassNameTranslator.class.getName()), is(true));
	}

	@Test
	public void isJavaIdentifier_DefaultPackageJavaClass() {
		assertThat(ClassNameTranslator.isJavaIdentifier(ClassNameTranslator.class.getSimpleName()), is(true));
	}

	@Test
	public void isJavaIdentifier_InvalidJavaClass() {
		assertThat(ClassNameTranslator.isJavaIdentifier("-InvalidName"), is(false));
	}

	@Test
	public void isJavaIdentifier_DefaultPackagePackageInfo() {
		assertThat(ClassNameTranslator.isJavaIdentifier("package-info"), is(true));
	}

	@Test
	public void isJavaIdentifier_PackageInfoWithinPackage() {
		assertThat(ClassNameTranslator.isJavaIdentifier("example.package-info"), is(true));
	}

}
