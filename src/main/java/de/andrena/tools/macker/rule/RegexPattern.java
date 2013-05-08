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
 
package de.andrena.tools.macker.rule;


import java.util.*;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

import de.andrena.tools.macker.structure.ClassInfo;

public final class RegexPattern
    implements Pattern
    {
    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    public RegexPattern(String regexStr)
        throws MackerRegexSyntaxException
        { regex = new MackerRegex(regexStr); }
        
    //--------------------------------------------------------------------------
    // Properties
    //--------------------------------------------------------------------------

    public MackerRegex getRegex()
        { return regex; }
    
    private final MackerRegex regex;

    //--------------------------------------------------------------------------
    // Evaluation
    //--------------------------------------------------------------------------

    public boolean matches(EvaluationContext context, ClassInfo classInfo)
        throws RulesException
        { return regex.matches(context, classInfo.getFullName()); }
    
    public String getMatch(EvaluationContext context, ClassInfo classInfo)
        throws RulesException
        { return regex.getMatch(context, classInfo.getFullName()); }
    
    public String toString()
        { return regex.toString(); }
    }

