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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public final class MackerRegex {
	// --------------------------------------------------------------------------
	// Constructors
	// --------------------------------------------------------------------------

	public MackerRegex(String regexStr) throws MackerRegexSyntaxException {
		this(regexStr, true);
	}

	public MackerRegex(String regexStr, boolean allowParts) throws MackerRegexSyntaxException {
		if (regexStr == null)
			throw new NullPointerException("regexStr == null");

		this.regexStr = regexStr;
		parts = null;
		regex = null;
		prevVarValues = new HashMap<String, String>();

		if (!(allowParts ? allowable : allowableNoParts).matcher(regexStr).matches())
			throw new MackerRegexSyntaxException(regexStr);
	}

	// --------------------------------------------------------------------------
	// Properties
	// --------------------------------------------------------------------------

	public String getPatternString() {
		return regexStr;
	}

	private final String regexStr;

	// --------------------------------------------------------------------------
	// Evaluation
	// --------------------------------------------------------------------------

	public boolean matches(EvaluationContext context, String s) throws UndeclaredVariableException,
			MackerRegexSyntaxException {
		return getMatch(context, s) != null;
	}

	public String getMatch(EvaluationContext context, String s) throws UndeclaredVariableException,
			MackerRegexSyntaxException {
		parseExpr(context);
		Boolean match = matchCache.get(s);
		if (match != null)
			return match ? matchResultCache.get(s) : null;

		Matcher matcher = regex.matcher('.' + s);
		match = matcher.matches();
		matchCache.put(s, match);
		if (match) {
			String matchResult = matcher.group(matcher.groupCount());
			matchResultCache.put(s, matchResult);
			return matchResult;
		} else
			return null;
	}

	private void parseExpr(EvaluationContext context) throws UndeclaredVariableException, MackerRegexSyntaxException {
		if (parts == null) {
			parts = new ArrayList<Part>();
			Matcher varMatcher = var.matcher(regexStr);
			for (int pos = 0; pos >= 0;) {
				boolean hasAnotherVar = varMatcher.find(pos);
				int expEnd = hasAnotherVar ? varMatcher.start() : regexStr.length();

				if (pos < expEnd)
					parts.add(new ExpPart(parseSubexpr(regexStr.substring(pos, expEnd))));
				if (hasAnotherVar)
					parts.add(new VarPart(varMatcher.group(1)));

				pos = hasAnotherVar ? varMatcher.end() : -1;
			}
		}

		// Building the regexp is expensive; there's no point in doing it if we
		// already have one cached, and the relevant variables haven't changed

		boolean changed = (regex == null);
		for (Map.Entry<String, String> entry : prevVarValues.entrySet()) {
			String name = entry.getKey();
			String value = entry.getValue();
			if (!context.getVariableValue(name).equals(value)) {
				changed = true;
				break;
			}
		}

		if (changed) {
			StringBuffer builtRegexStr = new StringBuffer("^\\.?");
			for (Part part : parts) {
				if (part instanceof VarPart) {
					String varName = ((VarPart) part).varName;
					String varValue = context.getVariableValue(varName);
					prevVarValues.put(varName, varValue);
					builtRegexStr.append(parseSubexpr(varValue));
				} else if (part instanceof ExpPart)
					builtRegexStr.append(((ExpPart) part).exp);
			}
			builtRegexStr.append('$');

			try {
				regex = Pattern.compile(builtRegexStr.toString());
			} catch (PatternSyntaxException pse) {
				System.out.println("builtRegexStr = " + builtRegexStr);
				throw new MackerRegexSyntaxException(regexStr, pse);
			}

			// ! if(???)
			// ! throw new MackerRegexSyntaxException(regexStr,
			// "Too many parenthesized expressions");
			matchCache = new HashMap<String, Boolean>();
			matchResultCache = new HashMap<String, String>();
		}
	}

	private String parseSubexpr(String exp) {
		return exp.replace(".", "[\\.\\$]").replace("/", "\\.").replace("$", "\\$").replace("*", "\uFFFF")
				.replace("\uFFFF\uFFFF", ".*").replace("\uFFFF", "[^\\.]*");
	}

	private Pattern regex;
	private List<Part> parts;
	private Map<String, String> prevVarValues;
	private Map<String, Boolean> matchCache;
	private Map<String, String> matchResultCache;
	static private Pattern var, allowable, allowableNoParts;
	static {
		String varS = "\\$\\{([A-Za-z0-9_\\.\\-]+)\\}";
		String partS = "(([A-Za-z_]|[\\(\\)]|\\*|" + varS + ")" + "([A-Za-z0-9_]|[\\(\\)]|\\*|" + varS + ")*)";
		var = Pattern.compile(varS);
		allowable = Pattern.compile("^([\\$\\./]?" + partS + ")+$");
		allowableNoParts = Pattern.compile("^" + partS + "$");
	}

	private class Part {
	}

	private class VarPart extends Part {
		public VarPart(String varName) {
			this.varName = varName;
		}

		public String varName;

		@Override
		public String toString() {
			return "var(" + varName + ")";
		}
	}

	private class ExpPart extends Part {
		public ExpPart(String exp) {
			this.exp = exp;
		}

		public String exp;

		@Override
		public String toString() {
			return "exp(" + exp + ")";
		}
	}

	// --------------------------------------------------------------------------
	// Object
	// --------------------------------------------------------------------------

	@Override
	public String toString() {
		return '"' + regexStr + '"';
	}
}
