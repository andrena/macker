package de.andrena.tools.macker.rule;


import org.junit.Test;

public class RuleSetBuilderTest {
	@Test
	public void build_WithoutInternetConnection() throws Exception {
		SecurityManager oldSecurityManager = System.getSecurityManager();
		System.setSecurityManager(new NoInternetConnectionSecurityManager());
		new RuleSetBuilder().build(getClass().getClassLoader().getResourceAsStream("macker-rules.xml"));
		System.setSecurityManager(oldSecurityManager);
	}
}
