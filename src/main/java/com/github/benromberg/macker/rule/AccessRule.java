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
 
package com.github.benromberg.macker.rule;


import java.util.*;

import com.github.benromberg.macker.event.AccessRuleViolation;
import com.github.benromberg.macker.event.ListenerException;
import com.github.benromberg.macker.event.MackerIsMadException;
import com.github.benromberg.macker.structure.ClassInfo;
import com.github.benromberg.macker.structure.ClassManager;
import com.github.benromberg.macker.util.IncludeExcludeLogic;
import com.github.benromberg.macker.util.IncludeExcludeNode;

import net.innig.collect.MultiMap;

public class AccessRule
    extends Rule
    {
    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    public AccessRule(RuleSet parent)
        {
        super(parent);
        type = AccessRuleType.DENY;
        from = to = Pattern.ALL;
        }
    
    //--------------------------------------------------------------------------
    // Properties
    //--------------------------------------------------------------------------

    public AccessRuleType getType()
        { return type; }
    
    public void setType(AccessRuleType type)
        {
        if(type == null)
            throw new NullPointerException("type parameter cannot be null");
        this.type = type;
        }
    
    public Pattern getFrom()
        { return from; }
    
    public void setFrom(Pattern from)
        { this.from = from; }
    
    public String getMessage()
        { return message; }
    
    public void setMessage(String message)
        { this.message = message; }

    public Pattern getTo()
        { return to; }
    
    public void setTo(Pattern to)
        { this.to = to; }
    
    public AccessRule getChild()
        { return child; }
    
    public void setChild(AccessRule child)
        { this.child = child; }
    
    public AccessRule getNext()
        { return next; }
    
    public void setNext(AccessRule next)
        { this.next = next; }
    
    private AccessRuleType type;
    private Pattern from, to;
    private String message;
    private boolean bound;
    private AccessRule child, next;

    //--------------------------------------------------------------------------
    // Evaluation
    //--------------------------------------------------------------------------

    public void check(EvaluationContext context, ClassManager classes)
        throws RulesException, MackerIsMadException, ListenerException
        {
        EvaluationContext localContext = new EvaluationContext(context);
        for(Iterator refIter = classes.getReferences().entrySet().iterator(); refIter.hasNext(); )
            {
            MultiMap.Entry entry = (MultiMap.Entry) refIter.next();
            ClassInfo from = (ClassInfo) entry.getKey();
            ClassInfo to   = (ClassInfo) entry.getValue();
            if(from.equals(to))
                continue;
            if(!localContext.getRuleSet().isInSubset(localContext, from))
                continue;

            localContext.setVariableValue("from", from.getClassName());
            localContext.setVariableValue("to",     to.getClassName());
            localContext.setVariableValue("from-package", from.getPackageName());
            localContext.setVariableValue("to-package",     to.getPackageName());
            localContext.setVariableValue("from-full", from.getFullName());
            localContext.setVariableValue("to-full",     to.getFullName());

            if(!checkAccess(localContext, from, to))
                {
                List messages;
                if(getMessage() == null)
                    messages = Collections.EMPTY_LIST;
                else
                    messages = Collections.singletonList(
                        VariableParser.parse(localContext, getMessage()));
                
                context.broadcastEvent(
                    new AccessRuleViolation(this, from, to, messages));
                }
            }
        }
    
    public boolean checkAccess(EvaluationContext context, ClassInfo fromClass, ClassInfo toClass)
        throws RulesException
        { return IncludeExcludeLogic.apply(makeIncludeExcludeNode(this, context, fromClass, toClass)); }
    
    private static IncludeExcludeNode makeIncludeExcludeNode(
            final AccessRule rule,
            final EvaluationContext context,
            final ClassInfo fromClass,
            final ClassInfo toClass)
        {
        return (rule == null)
            ? null
            : new IncludeExcludeNode()
                {
                public boolean isInclude()
                    { return rule.getType() == AccessRuleType.ALLOW; }
        
                public boolean matches()
                    throws RulesException
                    {
                    return rule.getFrom().matches(context, fromClass)
                        && rule.  getTo().matches(context, toClass);
                    }
                
                public IncludeExcludeNode getChild()
                    { return makeIncludeExcludeNode(rule.getChild(), context, fromClass, toClass); }
                
                public IncludeExcludeNode getNext()
                    { return makeIncludeExcludeNode(rule.getNext(), context, fromClass, toClass); }
                };
        }
    }



