/*______________________________________________________________________________
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
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Innig collection utilities.
 * 
 * <p align="center">
 * <table cellpadding=4 cellspacing=2 border=0 bgcolor="#338833" width="90%">
 * <tr>
 * <td bgcolor="#EEEEEE">
 * <b>Maturity:</b> This is not a mature API, and the implementation is
 * completely experimental. The radix sort works, but is quite slow.</td>
 * </tr>
 * <tr>
 * <td bgcolor="#EEEEEE">
 * <b>Plans:</b> This class will eventually contain synchronized and immutable
 * support for {@link MultiMap}. The radix sort will either improve or
 * disappear.</td>
 * </tr>
 * </table>
 */
public class InnigCollections {
	public static <K, V> MultiMap<K, V> emptyMultimap() {
		return EMPTY_MULTIMAP;
	}

	public static final MultiMap EMPTY_MULTIMAP = new MultiMap() {
		public int size() {
			return 0;
		}

		public boolean isEmpty() {
			return true;
		}

		public boolean containsKey(Object key) {
			return false;
		}

		public boolean containsValue(Object value) {
			return false;
		}

		public Set get(Object key) {
			return null;
		}

		public boolean put(Object key, Object value) {
			throw new UnsupportedOperationException(unsupported);
		}

		public boolean putAll(Object key, Collection values) {
			throw new UnsupportedOperationException(unsupported);
		}

		public boolean remove(Object key, Object value) {
			throw new UnsupportedOperationException(unsupported);
		}

		public Set removeKey(Object key) {
			throw new UnsupportedOperationException(unsupported);
		}

		public void putAll(MultiMap multimap) {
			throw new UnsupportedOperationException(unsupported);
		}

		public void putAll(Map map) {
			throw new UnsupportedOperationException(unsupported);
		}

		public void clear() {
			throw new UnsupportedOperationException(unsupported);
		}

		public Set keySet() {
			return Collections.EMPTY_SET;
		}

		public Collection values() {
			return Collections.EMPTY_SET;
		}

		public Set entrySet() {
			return Collections.EMPTY_SET;
		}

		@Override
		public boolean equals(Object o) {
			return (o instanceof MultiMap) && ((MultiMap) o).isEmpty();
		}

		@Override
		public int hashCode() {
			return getClass().hashCode();
		}

		@Override
		public String toString() {
			return "{}";
		}

		private final String unsupported = "InnigCollections.EMPTY_MULTIMAP is immutable";
	};

	public static <K, V> MultiMap<K, V> unmodifiableMultiMap(MultiMap<K, V> mm) {
		return new UnmodifiableMultiMap<K, V>(mm);
	}

	private static class UnmodifiableMultiMap<K, V> implements MultiMap<K, V>, Serializable {
		public UnmodifiableMultiMap(MultiMap<K, V> mm) {
			if (mm == null)
				throw new IllegalArgumentException("multimap is null");
			this.mm = mm;
		}

		public int size() {
			return mm.size();
		}

		public boolean isEmpty() {
			return mm.isEmpty();
		}

		public boolean containsKey(K key) {
			return mm.containsKey(key);
		}

		public boolean containsValue(V value) {
			return mm.containsValue(value);
		}

		public Set<V> get(K key) {
			return Collections.unmodifiableSet(mm.get(key));
		}

		public boolean put(K key, V value) {
			throw new UnsupportedOperationException(unsupported);
		}

		public boolean remove(K key, V value) {
			throw new UnsupportedOperationException(unsupported);
		}

		public Set<V> removeKey(K key) {
			throw new UnsupportedOperationException(unsupported);
		}

		public void clear() {
			throw new UnsupportedOperationException(unsupported);
		}

		public boolean putAll(K key, Collection<? extends V> values) {
			throw new UnsupportedOperationException(unsupported);
		}

		public void putAll(MultiMap<? extends K, ? extends V> multimap) {
			throw new UnsupportedOperationException(unsupported);
		}

		public void putAll(Map<? extends K, ? extends V> map) {
			throw new UnsupportedOperationException(unsupported);
		}

