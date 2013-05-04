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
 
package net.innig.macker.rule.filter;

import net.innig.util.*;
import java.util.*;

public abstract class FilterFinder
    {
    public static Filter findFilter(String filterName)
        throws NoSuchFilterException
        {
        Filter filter = (Filter) filterCache.get(filterName);
        if(filter == null)
            {
            String filterClassName = 
                Conf.getMergedProperties(FILTER_CONF, FilterFinder.class.getClassLoader())
                    .getProperty(filterName);
            if(filterClassName == null)
                throw new NoSuchFilterException(filterName);
            try {
                filterCache.put(
                    filterName,
                    filter = (Filter) Class.forName(filterClassName).newInstance());
                }
            catch(ClassNotFoundException cnfe)
                { throwFilterConfigException(filterClassName, filterName, cnfe); }
            catch(IllegalAccessException iae)
                { throwFilterConfigException(filterClassName, filterName, iae); }
            catch(InstantiationException ie)
                { throwFilterConfigException(filterClassName, filterName, ie); }
            catch(ClassCastException cce)
                { throwFilterConfigException(filterClassName, filterName, cce); }
            }
        return filter;
        }
    
    private static void throwFilterConfigException(String filterClassName, String filterName, Exception source)
        {
        throw new CorruptConfigurationException(
            FILTER_CONF,
            "Unable to use class " + filterClassName
             + " specified for filter \"" + filterName
             + "\": " + source);
        }
    
    private FilterFinder() { }
    
    private static final String FILTER_CONF = "net.innig.macker.filter";
    private static Map filterCache = new HashMap();
    }
