/*______________________________________________________________________________
 * 
 * net.innig.util.EnumeratedType
 * 
 *______________________________________________________________________________
 * 
 * Copyright 2001-2002 Kendall Helmstetter Gelner, Paul Cantrell
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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * An alternative to using hand-assigned integer constants for enumerated types.
 * Subclass to create your own enumerated type (a.k.a. "typesafe enum"):
 * <p>
 * 
 * <pre>
 * public final class ColorType extends EnumeratedType
 *    {
 *    static public RED   = new ColorType("red");
 *    static public GREEN = new ColorType("green");
 *    static public BLUE  = new ColorType("blue");
 *    private ColorType( String type ) { super( type ); }
 *    }
 * </pre>
 * <p>
 * There are three important features of an enumerated type class:
 * <ul>
 * <li>It has a <b>private constructor</b>.
 * <li>It declares all its types as <b>static final</b> fields.
 * <li>The class is <b>immutable</b>, meaning that the class is <b>final</b> and
 * so are any fields it declares.
 * </ul>
 * <p>
 * Because they have private constructors, you can use object identity (
 * <code>==</code>) to differentiate enumerated types:
 * 
 * <pre>
 * void kermitCheck(ColorType color) {
 * 	if (color == ColorType.GREEN)
 * 		System.out.println(&quot;It's not easy!&quot;);
 * }
 * </pre>
 * <p>
 * Voila! Compile-time type safety! See <a
 * href="http://java.sun.com/docs/books/effective/">Effective Java</a> #21 for a
 * superb exposition of enumerated types and their advantages, including
 * examples with more conventional brace formatting.
 * <p>
 * This implementation of EnumeratedType keeps an internal pool of the types of
 * a different class. For the class above, the call <blockquote>
 * <code>EnumeratedType.allTypes(ColorType.class)</code></blockquote> would
 * return the set <code>[ColorType.RED, ColorType.GREEN, ColorType.BLUE]</code>.
 * This internal pooling allows you to look up enumerated types by name:
 * <blockquote>
 * <code>EnumeratedType.resolveFromName(ColorType.class, "blue")</code>
 * </blockquote> would return <code>ColorType.BLUE</code>.
 * <p>
 * EnumeratedType serializes correctly, preserving that all-important object
 * identity on the receiving side -- you can use <code>==</code> on enumerated
 * type objects sent over the wire.
 * 
 * <p align="center">
 * <table cellpadding=4 cellspacing=2 border=0 bgcolor="#338833" width="90%">
 * <tr>
 * <td bgcolor="#EEEEEE">
 * <b>Maturity:</b> The serialization/resolve/allTypes support is relatively
 * new. It's succeeded in unit tests, but not real-world tests. The basic
 * enumerated type pattern is thoroughly established and well-worn.</td>
 * </tr>
 * <tr>
 * <td bgcolor="#EEEEEE">
 * <b>Plans:</b> Perhaps add JDO support.</td>
 * </tr>
 * </table>
 * 
 * @author Kendall Helmstetter Gelner, Paul Cantrell
 * @version [Development version]
 */
public abstract class EnumeratedType implements java.io.Serializable {
	/**
	 * Returns the set of valid types for the given enumerated type class. This
	 * does not include subclasses.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given class is not a subclass of EnumeratedType.
	 */
	public static Set/* <EnumeratedType> */allTypes(Class enumTypeClass) {
		checkEnumerated(enumTypeClass);
		return Collections.unmodifiableSet(new HashSet(getValuePool(enumTypeClass).values()));
	}

	/**
	 * Returns the set of valid type names for the given enumerated type class.
	 * This does not include subclasses.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given class is not a subclass of EnumeratedType.
	 */
	public static Set/* <String> */allTypeNames(Class enumTypeClass) {
		checkEnumerated(enumTypeClass);
		return Collections.unmodifiableSet(getValuePool(enumTypeClass).keySet());
	}

	/**
	 * Returns the enumerated type object of a given class with a given name, or
	 * null if there is no type object with that name.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given class is not a subclass of EnumeratedType.
	 */
	public static EnumeratedType resolveFromName(Class enumTypeClass, String name) {
		checkEnumerated(enumTypeClass);
		return (EnumeratedType) getValuePool(enumTypeClass).get(name);
	}

	private static void checkEnumerated(Class enumTypeClass) {
		if (!EnumeratedType.class.isAssignableFrom(enumTypeClass))
			throw new IllegalArgumentException(enumTypeClass.getName() + " is not a subclass of "
					+ EnumeratedType.class.getName());
	}

	private static Map/* <String,EnumeratedType> */getValuePool(Class c) {
		synchronized (valuePoolPool) {
			Map valuePool = (Map) valuePoolPool.get(c);
			if (valuePool == null)
				valuePoolPool.put(c, valuePool = new TreeMap());
			return valuePool;
		}
	}

	private static Map/* <Class,Map<String,EnumeratedType>> */valuePoolPool = new HashMap();

	// -----------------------------------------------------------------------------------------

	/**
	 * Creates a new enumerated type with a name unique to the type's class.
	 * 
	 * @throws IllegalStateException
	 *             if a type with the given name is already registered for this
	 *             class.
	 */
	protected EnumeratedType(String name) {
		this.name = name;
		synchronized (valuePoolPool) {
			Map pool = getValuePool(getClass());
			if (pool.get(name) != null)
				throw new IllegalStateException("Duplicate enumerated type name for " + getClass() + ": \"" + name
						+ '"');
			pool.put(name, this);
		}
	}

	/**
	 * Returns the name of this enumerated type.
	 */
	public final String getName() {
		return name;
	}

	/**
	 * Supports serialization of enumerated types.
	 */
	protected final Object readResolve() {
		return resolveFromName(getClass(), getName());
	}

	@Override
	public String toString() {
		return getName();
	}

	/**
	 * Enumerated types always use object identity.
	 */
	@Override
	public final boolean equals(Object that) {
		return this == that;
	}

	@Override
	public final int hashCode() {
		return super.hashCode();
	}

	private final String name;
}
