package de.andrena.tools.macker.util.collect;

import java.io.Serializable;

public class ConstructorFactory<T> implements Factory<T>, Serializable {
	public ConstructorFactory(Class<? extends T> type) {
		this.type = type;
	}

	public T create() {
		try {
			return type.newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private Class<? extends T> type;
}
