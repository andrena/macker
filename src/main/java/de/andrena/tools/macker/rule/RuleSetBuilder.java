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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaders;

import de.andrena.tools.macker.rule.filter.Filter;
import de.andrena.tools.macker.rule.filter.FilterFinder;

public class RuleSetBuilder {

	private final SAXBuilder saxBuilder;

	public RuleSetBuilder() {
		saxBuilder = new SAXBuilder(XMLReaders.XSDVALIDATING);
	}

	public Collection<RuleSet> build(final InputStream is)
			throws RulesException {
		try {
			return build(saxBuilder.build(is));
		} catch (final JDOMException jdome) {
			throw new RulesDocumentException(jdome);
		} catch (final IOException ioe) {
			throw new RulesDocumentException(ioe);
		}
	}

	public Collection<RuleSet> build(final Reader reader) throws RulesException {
		try {
			return build(saxBuilder.build(reader));
		} catch (final JDOMException jdome) {
			throw new RulesDocumentException(jdome);
		} catch (final IOException ioe) {
			throw new RulesDocumentException(ioe);
		}
	}

	public Collection<RuleSet> build(final File file) throws RulesException {
		try {
			return build(saxBuilder.build(file));
		} catch (final JDOMException jdome) {
			throw new RulesDocumentException(jdome);
		} catch (final IOException ioe) {
			throw new RulesDocumentException(ioe);
		}
	}

	public Collection<RuleSet> build(final String fileName)
			throws RulesException {
		return build(new File(fileName));
	}

	public Collection<RuleSet> build(final Document doc) throws RulesException {
		return build(doc.getRootElement());
	}

	public Collection<RuleSet> build(final Element elem) throws RulesException {
		final Collection<RuleSet> ruleSets = new ArrayList<RuleSet>();

		for (final Element rsElem : getChildren(elem, "ruleset")) {
			ruleSets.add(buildRuleSet(rsElem, RuleSet.getMackerDefaults()));
		}
		return ruleSets;
	}

	private Collection<Element> getChildren(final Element elem,
			final String tagName) {
		return elem.getChildren(tagName);
	}

	public RuleSet buildRuleSet(final Element ruleSetElem, final RuleSet parent)
			throws RulesException {
		final RuleSet ruleSet = new RuleSet(parent);

		final String name = ruleSetElem.getAttributeValue("name");
		if (name != null) {
			ruleSet.setName(name);
		}

		buildSeverity(ruleSet, ruleSetElem);

		for (final Element subElem : getChildren(ruleSetElem)) {
			final String subElemName = subElem.getName();
			if (subElemName.equals("pattern")) {
				final String patternName = subElem.getAttributeValue("name");
				if (ruleSet.declaresPattern(patternName)) {
					throw new RulesDocumentException(subElem,
							"Pattern named \"" + patternName
									+ "\" is already defined in this context");
				}

				ruleSet.setPattern(patternName, buildPattern(subElem, ruleSet));
			} else if (subElemName.equals("subset")) {
				if (ruleSet.getSubsetPattern() != null) {
					throw new RulesDocumentException(subElem,
							"<ruleset> may only contain a single <subset> element");
				}
				ruleSet.setSubsetPattern(buildPattern(subElem, ruleSet));
			} else if (subElemName.equals("access-rule")) {
				ruleSet.addRule(buildAccessRule(subElem, ruleSet));
			} else if (subElemName.equals("var")) {
				ruleSet.addRule(buildVariable(subElem, ruleSet));
			} else if (subElemName.equals("foreach")) {
				ruleSet.addRule(buildForEach(subElem, ruleSet));
			} else if (subElemName.equals("ruleset")) {
				ruleSet.addRule(buildRuleSet(subElem, ruleSet));
			} else if (subElemName.equals("message")) {
				ruleSet.addRule(buildMessage(subElem, ruleSet));
			}
		}

		return ruleSet;
	}

	public Pattern buildPattern(final Element patternElem, final RuleSet ruleSet)
			throws RulesException {
		return buildPattern(patternElem, ruleSet, true, null);
	}

	public Pattern buildPattern(final Element patternElem,
			final RuleSet ruleSet, final boolean isTopElem,
			final Pattern nextPat) throws RulesException {
		// handle options

		final String otherPatName = patternElem.getAttributeValue("pattern");
		final String className = getClassNameAttributeValue(patternElem);
		final String filterName = patternElem.getAttributeValue("filter");

		CompositePatternType patType;
		if (patternElem.getName().equals("include")) {
			patType = CompositePatternType.INCLUDE;
		} else if (patternElem.getName().equals("exclude")) {
			patType = filterName == null ? CompositePatternType.EXCLUDE
					: CompositePatternType.INCLUDE;
		} else if (isTopElem) {
			patType = CompositePatternType.INCLUDE;
		} else {
			throw new RulesDocumentException(patternElem, "Invalid element <"
					+ patternElem.getName() + "> --"
					+ " expected <include> or <exclude>");
		}

		if (otherPatName != null && className != null) {
			throw new RulesDocumentException(patternElem,
					"patterns cannot have both a \"pattern\" and a \"class\" attribute");
		}

		// do the head thing

		Pattern head = null;
		if (className != null) {
			head = new RegexPattern(className);
		} else if (otherPatName != null) {
			head = ruleSet.getPattern(otherPatName);
			if (head == null) {
				throw new UndeclaredPatternException(otherPatName);
			}
		}

		// build up children

		Pattern childrenPat = null;
		final List<Element> children = new ArrayList<Element>(
				getChildren(patternElem)); // !
		// workaround
		// for
		// bug
		// in
		// JUnit
		// List children = patternElem.getChildren(); // this should work
		// instead when JUnit bug is fixed
		for (final ListIterator<Element> childIter = children
				.listIterator(children.size()); childIter.hasPrevious();) {
			final Element subElem = childIter.previous();
			if (subElem.getName().equals("message")) {
				continue;
			}

			childrenPat = buildPattern(subElem, ruleSet, false, childrenPat);
		}

		// wrap head in a filter if necessary

		if (filterName != null) {
			final Map<String, String> options = new HashMap<String, String>();
			for (final Attribute attr : getAttributes(patternElem)) {
				options.put(attr.getName(), attr.getValue());
			}
			options.remove("name");
			options.remove("pattern");
			options.remove("class");
			options.remove("regex");

			final Filter filter = FilterFinder.findFilter(filterName);
			head = filter.createPattern(
					ruleSet,
					head == null ? new ArrayList<Pattern>() : Collections
							.singletonList(head), options);

			if (patternElem.getName().equals("exclude")) {
				head = CompositePattern.create(CompositePatternType.EXCLUDE,
						head, null, null);
			}
		}

		// pull together composite

		return CompositePattern.create(patType, head, childrenPat, nextPat);
	}

