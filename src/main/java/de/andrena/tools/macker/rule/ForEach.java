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

import de.andrena.tools.macker.event.*;
import de.andrena.tools.macker.structure.ClassInfo;
import de.andrena.tools.macker.structure.ClassManager;

public class ForEach
    extends Rule
    {
    public ForEach(RuleSet parent)
        { super(parent); }
        
    public String getVariableName()
        { return variableName; }
    
    public void setVariableName(String variableName)
        { this.variableName = variableName; }
    
    public String getRegex()
        { return regexS; }
    
    public void setRegex(String regexS)
        throws MackerRegexSyntaxException
        {
        this.regexS = regexS;
        regexPat = new RegexPattern(regexS);
        }
    
    public RuleSet getRuleSet()
        { return ruleSet; }
    
    public void setRuleSet(RuleSet ruleSet)
         { this.ruleSet = ruleSet; }

    public void check(
            EvaluationContext parentContext,
            ClassManager classes)
        throws RulesException, MackerIsMadException, ListenerException
        {
        EvaluationContext context = new EvaluationContext(ruleSet, parentContext);
        
        Set varValues = new TreeSet();
        Set pool = new HashSet();
        for(Iterator p = classes.getPrimaryClasses().iterator(); p.hasNext(); )
            {
            ClassInfo curClass = (ClassInfo) p.next();
            if(getParent().isInSubset(context, curClass))
                {
                pool.add(curClass);
                for(Iterator r = curClass.getReferences().keySet().iterator(); r.hasNext(); )
                    pool.add(r.next());
                }
            }
        
        for(Iterator i = pool.iterator(); i.hasNext(); )
            {
            ClassInfo classInfo = (ClassInfo) i.next();
            String varValue = regexPat.getMatch(parentContext, classInfo);
            if(varValue != null)
                varValues.add(varValue);
            }
        
        context.broadcastEvent(new ForEachStarted(this));
        for(Iterator i = varValues.iterator(); i.hasNext(); )
            {
            String varValue = (String) i.next();
            context.broadcastEvent(new ForEachIterationStarted(this, varValue));
            
            context.setVariableValue(getVariableName(), varValue);
            ruleSet.check(context, classes);

            context.broadcastEvent(new ForEachIterationFinished(this, varValue));
            }
        context.broadcastEvent(new ForEachFinished(this));
        }
    
    private RuleSet ruleSet;
    private String variableName, regexS;
    private RegexPattern regexPat;
    }
