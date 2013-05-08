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

import de.andrena.tools.macker.event.ListenerException;
import de.andrena.tools.macker.event.MackerEvent;
import de.andrena.tools.macker.event.MackerEventListener;
import de.andrena.tools.macker.event.MackerIsMadException;
import de.andrena.tools.macker.structure.ClassManager;

public class EvaluationContext
    {
    public EvaluationContext(ClassManager classManager, RuleSet ruleSet)
        {
        this.classManager = classManager;
        this.ruleSet = ruleSet;
        varValues = new HashMap();
        listeners = new HashSet();
        }
    
    public EvaluationContext(RuleSet ruleSet, EvaluationContext parent)
        {
        this(parent.getClassManager(), ruleSet);
        this.parent = parent;
        }
    
    public EvaluationContext(EvaluationContext parent)
        { this(parent.getRuleSet(), parent); }
    
    public EvaluationContext getParent()
        { return parent; }
    
    public ClassManager getClassManager()
        { return classManager; }
    
    public RuleSet getRuleSet()
        { return ruleSet; }
    
    public void setVariableValue(String name, String value)
        throws UndeclaredVariableException
        { varValues.put(name, (value == null) ? "" : VariableParser.parse(this, value)); }
    
    public String getVariableValue(String name)
        throws UndeclaredVariableException
        {
        String value = (String) varValues.get(name);
        if(value != null)
            return value;
        if(parent != null)
            return parent.getVariableValue(name);
        throw new UndeclaredVariableException(name);
        }
        
    public void setVariables(Map/*<String,String>*/ vars)
        { varValues.putAll(vars); }
    
    public void addListener(MackerEventListener listener)
        { listeners.add(listener); }
        
    public void removeListener(MackerEventListener listener)
        { listeners.remove(listener); }
    
    public void broadcastStarted()
        throws ListenerException
        { broadcastStarted(getRuleSet()); }
        
    protected void broadcastStarted(RuleSet targetRuleSet)
        throws ListenerException
        {
        for(Iterator i = listeners.iterator(); i.hasNext(); )
            ((MackerEventListener) i.next()).mackerStarted(targetRuleSet);
        if(getParent() != null)
            getParent().broadcastStarted(targetRuleSet);
        }
    
    public void broadcastFinished()
        throws MackerIsMadException, ListenerException
        { broadcastFinished(getRuleSet()); }
        
    protected void broadcastFinished(RuleSet targetRuleSet)
        throws MackerIsMadException, ListenerException
        {
        for(Iterator i = listeners.iterator(); i.hasNext(); )
            ((MackerEventListener) i.next()).mackerFinished(targetRuleSet);
        if(getParent() != null)
            getParent().broadcastFinished(targetRuleSet);
        }
    
    public void broadcastAborted()
        { broadcastAborted(getRuleSet()); }
        
    protected void broadcastAborted(RuleSet targetRuleSet)
        {
        for(Iterator i = listeners.iterator(); i.hasNext(); )
            ((MackerEventListener) i.next()).mackerAborted(targetRuleSet);
        if(getParent() != null)
            getParent().broadcastAborted(targetRuleSet);
        }
    
    public void broadcastEvent(MackerEvent event)
        throws MackerIsMadException, ListenerException
        { broadcastEvent(event, getRuleSet()); }
        
    protected void broadcastEvent(MackerEvent event, RuleSet targetRuleSet)
        throws MackerIsMadException, ListenerException
        {
        for(Iterator i = listeners.iterator(); i.hasNext(); )
            ((MackerEventListener) i.next()).handleMackerEvent(targetRuleSet, event);
        if(getParent() != null)
            getParent().broadcastEvent(event, targetRuleSet);
        }
        
    private RuleSet ruleSet;
    private EvaluationContext parent;
    private Map varValues;
    private Set listeners;
    private ClassManager classManager;
    }



