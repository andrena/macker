/*______________________________________________________________________________
 *
 * Macker   http://innig.net/macker/
 *
 * Copyright 2002 Paul Cantrell
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
 
package net.innig.macker.rule;

import java.util.*;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

public final class VariableParser
    {
    public static String parse(EvaluationContext context, String inS)
        throws UndeclaredVariableException
        {
        StringBuffer outS = new StringBuffer();
        for(int pos = 0; pos >= 0; )
            {
            boolean hasAnotherVar = var.match(inS, pos);
            int expEnd = hasAnotherVar ? var.getParenStart(0) : inS.length();
            
            if(pos < expEnd)
                outS.append(inS.substring(pos, expEnd));
            if(hasAnotherVar)
                outS.append(context.getVariableValue(var.getParen(1)));
            
            pos = hasAnotherVar ? var.getParenEnd(0) : -1;
            }
        return outS.toString();
        }
    
    static private RE var;
    static
        {
        try
            { var = new RE("\\$\\{([A-Za-z0-9_\\.\\-]+)\\}"); }
        catch(RESyntaxException rese)
            {
            rese.printStackTrace(System.out);
            throw new RuntimeException("Can't initialize VariableParser: " + rese);
            }
        }
    
    private VariableParser() { }
    }

