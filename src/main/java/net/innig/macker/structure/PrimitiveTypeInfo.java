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
 
package net.innig.macker.structure;

import java.util.*;
import net.innig.collect.InnigCollections;
import net.innig.collect.MultiMap;
import net.innig.util.EnumeratedType;

/**
    Hard-coded class info for Java's primitive types.
*/
public final class PrimitiveTypeInfo
    extends EnumeratedType
    implements ClassInfo
    {
    public static PrimitiveTypeInfo getPrimitiveTypeInfo(String typeName)
        { return (PrimitiveTypeInfo) EnumeratedType.resolveFromName(PrimitiveTypeInfo.class, typeName); }
    
    public static final PrimitiveTypeInfo
        BYTE    = new PrimitiveTypeInfo("byte"),
        SHORT   = new PrimitiveTypeInfo("short"),
        INT     = new PrimitiveTypeInfo("int"),
        LONG    = new PrimitiveTypeInfo("long"),
        CHAR    = new PrimitiveTypeInfo("char"),
        BOOLEAN = new PrimitiveTypeInfo("boolean"),
        FLOAT   = new PrimitiveTypeInfo("float"),
        DOUBLE  = new PrimitiveTypeInfo("double"),
        VOID    = new PrimitiveTypeInfo("void");
    
    public static final Set/*<PrimitiveTypeInfo>*/ ALL =
        EnumeratedType.allTypes(PrimitiveTypeInfo.class);

    private PrimitiveTypeInfo(String className)
        { super(className); }
    
    public ClassManager getClassManager()
        { return null; }
    
    public boolean isComplete()
        { return true; }
    
    public String getFullName()      { return getName(); }
    public String getClassName() { return getName(); }
    public String getPackageName()    { return null; }
    
    public boolean isInterface() { return false; }
    public boolean isAbstract()  { return false; }
    public boolean isFinal()     { return true; }
    public AccessModifier getAccessModifier() { return AccessModifier.PUBLIC; }
    
    public ClassInfo getExtends()                   { return null; }
    public Set/*<ClassInfo>*/ getImplements()       { return Collections.EMPTY_SET; }
    public Set/*<ClassInfo>*/ getDirectSupertypes() { return Collections.EMPTY_SET; }
    public Set/*<ClassInfo>*/ getSupertypes()       { return Collections.EMPTY_SET; }
    public MultiMap/*<ClassInfo,Reference>*/ getReferences()
        { return InnigCollections.EMPTY_MULTIMAP; }
    
    public int compareTo(Object that)
        { return getFullName().compareTo(((ClassInfo) that).getFullName()); }
    }


