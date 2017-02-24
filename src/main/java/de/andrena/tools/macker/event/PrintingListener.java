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

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.andrena.tools.macker.rule.RuleSet;
import de.andrena.tools.macker.rule.RuleSeverity;
import de.andrena.tools.macker.util.collect.CompositeMultiMap;
import de.andrena.tools.macker.util.collect.MultiMap;

public class PrintingListener implements MackerEventListener {
	public PrintingListener(PrintWriter out) {
		this.out = out;
	}

	public PrintingListener(Writer out) {
		this.out = new PrintWriter(out, true);
	}

	public PrintingListener(OutputStream out) {
		this.out = new PrintWriter(out, true);
	}

	public void setThreshold(RuleSeverity threshold) {
		this.threshold = threshold;
	}

	public void setMaxMessages(int maxMessages) {
		this.maxMessages = maxMessages;
	}

	public void mackerStarted(RuleSet ruleSet) {
		if (ruleSet.getParent() == null || ruleSet.hasName()) {
			out.println();
			out.println("(Checking ruleset: " + ruleSet.getName() + " ...)");
			first = true;
		}
	}

	public void mackerFinished(RuleSet ruleSet) throws MackerIsMadException {
	}

	public void mackerAborted(RuleSet ruleSet) {
	} // don't care

	public void handleMackerEvent(RuleSet ruleSet, MackerEvent event) throws MackerIsMadException {
		if (event instanceof ForEachEvent) {
			if (event instanceof ForEachIterationStarted) {
				ForEachIterationStarted iterStart = (ForEachIterationStarted) event;
				out.print('(');
				out.print(iterStart.getForEach().getVariableName());
				out.print(": ");
				out.print(iterStart.getVariableValue());
				out.println(")");
			}
			// ignore other ForEachEvents
		} else {
			eventsBySeverity.put(event.getRule().getSeverity(), event);
			if (event.getRule().getSeverity().compareTo(threshold) >= 0) {
				if (messagesPrinted < maxMessages) {
					if (first) {
						out.println();
						first = false;
					}
					out.println(event.toStringVerbose());
				}
				if (messagesPrinted == maxMessages)
					out.println("WARNING: Exceeded the limit of " + maxMessages + " message"
							+ (maxMessages == 1 ? "" : "s") + "; further messages surpressed");
				messagesPrinted++;
			}

		}
	}

	public void printSummary() {
		// output looks like: "(2 errors, 1 warning)"
		boolean firstSeverity = true;
		List<RuleSeverity> severities = new ArrayList<RuleSeverity>(eventsBySeverity.keySet());
		Collections.reverse(severities);
		for (RuleSeverity severity : severities) {
			Collection<MackerEvent> eventsForSev = eventsBySeverity.get(severity);
			if (eventsForSev.size() > 0) {
				if (firstSeverity)
					out.print("(");
				else
					out.print(", ");
				firstSeverity = false;
				out.print(eventsForSev.size());
				out.print(' ');
				out.print((eventsForSev.size() == 1) ? severity.getName() : severity.getNamePlural());
			}
		}
		if (firstSeverity) {
			// no problems found -> log that to make clear the run is finished
			out.println("(no issues)");
		} else {
			out.println(')');
		}
	}

	@Override
	public String toString() {
		return "PrintingListener";
	}

	private boolean first;
	private PrintWriter out;
	private int maxMessages = Integer.MAX_VALUE, messagesPrinted = 0;
	private RuleSeverity threshold = RuleSeverity.INFO;
	private final MultiMap<RuleSeverity, MackerEvent> eventsBySeverity = new CompositeMultiMap<RuleSeverity, MackerEvent>(
			new EnumMap<RuleSeverity, Set<MackerEvent>>(RuleSeverity.class), HashSet.class);
}
