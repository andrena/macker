package de.andrena.tools.macker.rule;

import java.security.Permission;

public class NoInternetConnectionSecurityManager extends SecurityManager {
	@Override
	public void checkConnect(String host, int port) {
		throw new SecurityException();
	}

	@Override
	public void checkConnect(String host, int port, Object context) {
		throw new SecurityException();
	}

	@Override
	public void checkAccept(String host, int port) {
		throw new SecurityException();
	}

	@Override
	public void checkPermission(Permission perm) {
	}
}