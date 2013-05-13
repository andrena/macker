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

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import de.andrena.tools.macker.util.collect.InnigCollections;
import de.andrena.tools.macker.util.collect.MultiMap;

/**
 * Hard-coded class info for Java's primitive types.
 */
public enum PrimitiveTypeInfo implements ClassInfo {
	BYTE, SHORT, INT, LONG, CHAR, BOOLEAN, FLOAT, DOUBLE, VOID;

	public static final Set<PrimitiveTypeInfo> ALL = Collections
			.unmodifiableSet(EnumSet.allOf(PrimitiveTypeInfo.class));

	public ClassManager getClassManager() {
		return null;
	}

	public boolean isComplete() {
		return true;
	}

	public String getName() {
		return toString().toLowerCase();
	}

	public String getFullName() {
		return getName();
	}

	public String getClassName() {
		return getName();
	}

	public String getPackageName() {
		return null;
	}

	public boolean isInterface() {
		return false;
	}

	public boolean isAbstract() {
		return false;
	}

	public boolean isFinal() {
		return true;
	}

	public AccessModifier getAccessModifier() {
		return AccessModifier.PUBLIC;
	}

	public ClassInfo getExtends() {
		return null;
	}

	public Set<ClassInfo> getImplements() {
		return Collections.emptySet();
	}

	public Set<ClassInfo> getDirectSupertypes() {
		return Collections.emptySet();
	}

	public Set<ClassInfo> getSupertypes() {
		return Collections.emptySet();
	}

	public MultiMap<ClassInfo, Reference> getReferences() {
		return InnigCollections.emptyMultimap();
	}
}
