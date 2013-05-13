/*______________________________________________________________________________
 *
 * Macker   http://innig.net/macker/
 *
 * Copyright 2002 Paul Cantrell
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class VariableParser {
	public static String parse(EvaluationContext context, String inS) throws UndeclaredVariableException {
		StringBuffer outS = new StringBuffer();
		Matcher varMatcher = var.matcher(inS);
		for (int pos = 0; pos >= 0;) {
			boolean hasAnotherVar = varMatcher.find(pos);
			int expEnd = hasAnotherVar ? varMatcher.start() : inS.length();

			if (pos < expEnd)
				outS.append(inS.substring(pos, expEnd));
			if (hasAnotherVar)
				outS.append(context.getVariableValue(varMatcher.group(1)));

			pos = hasAnotherVar ? varMatcher.end() : -1;
		}
		return outS.toString();
	}

	static private Pattern var = Pattern.compile("\\$\\{([A-Za-z0-9_\\.\\-]+)\\}");

	private VariableParser() {
	}
}
