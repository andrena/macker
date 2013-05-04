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
 
package net.innig.macker.util;

import net.innig.macker.rule.RulesException;

public class IncludeExcludeLogic
    {
    public static boolean apply(IncludeExcludeNode node)
        throws RulesException
        {
        return applyNext(
            node,
            node.isInclude()
                ? false  // include starts with all excluded, and
                : true); // exclude starts with all included
        }

    private static boolean applyNext(
            IncludeExcludeNode node,
            boolean prevMatches)
        throws RulesException
        {
        IncludeExcludeNode child = node.getChild(), next = node.getNext();
        boolean curMatches = node.matches();
        boolean matchesSoFar =
            node.isInclude()
                ? prevMatches || ( curMatches && (child == null || apply(child)))
                : prevMatches && (!curMatches || (child != null && apply(child)));
        return
            (next == null)
                ? matchesSoFar
                : applyNext(next, matchesSoFar);
        }
    }