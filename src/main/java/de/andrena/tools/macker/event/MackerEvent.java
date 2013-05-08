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
 
package de.andrena.tools.macker.event;


import java.util.*;

import de.andrena.tools.macker.rule.Rule;
import de.andrena.tools.macker.rule.RuleSeverity;

public class MackerEvent
    extends EventObject
    {
    public MackerEvent(
            Rule rule,
            String description,
            List/*<String>*/ messages)
        {
        super(rule);
        this.rule = rule;
        this.description = description;
        this.messages = Collections.unmodifiableList(new ArrayList(messages));
        }
    
    public Rule getRule()
        { return rule; }
        
    public String getDescription()
        { return description; }
        
    public List/*<String>*/ getMessages()
        { return messages; }
    
    public String toString()
        { return getDescription(); }
    
    public String toStringVerbose()
        {
        //! This is completely crappy -- the PrintingListener probably should be the one to deal with this
        final String CR = System.getProperty("line.separator");
        StringBuffer s = new StringBuffer();
        if(rule.getSeverity() != RuleSeverity.ERROR)
            {
            s.append(rule.getSeverity().getName().toUpperCase());
            s.append(": ");
            }
        for(Iterator i = messages.iterator(); i.hasNext(); )
            {
            s.append(i.next().toString());
            s.append(CR);
            }
        if(getDescription() != null)
            s.append(getDescription());
        s.append(CR);
        return s.toString();
        }
    
    private final Rule rule;
    private final String description;
    private final List/*<String>*/ messages;
    }

