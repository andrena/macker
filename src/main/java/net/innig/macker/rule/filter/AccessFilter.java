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
 
package net.innig.macker.rule.filter;

import net.innig.macker.rule.*;
import net.innig.macker.structure.ClassInfo;
import net.innig.macker.structure.AccessModifier;
import java.util.Map;
import java.util.List;

public class AccessFilter
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

        String
            maxS = (String) options.get("max"),
            minS = (String) options.get("min");
        final AccessModifier
            max = (maxS != null) ? AccessModifier.fromName(maxS) : AccessModifier.PUBLIC,
            min = (minS != null) ? AccessModifier.fromName(minS) : AccessModifier.PRIVATE;
            
        if(maxS == null && minS == null)
            throw new FilterSyntaxException(
                this, options.get("filter") + " requires a \"max\" or \"min\" option (or both)");
        if(max == null && maxS != null)
            throw new FilterSyntaxException(
                this, 
                '"' + maxS + "\" is not a valid access level; expected one of: "
                + AccessModifier.allTypesSorted(AccessModifier.class));
        if(min == null && minS != null)
            throw new FilterSyntaxException(
                this, 
                '"' + minS + "\" is not a valid access level; expected one of: "
                + AccessModifier.allTypesSorted(AccessModifier.class));
        
        return new Pattern()
            {
            public boolean matches(EvaluationContext context, ClassInfo classInfo)
                throws RulesException
                {
                return classInfo.getAccessModifier().greaterThanEq(min)
                    && classInfo.getAccessModifier().   lessThanEq(max);
                }
            };
        }
    }
