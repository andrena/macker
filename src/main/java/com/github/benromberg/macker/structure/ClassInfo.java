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

import java.util.*;
import net.innig.collect.*;

/**
    Information about a class's structure.
*/
public interface ClassInfo
    extends Comparable
    {
    public ClassManager getClassManager();
    public boolean isComplete();

    public String getFullName();
    public String getClassName();
    public String getPackageName();

    public boolean isInterface();
    public boolean isAbstract();
    public boolean isFinal();
    public AccessModifier getAccessModifier();
    
    public ClassInfo getExtends();
    public Set/*<ClassInfo>*/ getImplements();
    public Set/*<ClassInfo>*/ getDirectSupertypes();
    public Set/*<ClassInfo>*/ getSupertypes();

    public MultiMap/*<ClassInfo,Reference>*/ getReferences();
    }

