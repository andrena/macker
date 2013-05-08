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

import org.jdom.Element;
import org.jdom.JDOMException;

/**
    Indicates a structural exception in rules specification XML.
*/

public class RulesDocumentException
    extends RulesException
    {
    public RulesDocumentException(Exception e)
        {
        super("Error in rules document XML", e);
        element = null;
        }
    
    public RulesDocumentException(Element element, String message)
        {
        super("Error in rules document XML: " + message + " (Offending element: " + element + ')');
        this.element = element;
        }
    
    public final Element getElement()
        { return element; }
    
    private final Element element;
    }
