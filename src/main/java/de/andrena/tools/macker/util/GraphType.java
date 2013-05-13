/*______________________________________________________________________________
 * 
 * net.innig.util.GraphType
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

package de.andrena.tools.macker.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import de.andrena.tools.macker.util.collect.GraphWalker;
import de.andrena.tools.macker.util.collect.Graphs;
import de.andrena.tools.macker.util.collect.HashMultiMap;
import de.andrena.tools.macker.util.collect.MultiMap;

/**
 * An enumerated type with directed edges between its members. Useful for
 * modeling types with a natural "is-a" relationship.
 * 
 * <p align="center">
 * <table cellpadding=4 cellspacing=2 border=0 bgcolor="#338833" width="90%">
 * <tr>
 * <td bgcolor="#EEEEEE">
 * <b>Maturity:</b> Brand spankin' new.</td>
 * </tr>
 * <tr>
 * <td bgcolor="#EEEEEE">
 * <b>Plans:</b> Perhaps add JDO support.</td>
 * </tr>
 * </table>
 * 
 * @author Paul Cantrell
 * @version [Development version]
 */
public abstract class GraphType extends EnumeratedType {
	private static final MultiMap childMap = new HashMultiMap(), descendentMap = new HashMultiMap();

	// -----------------------------------------------------------------------------

	public GraphType(String name) {
		this(name, Collections.EMPTY_SET);
	}

	public GraphType(String name, GraphType parent) {
		this(name, Collections.singleton(parent));
	}

	public GraphType(String name, GraphType[] parents) {
		this(name, Arrays.asList(parents));
	}

	public GraphType(String name, Collection/* <GraphType> */parents) {
		super(name);
		setParents(parents);
	}

	protected void setParents(Collection/* <GraphType> */parents) {
		if (parents == null)
			throw new IllegalArgumentException("parents == null");
		if (this.parents != null)
			throw new IllegalArgumentException("You can only set the parents once. Sorry.");

		this.parents = Collections.unmodifiableSet(new HashSet(parents));
		ancestors = Collections.unmodifiableSet(Graphs.reachableNodes(this, new GraphWalker() {
			public Collection getEdgesFrom(Object node) {
				return ((GraphType) node).getParents();
			}
		}));
		// delay children & descendents until all instances are accounted for!
		synchronized (childMap) {
			for (Iterator i = parents.iterator(); i.hasNext();)
				childMap.put(i.next(), this);
			for (Iterator i = ancestors.iterator(); i.hasNext();)
				descendentMap.put(i.next(), this);
		}
	}

	public boolean is(GraphType that) {
		return getAncestors().contains(that);
	}

	public Set getParents() {
		return parents;
	}

	public Set getAncestors() {
		return ancestors;
	}

	public Set getChildren() {
		synchronized (childMap) {
			return emptyIfNull(childMap.get(this));
		}
	}

	public Set getDescendents() {
		synchronized (childMap) {
			return emptyIfNull(descendentMap.get(this));
		}
	}

	private Set emptyIfNull(Set s) {
		return (s == null) ? Collections.EMPTY_SET : s;
	}

	private transient Set parents, ancestors, children, descendents;
}
