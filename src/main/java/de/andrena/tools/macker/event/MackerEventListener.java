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

import de.andrena.tools.macker.rule.RuleSet;

public interface MackerEventListener
    extends EventListener
    {
    /** Called before rule checking begins for the given ruleset.
     */
    public void mackerStarted(RuleSet ruleSet)
        throws ListenerException;
    
    /** Called after rule checking has finished for the given ruleset.
     */
    public void mackerFinished(RuleSet ruleSet)
        throws MackerIsMadException, ListenerException;
    
    /** Called after an exception has aborted rule checking for the given ruleset.
     *  <b>??</b>: Is mackerAborted called if mackerFinished() was already called, but
     *	another listener subsequently aborted?
     */
    public void mackerAborted(RuleSet ruleSet);
    
    /** Handles Macker's irrational anger.
     */
    public void handleMackerEvent(RuleSet ruleSet, MackerEvent event)
        throws MackerIsMadException, ListenerException;
    }
