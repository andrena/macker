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
 
package com.github.benromberg.macker.rule;

import org.jdom.Element;

import org.apache.regexp.RESyntaxException;

public class MackerRegexSyntaxException
    extends RulesException
    {
    public MackerRegexSyntaxException(String regexp)
        {
        super(getMessage(regexp) + "");
        this.regexp = regexp;
        }
    
    public MackerRegexSyntaxException(String regexp, RESyntaxException cause)
        {
        super(getMessage(regexp) + ": ", cause);
        this.regexp = regexp;
        }
    
    public MackerRegexSyntaxException(String regexp, String message)
        {
        super(getMessage(regexp) + ": " + message);
        this.regexp = regexp;
        }
    
    public final String getRegexp()
        { return regexp; }
        
    private static String getMessage(String regexp)
        { return "\"" + regexp + "\" is not a valid Macker regexp pattern"; }
    
    private final String regexp;
    }
    
    
    