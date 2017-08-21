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

import java.util.Collections;
import java.util.List;

import de.andrena.tools.macker.event.AccessRuleViolation;
import de.andrena.tools.macker.event.ListenerException;
import de.andrena.tools.macker.event.MackerIsMadException;
import de.andrena.tools.macker.structure.ClassInfo;
import de.andrena.tools.macker.structure.ClassManager;
import de.andrena.tools.macker.util.IncludeExcludeLogic;
import de.andrena.tools.macker.util.IncludeExcludeNode;
import de.andrena.tools.macker.util.collect.MultiMap;

/**
 * Processes <access-rule> tags in rule XML files.
 */
public class AccessRule extends Rule {
	// --------------------------------------------------------------------------
	// Constructors
	// --------------------------------------------------------------------------

	public AccessRule(RuleSet parent) {
		super(parent);
		type = AccessRuleType.DENY;
		from = to = Pattern.ALL;
	}

	// --------------------------------------------------------------------------
	// Properties
	// --------------------------------------------------------------------------

	public AccessRuleType getType() {
		return type;
	}

	public void setType(AccessRuleType type) {
		if (type == null)
			throw new NullPointerException("type parameter cannot be null");
		this.type = type;
	}

	public Pattern getFrom() {
		return from;
	}

	public void setFrom(Pattern from) {
		this.from = from;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Pattern getTo() {
		return to;
	}

	public void setTo(Pattern to) {
		this.to = to;
	}

	public AccessRule getChild() {
		return child;
	}

	public void setChild(AccessRule child) {
		this.child = child;
	}

	public AccessRule getNext() {
		return next;
	}

	public void setNext(AccessRule next) {
		this.next = next;
	}

	private AccessRuleType type;
	private Pattern from, to;
	private String message;
	private AccessRule child, next;

	// --------------------------------------------------------------------------
	// Evaluation
	// --------------------------------------------------------------------------

	@Override
	public void check(EvaluationContext context, ClassManager classes) throws RulesException, MackerIsMadException,
			ListenerException {
		EvaluationContext localContext = new EvaluationContext(context);
		for (MultiMap.Entry<ClassInfo, ClassInfo> reference : classes.getReferences().entrySet()) {
			ClassInfo from = reference.getKey();
			ClassInfo to = reference.getValue();
			if (from.equals(to))
				continue;
			if (!localContext.getRuleSet().isInSubset(localContext, from))
				continue;

			localContext.setVariableValue("from", from.getClassName());
			localContext.setVariableValue("to", to.getClassName());
			localContext.setVariableValue("from-package", from.getPackageName());
			localContext.setVariableValue("to-package", to.getPackageName());
			localContext.setVariableValue("from-full", from.getFullName());
			localContext.setVariableValue("to-full", to.getFullName());

			if (!checkAccess(localContext, from, to)) {
				List<String> messages;
				if (getMessage() == null)
					messages = Collections.emptyList();
				else
					messages = Collections.singletonList(VariableParser.parse(localContext, getMessage()));
				context.broadcastEvent(new AccessRuleViolation(this, from, to, messages));
			}
		}
	}

	private boolean checkAccess(EvaluationContext context, ClassInfo fromClass, ClassInfo toClass)
			throws RulesException {
		return IncludeExcludeLogic.apply(makeIncludeExcludeNode(this, context, fromClass, toClass));
	}

	static IncludeExcludeNode makeIncludeExcludeNode(final AccessRule rule, final EvaluationContext context,
			final ClassInfo fromClass, final ClassInfo toClass) {
		return (rule == null) ? null : new IncludeExcludeNode() {
			public boolean isInclude() {
				return rule.getType() == AccessRuleType.ALLOW;
			}

			public boolean matches() throws RulesException {
				return rule.getFrom().matches(context, fromClass) && rule.getTo().matches(context, toClass);
			}

			public IncludeExcludeNode getChild() {
				return makeIncludeExcludeNode(rule.getChild(), context, fromClass, toClass);
			}

			public IncludeExcludeNode getNext() {
				return makeIncludeExcludeNode(rule.getNext(), context, fromClass, toClass);
			}
		};
	}
}