	private Collection<Attribute> getAttributes(final Element patternElem) {
		return patternElem.getAttributes();
	}

	public Variable buildVariable(final Element forEachElem,
			final RuleSet parent) throws RulesException {
		final String varName = forEachElem.getAttributeValue("name");
		if (varName == null) {
			throw new RulesDocumentException(forEachElem,
					"<var> is missing the \"name\" attribute");
		}

		final String value = forEachElem.getAttributeValue("value");
		if (value == null) {
			throw new RulesDocumentException(forEachElem,
					"<var> is missing the \"value\" attribute");
		}

		return new Variable(parent, varName, value);
	}

	public Message buildMessage(final Element messageElem, final RuleSet parent)
			throws RulesException {
		final Message message = new Message(parent, messageElem.getText());
		buildSeverity(message, messageElem);
		return message;
	}

	public ForEach buildForEach(final Element forEachElem, final RuleSet parent)
			throws RulesException {
		final String varName = forEachElem.getAttributeValue("var");
		if (varName == null) {
			throw new RulesDocumentException(forEachElem,
					"<foreach> is missing the \"var\" attribute");
		}

		final String className = getClassNameAttributeValue(forEachElem);
		if (className == null) {
			throw new RulesDocumentException(forEachElem,
					"<foreach> is missing the \"class\" attribute");
		}

		final ForEach forEach = new ForEach(parent);
		forEach.setVariableName(varName);
		forEach.setRegex(className);
		forEach.setRuleSet(buildRuleSet(forEachElem, parent));
		return forEach;
	}

	public AccessRule buildAccessRule(final Element ruleElem,
			final RuleSet ruleSet) throws RulesException {
		AccessRule prevRule = null, topRule = null;
		for (final Element subElem : getChildren(ruleElem)) {
			final AccessRule accRule = new AccessRule(ruleSet);

			if (subElem.getName().equals("allow")) {
				accRule.setType(AccessRuleType.ALLOW);
			} else if (subElem.getName().equals("deny")) {
				accRule.setType(AccessRuleType.DENY);
			} else if (subElem.getName().equals("from")
					|| subElem.getName().equals("to")
					|| subElem.getName().equals("message")) {
				continue;
			} else {
				throw new RulesDocumentException(subElem, "Invalid element <"
						+ subElem.getName() + "> --"
						+ " expected an access rule (<deny> or <allow>)");
			}

			final Element fromElem = subElem.getChild("from");
			if (fromElem != null) {
				accRule.setFrom(buildPattern(fromElem, ruleSet));
			}

			final Element toElem = subElem.getChild("to");
			if (toElem != null) {
				accRule.setTo(buildPattern(toElem, ruleSet));
			}

			if (!subElem.getChildren().isEmpty()) {
				accRule.setChild(buildAccessRule(subElem, ruleSet));
			}

			if (topRule == null) {
				topRule = accRule;
			} else {
				prevRule.setNext(accRule);
			}
			prevRule = accRule;
		}
		if (topRule != null) {
			topRule.setMessage(ruleElem.getChildText("message"));
			buildSeverity(topRule, ruleElem);
		}
		return topRule;
	}

	private List<Element> getChildren(final Element ruleElem) {
		return ruleElem.getChildren();
	}

	public void buildSeverity(final Rule rule, final Element elem)
			throws RulesDocumentException {
		final String severityS = elem.getAttributeValue("severity");
		if (severityS != null && !"".equals(severityS)) {
			RuleSeverity severity;
			try {
				severity = RuleSeverity.fromName(severityS);
			} catch (final IllegalArgumentException iae) {
				throw new RulesDocumentException(elem, iae.getMessage());
			}
			rule.setSeverity(severity);
		}
	}

	private String getClassNameAttributeValue(final Element elem) {
		String value = elem.getAttributeValue("class");
		if (value == null) {
			value = elem.getAttributeValue("regex");
			if (value != null) {
				System.err
						.println("WARNING: The \"regex\" attribute is deprecated, and will be removed in v1.0.  Use \"class\" instead");
			}
		}
		return value;
	}

}
