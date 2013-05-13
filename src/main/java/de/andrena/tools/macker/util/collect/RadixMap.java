/*______________________________________________________________________________
 * 
 * net.innig.util.RadixMap
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

import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import de.andrena.tools.macker.util.EnumeratedType;

/**
 * A map which theoretically has fast lookups, but doesn't actually work very
 * well (yet).
 * 
 * <p align="center">
 * <table cellpadding=4 cellspacing=2 border=0 bgcolor="#338833" width="90%">
 * <tr>
 * <td bgcolor="#EEEEEE">
 * <b>Maturity:</b> All the radix utilities in innig-util are completely
 * experimental. (In particular, this class is not completely implemented!) They
 * may stay; they may improve; they may go away.</td>
 * </tr>
 * <tr>
 * <td bgcolor="#EEEEEE">
 * <b>Plans:</b> Experiment.</td>
 * </tr>
 * </table>
 */
public class RadixMap extends AbstractMap {
	public RadixMap(Radix radix) {
		this.radix = radix;
	}

	public RadixMap(Radix radix, Map otherMap) {
		this(radix);
		putAll(otherMap);
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public boolean isEmpty() {
		return size == 0;
	}

	@Override
	public boolean containsKey(Object key) {
		RadixTree tree = getRadixTree(key, false);
		return (tree == null) ? true : tree.hasValue();
	}

	@Override
	public Object get(Object key) {
		RadixTree tree = getRadixTree(key, false);
		return (tree == null) ? null : tree.getValue();
	}

	@Override
	public Object put(Object key, Object value) {
		RadixTree tree = getRadixTree(key, true);
		Object oldValue = tree.getValue();
		tree.setValue(value);
		size++;
		version++;
		return oldValue;
	}

	@Override
	public Object remove(Object key) {
		RadixTree tree = getRadixTree(key, false);
		if (tree == null || !tree.hasValue())
			return null;
		Object oldValue = tree.getValue();
		tree.removeValue();
		size--;
		version++;
		// trim();
		return oldValue;
	}

	@Override
	public void clear() {
		version++;
		size = 0;
		root = null;
	}

	@Override
	public Set keySet() {
		if (keys == null)
			keys = new AbstractSet() {
				@Override
				public java.util.Iterator iterator() {
					return new Iterator(root.getInitialPosition(), root, IteratorType.ENTRIES);
				}

				@Override
				public int size() {
					return size;
				}

				@Override
				public boolean remove(Object obj) {
					return RadixMap.this.remove(obj) != null;
				}

				@Override
				public void clear() {
					RadixMap.this.clear();
				}
			};
		return keys;
	}

	@Override
	public Collection values() {
		if (values == null)
			values = new AbstractCollection() {
				@Override
				public java.util.Iterator iterator() {
					return new Iterator(root.getInitialPosition(), root, IteratorType.VALUES);
				}

				@Override
				public int size() {
					return RadixMap.this.size();
				}

				@Override
				public void clear() {
					RadixMap.this.clear();
				}
			};
		return values;
	}

	@Override
	public Set entrySet() {
		if (entries == null)
			entries = new AbstractSet() {
				@Override
				public java.util.Iterator iterator() {
					return new Iterator(root.getInitialPosition(), root, IteratorType.ENTRIES);
				}

				@Override
				public int size() {
					return size;
				}

				@Override
				public boolean remove(Object obj) {
					if (!(obj instanceof RadixMap.Entry))
						return false;
					RadixMap.Entry entry = (RadixMap.Entry) obj;
					return RadixMap.this.remove(entry.getKey()) != null;
				}

				@Override
				public void clear() {
					RadixMap.this.clear();
				}
			};
		return entries;
	}

	public Radix getRadix() {
		return radix;
	}

	private RadixTree getRadixTree(Object value, boolean create) {
		int valueMaxPos = radix.getMaxPosition(value), valueMinPos = radix.getMinPosition(value);

		if (root == null || root.getInitialPosition() < valueMaxPos) {
			if (!create)
				return null;
			// inserting ahead of root ... tricky
			// root = new RadixTree(valueMaxPos);
			// ////////////
			// return ..............;

			root = new RadixTree(value, valueMaxPos, valueMinPos);

		}

		RadixTree curTree = root;
		for (int pos = valueMaxPos; pos >= valueMinPos; pos--) {
			int digit = radix.digit(value, pos);
			RadixTree nextTree = curTree.getChild(pos, digit);
			if (nextTree == null) {
				if (!create)
					return null;
				nextTree = new RadixTree(value, pos - 1, valueMinPos);
				curTree.setChild(pos, digit, nextTree);
				// return curTree;
			}
			curTree = nextTree;
		}
		return curTree; // ?
	}

	private class RadixTree {
		public RadixTree(Object value, int maxPos, int minPos) {
			this.minPos = this.maxPos = maxPos;
			if (minPos <= maxPos)
				setChild(maxPos, radix.digit(value, maxPos), new RadixTree(value, maxPos - 1, minPos));
		}

		public int getInitialPosition() {
			return minPos;
		}

		public RadixTree getChild(int position, int digit) {
			if (position < minPos || position > maxPos)
				throw new IllegalArgumentException("illegal position (" + position + " not in [" + minPos + ","
						+ maxPos + "])");
			if (children == null)
				return null;
			return children[digit];
		}

		public void setChild(int position, int digit, RadixTree tree) {
			if (position < minPos || position > maxPos)
				throw new IllegalArgumentException("illegal position (" + position + " not in [" + minPos + ","
						+ maxPos + "])");
			if (digit < 0 || digit > radix.getBase())
				throw new IllegalArgumentException("illegal digit (" + digit + " not in [" + 0 + ","
						+ (children.length - 1) + "])");
			if (children == null)
				children = new RadixTree[radix.getBase()];
			if ((children[digit] == null) != (tree == null))
				childCount += (tree == null) ? -1 : 1;
			children[digit] = tree;
		}

		public boolean hasValue() {
			return hasValue;
		}

		public Object getValue() {
			return value;
		}

		public void setValue(Object value) {
			hasValue = true;
			this.value = value;
		}

		public void removeValue() {
			hasValue = false;
			value = null;
		}

		public boolean isEmpty() {
			return !hasValue && childCount == 0;
		}

		public void dumpTree(int i) {
			for (int n = i; n > 0; n--)
				System.out.print(' ');
			System.out.println("tree @ " + maxPos);
			if (hasValue) {
				for (int n = i; n > 0; n--)
					System.out.print(' ');
				System.out.println("value: " + value);
			}
			if (children != null)
				for (int child = 0; child < children.length; child++)
					if (children[child] != null) {
						for (int n = i; n > 0; n--)
							System.out.print(' ');
						System.out.println(child + ": ");
						children[child].dumpTree(i + 3);
					}
		}

		private int minPos, maxPos;
		private boolean hasValue;
		private Object value;
		private int childCount;
		private RadixTree[] children;
	}

	private class Entry implements Map.Entry {
		public Entry(Object key, Object value) {
			this.key = key;
			this.value = value;
		}

		public Object getKey() {
			return key;
		}

		public Object getValue() {
			return value;
		}

		public Object setValue(Object value) {
			Object oldValue = value;
			this.value = value;
			return oldValue;
		}

		@Override
		public boolean equals(Object that) {
			if (this == that)
				return true;
			if (that == null || !(that instanceof Map.Entry))
				return false;
			Map.Entry thatEntry = (Map.Entry) that;
			return key.equals(thatEntry.getKey())
					&& (value == null ? thatEntry.getValue() == null : value.equals(thatEntry.getValue()));
		}

		@Override
		public int hashCode() {
			return key.hashCode() + (value == null ? 0 : value.hashCode() * 23);
		}

		private Object key;
		private Object value;
	}

	private static class IteratorType extends EnumeratedType {
		public static final IteratorType KEYS = new IteratorType("keys"), VALUES = new IteratorType("values"),
				ENTRIES = new IteratorType("entries");

		private IteratorType(String name) {
			super(name);
		}
	}

	private class Iterator implements java.util.Iterator {
		public Iterator(int position, RadixTree tree, IteratorType type) {
			this.position = position;
			this.tree = tree;
			this.type = type;
			expectedVersion = version;
			curDigit = -1;
			if (tree != null)
				handledValue = !tree.hasValue();
		}

		public boolean hasNext() {
			checkModification();
			if (tree == null)
				return false;
			if (curIter != null && curIter.hasNext())
				return true;
			if (!handledValue)
				return true;
			for (nextDigit = curDigit + 1; nextDigit < radix.getBase(); nextDigit++) {
				RadixTree subTree = tree.getChild(position, nextDigit);
				if (subTree != null) {
					nextIter = new Iterator(position - 1, subTree, type);
					if (nextIter.hasNext())
						return true;
				}
			}
			return false;
		}

		public Object next() {
			if (!hasNext())
				throw new NoSuchElementException();

			if (!handledValue) {
				handledValue = true;
				if (type == IteratorType.VALUES)
					return tree.getValue();
				throw new UnsupportedOperationException();
			}

			curDigit = nextDigit;
			curIter = nextIter;
			return curIter.next();
		}

		public void remove() {
			if (curIter != null) {
				curIter.remove();
				expectedVersion = version;
				return;
			}
			checkModification();
			if (tree == null || !tree.hasValue())
				throw new IllegalStateException("no element to remove");
			tree.removeValue();
			size--;
			expectedVersion = ++version;
		}

		private void checkModification() {
			if (version != expectedVersion)
				throw new ConcurrentModificationException("radix map modified (" + version + " != " + expectedVersion
						+ ")");
		}

		private final int position;
		private final RadixTree tree;
		private final IteratorType type;
		private long expectedVersion;
		private int curDigit, nextDigit;
		private Iterator curIter, nextIter;
		private boolean handledValue;
	}

	private int size;
	private Radix radix;
	private RadixTree root;
	private Set keys, entries;
	private Collection values;

	private transient long version;

	@Override
	public String toString() {
		return getClass().getName();
	}

	public void dump() {
		root.dumpTree(0);
	}
}
