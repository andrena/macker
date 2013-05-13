/*______________________________________________________________________________
 * 
 * net.innig.util.Radix
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

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Extracts a radix representation -- a sequence of digits -- from objects.
 * Radix representations allow certain sorting and lookup algorithms which are
 * more efficient in large cases than those which are possible with comparison
 * alone.
 * 
 * <p align="center">
 * <table cellpadding=4 cellspacing=2 border=0 bgcolor="#338833" width="90%">
 * <tr>
 * <td bgcolor="#EEEEEE">
 * <b>Maturity:</b> All the radix utilities in innig-util are completely
 * experimental. They mostly work, but perform poorly. They may stay; they may
 * improve; they may go away.</td>
 * </tr>
 * <tr>
 * <td bgcolor="#EEEEEE">
 * <b>Plans:</b> Experiment.</td>
 * </tr>
 * </table>
 * 
 * @see InnigCollections#radixSort(List,Radix)
 * @see RadixMap
 */
public interface Radix extends Comparator {
	/**
	 * Returns the base (the number of digit values) in this radix. For the
	 * decimal radix we're familiar with, for example, the base is 10.
	 */
	public int getBase();

	/**
	 * Returns the digits of the radix representation of the given object. Lower
	 * positions are less significant digits.
	 * 
	 * @param o
	 *            An object from which this radix can extract digits.
	 * @param position
	 *            A digit position within <tt>o</tt>.
	 * @return a digit <i>d</i>, with -1 &lt;= <i>x</i> &lt; getBase(). A
	 *         variable-length radix uses the digit -1 signifies that the
	 *         position is beyond the end of the object's representation.
	 * @throws ClassCastException
	 *             if this radix doesn't handle the given object.
	 */
	public int digit(Object o, int position);

	/**
	 * Returns the position of the most significant digit for this radix. Can be
	 * negative.
	 * 
	 * @throws ClassCastException
	 *             if this radix doesn't handle the object
	 */
	public int getMaxPosition(Object o);

	/**
	 * Returns the position of the most significant digit for this radix. Can be
	 * negative.
	 * 
	 * @throws ClassCastException
	 *             if this radix doesn't handle the object
	 */
	public int getMinPosition(Object o);

	/**
	 * Returns the hightest position of the most significant digit of any of the
	 * values in the collection. If the radix is not variable-length, this
	 * method needn't scan the collection. Can be negative.
	 * 
	 * @throws ClassCastException
	 *             if this radix doesn't handle an object in the collection.
	 *             This exception is not guaranteed to be thrown if the list
	 *             contains an unhandled object, since some implementations may
	 *             not actually scan the values.
	 */
	public int getMaxPositionForAll(Collection values);

	/**
	 * Returns the lowest position of the least significant digit of any of the
	 * values in the collection. If the radix is not variable-length, this
	 * method needn't scan the collection. Can be negative.
	 * 
	 * @throws ClassCastException
	 *             if this radix doesn't handle an object in the collection.
	 *             This exception is not guaranteed to be thrown if the list
	 *             contains an unhandled object, since some implementations may
	 *             not actually scan the values.
	 */
	public int getMinPositionForAll(Collection values);

	/**
	 * Constructs an object from the given digits (optional operation). This
	 * operation inverts @{#digit(Object,int)}. Element 0 of the array
	 * corresponds to the digit in the position given by
	 * {@link #getMaxPosition(Object)}.
	 * 
	 * @throws UnsupportedOperationException
	 *             if this radix doesn't support this method.
	 * @throws IllegalArgumentException
	 *             if any of the digits exceed the base, any digits are
	 *             negative, the number of digits is too large, or the digits
	 *             don't represent a valid object.
	 */
	public Object objectFromDigits(int[] digits);

	/**
	 * Constructs an object from the given digits (optional operation). This
	 * operation inverts @{#digit(Object,int)}. The element of the array at the
	 * given offset corresponds to the digit in the position given by
	 * {@link #getMaxPosition(Object)}.
	 * 
	 * @throws UnsupportedOperationException
	 *             if this radix doesn't support this method.
	 * @throws IllegalArgumentException
	 *             if any of the digits exceed the base, any digits are
	 *             negative, the number of digits is too large, or the digits
	 *             don't represent a valid object.
	 */
	public Object objectFromDigits(int[] digits, int offset, int len);
}
