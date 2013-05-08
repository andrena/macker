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

import de.andrena.tools.macker.event.ListenerException;
import de.andrena.tools.macker.event.MackerIsMadException;
import de.andrena.tools.macker.event.MessageEvent;
import de.andrena.tools.macker.structure.ClassManager;

public class Message
    extends Rule
    {
    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    public Message(RuleSet parent, String message)
        {
        super(parent);
        this.message = message;
        setSeverity(RuleSeverity.INFO);
        }
    
    //--------------------------------------------------------------------------
    // Properties
    //--------------------------------------------------------------------------

    public String getMessage()
        { return message; }
    
    public void setMessage(String message)
        { this.message = message; }
    
    private String message;

    //--------------------------------------------------------------------------
    // Evaluation
    //--------------------------------------------------------------------------

    public void check(EvaluationContext context, ClassManager classes)
        throws RulesException, MackerIsMadException, ListenerException
        {
        context.broadcastEvent(
            new MessageEvent(
                this,
                VariableParser.parse(context, getMessage())));
        }
    }
