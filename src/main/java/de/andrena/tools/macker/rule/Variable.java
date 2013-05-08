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
 
package de.andrena.tools.macker.rule;


import java.util.*;

import de.andrena.tools.macker.event.MackerIsMadException;
import de.andrena.tools.macker.structure.ClassInfo;
import de.andrena.tools.macker.structure.ClassManager;

public class Variable
    extends Rule
    {
    public Variable(RuleSet parent, String name, String value)
        {
        super(parent);
        setVariableName(name);
        setValue(value);
        }
        
    public String getVariableName()
        { return variableName; }
    
    public void setVariableName(String variableName)
        { this.variableName = variableName; }
    
    public String getValue()
        { return value; }
    
    public void setValue(String value)
        { this.value = value;}

    public void check(
            EvaluationContext context,
            ClassManager classes)
        throws RulesException, MackerIsMadException
        {
        context.setVariableValue(getVariableName(), getValue());
        }
    
    private String variableName, value;
    }