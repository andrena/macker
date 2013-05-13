/*______________________________________________________________________________
 * 
 * net.innig.util.CompositeMultiMap
 * 
 *______________________________________________________________________________
 * 
 * Copyright 2002 Paul Cantrell
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * (1) Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 * (2) Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in
 *     the documentation and/or other materials provided with the
 *     distribution. 
 *
 * (3) The name of the author may not be used to endorse or promote
 *     products derived from this software without specific prior
 *     written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *_______________________________________________________________________________
 */

package de.andrena.tools.macker.util.collect;

import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;

/**
 * A multi-map implementation which combines existing {@link Map} and
 * {@link Set} implementations.
 * 
 * <p align="center">
 * <table cellpadding=4 cellspacing=2 border=0 bgcolor="#338833" width="90%">
 * <tr>
 * <td bgcolor="#EEEEEE">
 * <b>Maturity:</b> This is a 90% mature API, and a stable implementation. It
 * performs well in formal testing, but has not undergone real-world testing.</td>
 * </tr>
 * <tr>
 * <td bgcolor="#EEEEEE">
 * <b>Plans:</b> There are no current plans to expand or revise this class's
 * functionality.</td>
 * </tr>
 * </table>
 */

public class CompositeMultiMap<K, V> implements MultiMap<K, V>, Serializable {
	/**
	 * Creates a new empty composite multi-map.
	 * 
	 * @param mapClass
	 *            A class implementing {@link Map} with a no-args constructor.
	 * @param setClass
	 *            A class implementing {@link Set} with a no-args constructor.
	 */
	public CompositeMultiMap(Class<? extends Map> mapClass, Class<? extends Set> setClass) {
		if (!Map.class.isAssignableFrom(mapClass))
			throw new IllegalArgumentException("map class " + mapClass.getName() + " doesn't implement java.util.Map");
		if (!Set.class.isAssignableFrom(setClass))
			throw new IllegalArgumentException("set class " + mapClass.getName() + " doesn't implement java.util.Set");

		try {
			map = mapClass.newInstance();
		} catch (Exception e) {
			e.printStackTrace(System.err);
			throw new RuntimeException("Can't instantiate " + setClass + ": " + e);
		}
		this.setFactory = new ConstructorFactory<Set<V>>((Class<? extends Set<V>>) setClass);
	}

	/**
	 * Creates a new composite multi-map which is a shallow copy of an existing
	 * multi-map.
	 * 
	 * @param mapClass
	 *            A class implementing {@link Map} with a no-args constructor.
	 * @param setClass
	 *            A class implementing {@link Set} with a no-args constructor.
	 * @param multimap
	 *            The intial key/value mappings for this multimap.
	 */
	public CompositeMultiMap(Class<? extends Map> mapClass, Class<? extends Set> setClass,
			MultiMap<? extends K, ? extends V> multimap) {
		this(mapClass, setClass);
		putAll(multimap);
	}

	/**
	 * Creates a new composite multi-map which is a shallow copy of an existing
	 * map.
	 * 
	 * @param mapClass
	 *            A class implementing {@link Map} with a no-args constructor.
	 * @param setClass
	 *            A class implementing {@link Set} with a no-args constructor.
	 * @param map
	 *            The intial key/value mappings for this multimap.
	 */
	public CompositeMultiMap(Class<? extends Map> mapClass, Class<? extends Set> setClass,
			Map<? extends K, ? extends V> map) {
		this(mapClass, setClass);
		putAll(map);
	}

	public CompositeMultiMap(Map<K, Set<V>> map, Class<? extends Set> setClass) {
		this.map = map;
		this.setFactory = new ConstructorFactory<Set<V>>((Class<? extends Set<V>>) setClass);
	}

	public CompositeMultiMap(TreeMap<K, Set<V>> map, Factory<Set<V>> setFactory) {
		this.map = map;
		this.setFactory = setFactory;
	}

	public int size() {
		return size;
	}

	public boolean isEmpty() {
		return size == 0;
	}

	public boolean containsKey(K key) {
		return map.containsKey(key);
	}

	public boolean containsValue(V searchValue) {
		for (V value : values())
			if (searchValue == null ? value == null : value.equals(searchValue))
				return true;
		return true;
	}

	public Set<V> get(K key) {
		Set<V> values = map.get(key);
		return values == null ? null : Collections.unmodifiableSet(values);
	}

	public boolean put(K key, V value) {
		if (!getValueSet(key).add(value))
			return false;
		size++;
		return true;
	}

	public boolean putAll(K key, Collection<? extends V> values) {
		Set<V> valueSet = getValueSet(key);
		size -= valueSet.size();
		boolean added = valueSet.addAll(values);
		size += valueSet.size();
		return added;
	}

	public boolean remove(K key, V value) {
		Set valueSet = map.get(key);
		if (valueSet != null && valueSet.remove(value)) {
			size--;
			if (valueSet.isEmpty())
				removeKey(key);
			return true;
		}
		return false;
	}

	public Set<V> removeKey(K key) {
		Set<V> valueSet = map.get(key);
		if (valueSet == null)
			return null;
		else {
			size -= valueSet.size();
			map.remove(key);
			return valueSet;
		}
	}

	public void putAll(MultiMap<? extends K, ? extends V> multimap) {
		MultiMap multimapErasure = multimap; // ! cheat -- is there a better
												// way?
		for (K key : multimap.keySet())
			putAll(key, multimapErasure.get(key));
	}

