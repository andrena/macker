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
 
package com.github.benromberg.macker.structure;

import java.util.Set;
import net.innig.collect.MultiMap;

/**
    Holds a class name, and lazily loads other attributes.
*/
public class HollowClassInfo
    extends AbstractClassInfo
    {
    public HollowClassInfo(ClassManager classManager, String className)
        {
        super(classManager);
        this.className = className;
        }
    
    public String getFullName()
        { return className; }

    public boolean isComplete()                { return getActual().isComplete(); }
    public boolean isInterface()               { return getActual().isInterface(); }
    public boolean isAbstract()                { return getActual().isAbstract(); }
    public boolean isFinal()                   { return getActual().isFinal(); }
    public AccessModifier getAccessModifier()  { return getActual().getAccessModifier(); }
    public ClassInfo getExtends()              { return getActual().getExtends(); }
    public Set getImplements()                 { return getActual().getImplements(); }
    public Set getDirectSupertypes()           { return getActual().getDirectSupertypes(); }
    public Set getSupertypes()                 { return getActual().getSupertypes(); }
    public MultiMap getReferences()            { return getActual().getReferences(); }

    private ClassInfo getActual()
        {
        if(actual == null)
            actual = getClassManager().loadClassInfo(className);
        return actual;
        }
    
    private String className;
    private ClassInfo actual;
    }


