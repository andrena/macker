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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import de.andrena.tools.macker.event.ListenerException;
import de.andrena.tools.macker.event.MackerIsMadException;
import de.andrena.tools.macker.structure.ClassInfo;
import de.andrena.tools.macker.structure.ClassManager;

public class RuleSet extends Rule {
	public static RuleSet getMackerDefaults() {
		if (defaults == null)
			try {
				defaults = new RuleSet();
				defaults.setPattern("from", new RegexPattern("${from-full}"));
				defaults.setPattern("to", new RegexPattern("${to-full}"));
			} catch (MackerRegexSyntaxException mrse) {
				throw new RuntimeException("Macker built-ins are broken", mrse);
			} // ! what else to throw?
		return defaults;
	}

	private static RuleSet defaults;

	public RuleSet(RuleSet parent) {
		super(parent);
		if (parent == null)
			throw new IllegalArgumentException("parent == null");

		patterns = new HashMap<String, Pattern>();
		rules = new ArrayList<Rule>();
	}

	private RuleSet() {
		super(null);
		rules = Collections.emptyList();
		patterns = new HashMap<String, Pattern>();
	}

	public String getName() {
		if (name == null)
			return (getParent() != null) ? getParent().getName() : "<anonymous ruleset>";
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean hasName() {
		return name != null;
	}

	public boolean declaresPattern(String name) {
		return patterns.keySet().contains(name);
	}

	public Pattern getPattern(String name) {
		Pattern pat = patterns.get(name);
		if (pat != null)
			return pat;
		if (getParent() != null)
			return getParent().getPattern(name);
		return null;
	}

	public void setPattern(String name, Pattern pattern) {
		if (name == null)
			throw new NullPointerException("name cannot be null");
		if (pattern == null)
			throw new NullPointerException("pattern cannot be null");
		patterns.put(name, pattern);
	}

	public Collection<Pattern> getAllPatterns() {
		return patterns.values();
	}

	public void clearPattern(String name) {
		patterns.remove(name);
	}

	public Collection getRules() {
		return rules;
	}

	public void addRule(Rule rule) {
		rules.add(rule);
	}

	public Pattern getSubsetPattern() {
		return subsetPat;
	}

	public void setSubsetPattern(Pattern subsetPat) {
		this.subsetPat = subsetPat;
	}

	public boolean isInSubset(EvaluationContext context, ClassInfo classInfo) throws RulesException {
		if (subsetPat != null && !subsetPat.matches(context, classInfo))
			return false;
		if (getParent() != null)
			return getParent().isInSubset(context, classInfo);
		return true;
	}

	public void check(EvaluationContext parentContext, ClassManager classes) throws RulesException,
			MackerIsMadException, ListenerException {
		EvaluationContext context = new EvaluationContext(this, parentContext);
		context.broadcastStarted();
		boolean finished = false;
		try {
			for (Rule rule : rules)
				rule.check(context, classes);
			context.broadcastFinished();
			finished = true;
		} finally {
			if (!finished)
				context.broadcastAborted();
		}
	}

	@Override
	public String toString() {
		return getClass().getName() + '[' + name + ", parent=" + getParent() + ']';
	}

	private String name;
	private Map<String, Pattern> patterns;
	private Collection<Rule> rules;
	private Pattern subsetPat;
}
