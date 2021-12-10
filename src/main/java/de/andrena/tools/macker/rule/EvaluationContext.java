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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.andrena.tools.macker.event.ListenerException;
import de.andrena.tools.macker.event.MackerEvent;
import de.andrena.tools.macker.event.MackerEventListener;
import de.andrena.tools.macker.event.MackerIsMadException;
import de.andrena.tools.macker.structure.ClassManager;

public class EvaluationContext {
	public EvaluationContext(ClassManager classManager, RuleSet ruleSet) {
		this.classManager = classManager;
		this.ruleSet = ruleSet;
		varValues = new HashMap<String, String>();
		listeners = new HashSet<MackerEventListener>();
	}

	public EvaluationContext(RuleSet ruleSet, EvaluationContext parent) {
		this(parent.getClassManager(), ruleSet);
		this.parent = parent;
	}

	public EvaluationContext(EvaluationContext parent) {
		this(parent.getRuleSet(), parent);
	}

	public EvaluationContext getParent() {
		return parent;
	}

	public ClassManager getClassManager() {
		return classManager;
	}

	public RuleSet getRuleSet() {
		return ruleSet;
	}
	
	/**
	 * Set variable value with substitution of variables in the value string.
	 * @param name
	 * @param value
	 * @throws UndeclaredVariableException
	 */
	public void setVariableValueWithSubstitution(String name, String value) throws UndeclaredVariableException {
		varValues.put(name, (value==null) ? "" : VariableParser.parse(this, value));
	}
	
	/**
	 * Set variable value without substitution of variables in the value string.
	 * @param name
	 * @param value
	 * @throws UndeclaredVariableException
	 */
	public void setVariableValue(String name, String value) throws UndeclaredVariableException {
		varValues.put(name, (value==null) ? "" : value);
	}

	public String getVariableValue(String name) throws UndeclaredVariableException {
		String value = varValues.get(name);
		if (value != null)
			return value;
		if (parent != null)
			return parent.getVariableValue(name);
		throw new UndeclaredVariableException(name);
	}

	public void setVariables(Map<String, String> vars) {
		varValues.putAll(vars);
	}

	public void addListener(MackerEventListener listener) {
		listeners.add(listener);
	}

	public void removeListener(MackerEventListener listener) {
		listeners.remove(listener);
	}

	public void broadcastStarted() throws ListenerException {
		broadcastStarted(getRuleSet());
	}

	protected void broadcastStarted(RuleSet targetRuleSet) throws ListenerException {
		for (MackerEventListener listener : listeners)
			listener.mackerStarted(targetRuleSet);
		if (getParent() != null)
			getParent().broadcastStarted(targetRuleSet);
	}

	public void broadcastFinished() throws MackerIsMadException, ListenerException {
		broadcastFinished(getRuleSet());
	}

	protected void broadcastFinished(RuleSet targetRuleSet) throws MackerIsMadException, ListenerException {
		for (MackerEventListener listener : listeners)
			listener.mackerFinished(targetRuleSet);
		if (getParent() != null)
			getParent().broadcastFinished(targetRuleSet);
	}

	public void broadcastAborted() {
		broadcastAborted(getRuleSet());
	}

	protected void broadcastAborted(RuleSet targetRuleSet) {
		for (MackerEventListener listener : listeners)
			listener.mackerAborted(targetRuleSet);
		if (getParent() != null)
			getParent().broadcastAborted(targetRuleSet);
	}

	public void broadcastEvent(MackerEvent event) throws MackerIsMadException, ListenerException {
		broadcastEvent(event, getRuleSet());
	}

	protected void broadcastEvent(MackerEvent event, RuleSet targetRuleSet) throws MackerIsMadException,
			ListenerException {
		for (MackerEventListener listener : listeners)
			listener.handleMackerEvent(targetRuleSet, event);
		if (getParent() != null)
			getParent().broadcastEvent(event, targetRuleSet);
	}

	private RuleSet ruleSet;
	private EvaluationContext parent;
	private Map<String, String> varValues;
	private Set<MackerEventListener> listeners;
	private ClassManager classManager;
}
