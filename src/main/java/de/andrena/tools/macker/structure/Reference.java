/*______________________________________________________________________________
 *
 * Macker   http://innig.net/macker/
 *
 * Copyright 2002-2003 Paul Cantrell
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License version 2, as published by the
 * Free Software Foundation. See the file LICENSE.html for more information.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, including the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the license for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc. / 59 Temple
 * Place, Suite 330 / Boston, MA 02111-1307 / USA.
 *______________________________________________________________________________
 */

package de.andrena.tools.macker.structure;

/**
 * A reference from one class to another.
 */
public final class Reference {
	public Reference(ClassInfo from, ClassInfo to, ReferenceType type, String memberName, AccessModifier memberAccess) {
		if (from == null)
			throw new IllegalArgumentException("from is null");
		if (to == null)
			throw new IllegalArgumentException("to is null");
		if (type == null)
			throw new IllegalArgumentException("type is null");
		if ((memberName == null) != (memberAccess == null))
			throw new IllegalArgumentException("memberName and memberAccess must both be present or both be absent");
		this.from = from;
		this.to = to;
		this.type = type;
		this.memberName = memberName;
		this.memberAccess = memberAccess;
	}

	public ClassInfo getFrom() {
		return from;
	}

	public ClassInfo getTo() {
		return to;
	}

	public ReferenceType getType() {
		return type;
	}

	public String getMemberName() {
		return memberName;
	}

	public AccessModifier getMemberAccess() {
		return memberAccess;
	}

	// public String getFileName() { }
	// public int getLine()

	@Override
	public boolean equals(Object that) {
		if (this == that)
			return true;
		if (that == null)
			return false;
		if (this.getClass() != that.getClass())
			return false;
		Reference thatRef = (Reference) that;
		if (this.memberName == null && thatRef.memberName != null)
			return false;
		if (this.memberAccess == null && thatRef.memberAccess != null)
			return false;
		return this.from.equals(thatRef.from) && this.to.equals(thatRef.to) && this.type.equals(thatRef.type);
	}

	@Override
	public int hashCode() {
		return from.hashCode() ^ to.hashCode() * 17 ^ type.hashCode() * 103
				^ (memberName == null ? 0 : memberName.hashCode() * 23)
				^ (memberAccess == null ? 0 : memberAccess.hashCode() * 5);
	}

	@Override
	public String toString() {
		return "Ref(" + from + " -> " + to + ", " + type
				+ (memberAccess == null ? "" : ": " + memberAccess + " " + memberName) + ')';
	}

	private final ClassInfo from, to;
	private final ReferenceType type;
	private final String memberName;
	private final AccessModifier memberAccess;
}