		public Set<K> keySet() {
			return Collections.unmodifiableSet(mm.keySet());
		}

		public Collection<V> values() {
			return Collections.unmodifiableCollection(mm.values());
		}

		public Set<MultiMap.Entry<K, V>> entrySet() {
			return Collections.unmodifiableSet(mm.entrySet());
		}

		@Override
		public boolean equals(Object o) {
			return mm.equals(o);
		}

		@Override
		public int hashCode() {
			return mm.hashCode();
		}

		@Override
		public String toString() {
			return mm.toString();
		}

		private MultiMap<K, V> mm;
		private final String unsupported = "multimap is immutable";
	}

	public static <K, V> MultiMap<K, V> synchronizedMultiMap(MultiMap<K, V> mm) {
		return synchronizedMultiMap(mm, new Object());
	}

	public static <K, V> MultiMap<K, V> synchronizedMultiMap(MultiMap<K, V> mm, Object sync) {
		return new SynchronizedMultiMap<K, V>(mm, sync);
	}

	private static class SynchronizedMultiMap<K, V> implements MultiMap<K, V>, Serializable {
		public SynchronizedMultiMap(MultiMap<K, V> mm, final Object sync) {
			if (mm == null)
				throw new IllegalArgumentException("multimap is null");
			if (sync == null)
				throw new IllegalArgumentException("sync object is null");
			this.mm = mm;
			this.sync = sync;
		}

		public int size() {
			synchronized (sync) {
				return mm.size();
			}
		}

		public boolean isEmpty() {
			synchronized (sync) {
				return mm.isEmpty();
			}
		}

		public boolean containsKey(K key) {
			synchronized (sync) {
				return mm.containsKey(key);
			}
		}

		public boolean containsValue(V value) {
			synchronized (sync) {
				return mm.containsValue(value);
			}
		}

		public Set<V> get(K key) {
			synchronized (sync) {
				return mm.get(key);
			}
		}

		public boolean put(K key, V value) {
			synchronized (sync) {
				return put(key, value);
			}
		}

		public boolean remove(K key, V value) {
			synchronized (sync) {
				return remove(key, value);
			}
		}

		public Set<V> removeKey(K key) {
			synchronized (sync) {
				return removeKey(key);
			}
		}

		public void clear() {
			synchronized (sync) {
				clear();
			}
		}

		public boolean putAll(K key, Collection<? extends V> values) {
			synchronized (sync) {
				return putAll(key, values);
			}
		}

		public void putAll(MultiMap<? extends K, ? extends V> multimap) {
			synchronized (sync) {
				putAll(multimap);
			}
		}

		public void putAll(Map<? extends K, ? extends V> map) {
			synchronized (sync) {
				putAll(map);
			}
		}

		public Set<K> keySet() {
			synchronized (sync) {
				return mm.keySet();
			}
		}

		public Collection<V> values() {
			synchronized (sync) {
				return mm.values();
			}
		}

		public Set<MultiMap.Entry<K, V>> entrySet() {
			synchronized (sync) {
				return mm.entrySet();
			}
		}

		@Override
		public boolean equals(Object o) {
			synchronized (sync) {
				return mm.equals(o);
			}
		}

		@Override
		public int hashCode() {
			synchronized (sync) {
				return mm.hashCode();
			}
		}

		@Override
		public String toString() {
			synchronized (sync) {
				return mm.toString();
			}
		}

		private MultiMap<K, V> mm;
		private Object sync;
	}