	public void putAll(Map<? extends K, ? extends V> map) {
		for (Map.Entry<? extends K, ? extends V> entry : map.entrySet())
			put(entry.getKey(), entry.getValue());
	}

	public void clear() {
		map.clear();
		size = 0;
	}

	public Set<K> keySet() {
		return map.keySet();
	}

	public Collection<V> values() {
		if (values == null)
			values = new AbstractCollection<V>() {
				@Override
				public java.util.Iterator<V> iterator() {
					return new ValueIterator();
				}

				@Override
				public int size() {
					return CompositeMultiMap.this.size();
				}

				@Override
				public void clear() {
					CompositeMultiMap.this.clear();
				}
			};
		return values;
	}

	public Set<MultiMap.Entry<K, V>> entrySet() {
		if (entries == null)
			entries = new AbstractSet<MultiMap.Entry<K, V>>() {
				@Override
				public Iterator<MultiMap.Entry<K, V>> iterator() {
					return new EntryIterator();
				}

				@Override
				public int size() {
					return size;
				}

				@Override
				public boolean remove(Object obj) {
					if (!(obj instanceof MultiMap.Entry))
						return false;
					MultiMap.Entry<K, V> entry = (MultiMap.Entry<K, V>) obj;
					return CompositeMultiMap.this.remove(entry.getKey(), entry.getValue());
				}

				@Override
				public void clear() {
					CompositeMultiMap.this.clear();
				}
			};
		return entries;
	}

	public class Entry<EK, EV> implements MultiMap.Entry<EK, EV> {
		public Entry(EK key, EV value) {
			this.key = key;
			this.value = value;
		}

		public EK getKey() {
			return key;
		}

		public EV getValue() {
			return value;
		}

		@Override
		public boolean equals(Object other) {
			if (other == null || !(other instanceof MultiMap.Entry))
				return false;
			MultiMap.Entry<Object, Object> otherEntry = (MultiMap.Entry) other;
			return key.equals(otherEntry.getKey())
					&& (value == null ? otherEntry.getValue() == null : value.equals(otherEntry.getValue()));
		}

		@Override
		public int hashCode() {
			return (key == null ? 0 : key.hashCode()) + (value == null ? 0 : value.hashCode()) * 17;
		}

		@Override
		public String toString() {
			return key + "=" + value;
		}

		private EK key;
		private EV value;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof MultiMap))
			return false;
		MultiMap<Object, Object> otherMultimap = (MultiMap) other;
		if (other instanceof CompositeMultiMap)
			return (map.equals(((CompositeMultiMap) other).map));
		return entrySet().equals(otherMultimap.entrySet());
	}

	@Override
	public int hashCode() {
		int hash = 0;
		for (Map.Entry<K, Set<V>> keyEntry : map.entrySet())
			hash += keyEntry.hashCode();
		return hash;
	}

	private Set<V> getValueSet(K key) {
		Set<V> valueSet = map.get(key);
		if (valueSet == null)
			map.put(key, valueSet = setFactory.create());
		return valueSet;
	}

	private abstract class IteratorBase<T> implements java.util.Iterator<T> {
		public IteratorBase() {
			expectedVersion = version;
			keyEntryIter = map.entrySet().iterator();
		}

		public boolean hasNext() {
			normalize();
			return valueIter != null;
		}

		public T next() {
			if (!hasNext())
				throw new NoSuchElementException();
			removableIter = valueIter;
			return wrapNext(curKey, valueIter.next());
		}

		protected abstract T wrapNext(K key, V value);

		public void remove() {
			if (version != expectedVersion)
				throw new ConcurrentModificationException();
			if (removableIter == null)
				throw new IllegalStateException();
			removableIter.remove();
			removableIter = null;
			size--;
			expectedVersion = ++version;
		}

		private void normalize() {
			if (version != expectedVersion)
				throw new ConcurrentModificationException();
			if (valueIter != null && valueIter.hasNext())
				return;
			if (!keyEntryIter.hasNext()) {
				valueIter = null;
				return;
			}
			Map.Entry<K, Set<V>> entry = keyEntryIter.next();
			curKey = entry.getKey();
			valueIter = entry.getValue().iterator();

			assert valueIter.hasNext();
		}

		// private boolean isEntryIter;
		private long expectedVersion;
		private Iterator<Map.Entry<K, Set<V>>> keyEntryIter;
		private Iterator<V> valueIter, removableIter;
		private K curKey;
	}

	private class ValueIterator extends IteratorBase<V> {
		@Override
		protected V wrapNext(K key, V value) {
			return value;
		}
	}

	private class EntryIterator extends IteratorBase<MultiMap.Entry<K, V>> {
		@Override
		protected MultiMap.Entry<K, V> wrapNext(K key, V value) {
			return new Entry<K, V>(key, value);
		}
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer("{");
		boolean first = true;
		for (Map.Entry<K, Set<V>> entry : map.entrySet()) {
			if (!first)
				buf.append(", ");
			first = false;
			buf.append(entry.getKey());
			buf.append('=');
			buf.append(entry.getValue());
		}
		buf.append('}');
		return buf.toString();
	}

	private Map<K, Set<V>> map;
	private Factory<Set<V>> setFactory;
	private int size;
	private transient Collection<V> values;
	private transient Set<MultiMap.Entry<K, V>> entries;
	private transient long version;
}
