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
 
package com.github.benromberg.macker.rule.filter;

import java.util.Map;
import java.util.List;

import com.github.benromberg.macker.rule.*;
import com.github.benromberg.macker.structure.ClassInfo;

public class InterfaceFilter
    implements Filter
    {
    public Pattern createPattern(
            RuleSet ruleSet,
            List/*<Pattern>*/ params,
            Map/*<String,String>*/ options)
        throws RulesException
        {
        if(params.size() != 0)
            throw new FilterSyntaxException(
                this,
                "Filter \"" + options.get("filter") + "\" expects no parameters, but has " + params.size());
        return INTERFACE_PATTERN;
        }

    private final Pattern INTERFACE_PATTERN =
        new Pattern()
            {
            public boolean matches(EvaluationContext context, ClassInfo classInfo)
                throws RulesException
                { return classInfo.isInterface(); }
            };
    }