	/**
	 * Given a collection whose elements are already unique, returns a Set
	 * backed by that collection. This method is intended for situations where
	 * an algorithm produces an inherently unique collection of elements, and
	 * using a normal implementation of Set would introduce expensive and
	 * redundant uniqueness checks.
	 * <p>
	 * Note that if you pass a collection whose elements are not unique, or
	 * modify the backing collection after passing it in to add duplicates, you
	 * will break the contract of the Set interface. Use this method wisely --
	 * be sure that the collection you're passing in does, in fact, have unique
	 * elements.
	 * <p>
	 * Note that this method performs a uniqueness check <b>if and only if</b>
	 * you are running with assertions enabled.
	 */
	public static <E> Set<E> uniqueCollectionAsSet(final Collection<E> c) {
		if (c instanceof Set)
			return Collections.unmodifiableSet((Set<E>) c);

		assert c.size() == new HashSet<E>(c).size() : "uniqueCollectionAsSet was given a collection with duplicate elements: "
				+ c;

		return new AbstractSet<E>() {
			@Override
			public int size() {
				return c.size();
			}

			@Override
			public boolean isEmpty() {
				return c.isEmpty();
			}

			@Override
			public boolean contains(Object o) {
				return c.contains(o);
			}

			@Override
			public boolean containsAll(Collection other) {
				return c.containsAll(other);
			}

			@Override
			public Object[] toArray() {
				return c.toArray();
			}

			@Override
			public <T> T[] toArray(T[] a) {
				return c.toArray(a);
			}

			@Override
			public boolean add(E o) {
				throw new UnsupportedOperationException(unsupported);
			}

			@Override
			public boolean remove(Object o) {
				throw new UnsupportedOperationException(unsupported);
			}

			@Override
			public boolean addAll(Collection other) {
				throw new UnsupportedOperationException(unsupported);
			}

			@Override
			public boolean retainAll(Collection other) {
				throw new UnsupportedOperationException(unsupported);
			}

			@Override
			public boolean removeAll(Collection other) {
				throw new UnsupportedOperationException(unsupported);
			}

			@Override
			public void clear() {
				throw new UnsupportedOperationException(unsupported);
			}

			@Override
			public Iterator<E> iterator() {
				final Iterator<E> i = c.iterator();
				return new Iterator<E>() {
					public boolean hasNext() {
						return i.hasNext();
					}

					public E next() {
						return i.next();
					}

					public void remove() {
						throw new UnsupportedOperationException(unsupported);
					}
				};
			}

			// Don't delegate hashCode() and equals() -- they have stronger
			// contracts for Set than for Collection

			private final String unsupported = "sets returned by InnigCollections.uniqueCollectionAsSet() are immutable";
		};
	}

	public static <E> void addAll(Collection<E> c, Iterator<? extends E> i) {
		while (i.hasNext())
			c.add(i.next());
	}

	public static <E> void addAll(Collection<E> c, Enumeration<? extends E> e) {
		while (e.hasMoreElements())
			c.add(e.nextElement());
	}

	public static <E> List<E> toList(Iterator<E> i) {
		List<E> l = new ArrayList<E>();
		addAll(l, i);
		return l;
	}

	public static <E> List<E> toList(Enumeration<E> e) {
		List<E> l = new ArrayList<E>();
		addAll(l, e);
		return l;
	}

	public static <E> Set<E> toSet(E... array) {
		return Collections.unmodifiableSet(new HashSet(Arrays.asList(array)));
	}

	public static <E> Iterator<E> asIterator(final Enumeration<E> e) {
		return new Iterator<E>() {
			public boolean hasNext() {
				return e.hasMoreElements();
			}

			public E next() {
				return e.nextElement();
			}

			public void remove() {
				throw new UnsupportedOperationException("Iterator based on Enumeration doesn't support remove()");
			}
		};
	}

	public static <E> Iterator<E> select(final Iterator<E> iter, final Selector<? super E> selector) {
		return new Iterator<E>() {
			public boolean hasNext() {
				if (!dirty)
					return true;
				while (iter.hasNext())
					if (selector.select(nextObject = iter.next())) {
						dirty = false;
						return true;
					}
				nextObject = null;
				return false;
			}

			public E next() throws NoSuchElementException {
				if (!hasNext())
					throw new NoSuchElementException();
				dirty = true;
				E result = nextObject;
				nextObject = null; // allow GC
				return result;
			}

			public void remove() throws UnsupportedOperationException, IllegalStateException {
				iter.remove();
			}

			private boolean dirty = true;
			private E nextObject;
		};
	}

	public static <E> Collection<E> select(final Collection<E> c, final Selector<? super E> selector) {
		return toList(select(c.iterator(), selector));
	}

	public static <E> List<E> select(final List<E> l, final Selector<? super E> selector) {
		return toList(select(l.iterator(), selector));
	}

