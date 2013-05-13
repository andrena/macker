/*______________________________________________________________________________
 * 
 * Copyright 2000-2001 Paul Cantrell
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Graph utilities.
 * 
 * <p align="center">
 * <table cellpadding=4 cellspacing=2 border=0 bgcolor="#338833" width="90%">
 * <tr>
 * <td bgcolor="#EEEEEE">
 * <b>Maturity:</b> This is obviously a very immature API. The implementation of
 * the single method in it, however, works quite well.</td>
 * </tr>
 * <tr>
 * <td bgcolor="#EEEEEE">
 * <b>Plans:</b> In the future, innig-util will include more complete graph
 * handling utilities.</td>
 * </tr>
 * </table>
 * 
 * @author Paul Cantrell
 * @version [Development version]
 */

public abstract class Graphs {
	/**
	 * Returns the set of all nodes reachable along directed paths from a given
	 * node in a given graph. The implementation is smart about cycle detection.
	 */

	public static <N> Set<N> reachableNodes(N initial, GraphWalker<N> walker) {
		return reachableNodesFromSet(Collections.singleton(initial), walker);
	}

	/**
	 * Returns the set of all nodes reachable along directed paths from a given
	 * set of nodes in a given graph. The implementation is smart about cycle
	 * detection.
	 */

	public static <N> Set<N> reachableNodesFromSet(Set<N> initial, GraphWalker<N> walker) {
		Set<N> nodes = new HashSet<N>(), newNodes = initial, newerNodes = new HashSet<N>();

		while (!newNodes.isEmpty()) {
			nodes.addAll(newNodes); // Put the new nodes in the current list.

			// Now put all the newly reachable nodes a list of even newer nodes.

			for (N newNode : newNodes)
				newerNodes.addAll(walker.getEdgesFrom(newNode));

			// Add unwalked newer nodes for next time

			newerNodes.removeAll(nodes);
			newNodes = newerNodes;
			newerNodes = new HashSet<N>();
		}

		return nodes;
	}

	public static <N> Set<List<N>> findCycles(N initial, GraphWalker<N> walker) {
		Set<List<N>> cycles = new HashSet<List<N>>(); // ! change to
														// IdentityHashSet
		findCycles(initial, cycles, walker, new ArrayList<N>(), new HashSet<N>()); // !
																					// change
																					// to
																					// IdentityHashSet
		return cycles;
	}

	private static <N> void findCycles(N node, Set<List<N>> cycles, GraphWalker<N> walker, List<N> curPath,
			Set<N> visited) {
		curPath.add(node);

		if (visited.contains(node))
			cycles.add(Collections.unmodifiableList(new ArrayList<N>(curPath))); // !
																					// wastes
																					// 10%
		else {
			visited.add(node);
			for (N neighbor : walker.getEdgesFrom(node))
				findCycles(neighbor, cycles, walker, curPath, visited);
			visited.remove(node);
		}

		curPath.remove(curPath.size() - 1);
	}

	private Graphs() {
	} // To prevent instantiation
}