	public static <E> Set<E> select(final Set<E> s, final Selector<? super E> selector) {
		Set<E> out = new HashSet<E>();
		addAll(out, select(s.iterator(), selector));
		return out;
	}

	public static <E> SortedSet<E> select(final SortedSet<E> s, final Selector<? super E> selector) {
		SortedSet<E> out = new TreeSet<E>(s.comparator());
		addAll(out, select(s.iterator(), selector));
		return out;
	}

	public static <I, O> Iterator<O> map(final Iterator<I> iter, final Mapper<I, O> mapper) {
		return new Iterator<O>() {
			public boolean hasNext() {
				return iter.hasNext();
			}

			public O next() throws NoSuchElementException {
				return mapper.map(iter.next());
			}

			public void remove() throws UnsupportedOperationException, IllegalStateException {
				iter.remove();
			}
		};
	}

	public static <E> SortedSet<E> sort(Collection<E> objects, Comparator<? super E> comparator) {
		SortedSet<E> results = new TreeSet<E>(comparator);
		results.addAll(objects);
		return results;
	}

	/**
	 * Applies a linear-time radix sorting algorithm to a list. This algorithm
	 * is extremely efficient for very large lists for which a radix is
	 * available. It's not particularly good for small lists, however, which are
	 * better served by {@link InnigCollections#sort(Collection,Comparator)}.
	 */
	public static void radixSort(List list, Radix radix) {
		// Set up buckets

		final int bucketCount = radix.getBase() + 1;
		List[] ibuckets = new List[bucketCount], obuckets = new List[bucketCount];
		ibuckets[0] = list;
		final int initialBucketSize = list.size() / bucketCount * 4 / 3 + 2;

		// Sort

		boolean first = true;
		int minPos = radix.getMinPositionForAll(list), maxPos = radix.getMaxPositionForAll(list);
		for (int pos = minPos; pos <= maxPos; pos++) {
			// Put objects in buckets

			for (int n = 0; n < bucketCount; n++)
				if (ibuckets[n] != null && !ibuckets[n].isEmpty())
					for (Iterator i = ibuckets[n].iterator(); i.hasNext();) {
						Object o = i.next();
						int digitp1 = radix.digit(o, pos) + 1;
						List bucket = obuckets[digitp1];
						if (bucket == null)
							bucket = obuckets[digitp1] = new ArrayList(initialBucketSize);
						bucket.add(o);
					}

			// Swap in and out

			List[] swap = ibuckets;
			ibuckets = obuckets;
			obuckets = swap;

			// Clear output buckets

			if (first) {
				first = false;
				obuckets[0] = null; // the input list -- don't use it as a
									// bucket
			} else if (pos < maxPos)
				for (int n = 0; n < bucketCount; n++)
					if (obuckets[n] != null)
						obuckets[n].clear();
		}

		if (first)
			return; // list was all empty strings

		// Accumulate results

		obuckets = null;
		ListIterator listIter = list.listIterator();
		for (int n = 0; n < bucketCount; n++)
			if (ibuckets[n] != null && !ibuckets[n].isEmpty())
				for (Iterator bucketIter = ibuckets[n].iterator(); bucketIter.hasNext();) {
					listIter.next();
					listIter.set(bucketIter.next());
				}
		assert !listIter.hasNext();
	}

	/**
	 * Returns a multi-map which maps the values of the input to its keys.
	 */
	public static <K, V> MultiMap<V, K> inverse(MultiMap<K, V> multiMap) {
		MultiMap<V, K> out = new HashMultiMap<V, K>();
		for (MultiMap.Entry<K, V> entry : multiMap.entrySet())
			out.put(entry.getValue(), entry.getKey());
		return out;
	}

	/**
	 * Returns a multi-map which maps the values of the input to its keys.
	 */
	public static <K, V> MultiMap<V, K> inverse(Map<K, V> map) {
		MultiMap<V, K> out = new HashMultiMap<V, K>();
		for (Map.Entry<K, V> entry : map.entrySet())
			out.put(entry.getValue(), entry.getKey());
		return out;
	}
}
