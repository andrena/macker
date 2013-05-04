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
 
package net.innig.macker.structure;

import net.innig.util.GraphType;

/**
    Taxonomy of different kinds of references between classes.
    
    @see Reference
*/
public class ReferenceType
    extends GraphType
    {
    public static final ReferenceType
        SIGNATURE                  = new ReferenceType("signature"),
            MEMBER_SIGNATURE       = new ReferenceType("member-signature", SIGNATURE),
                METHOD_SIGNATURE   = new ReferenceType("method-signature", MEMBER_SIGNATURE),
                    METHOD_PARAM   = new ReferenceType("method-param", METHOD_SIGNATURE),
                    METHOD_RETURNS = new ReferenceType("method-returns", METHOD_SIGNATURE),
                    METHOD_THROWS  = new ReferenceType("method-throws", METHOD_SIGNATURE),
                FIELD_SIGNATURE    = new ReferenceType("field-signature", MEMBER_SIGNATURE),
            SUPER                  = new ReferenceType("super"),
                EXTENDS            = new ReferenceType("extends", SUPER),
                IMPLEMENTS         = new ReferenceType("implements", SUPER),
        INTERNAL                   = new ReferenceType("internal"),
            CONSTANT_POOL          = new ReferenceType("constant-pool", INTERNAL);
    
    private ReferenceType(String name) { super(name); }
    private ReferenceType(String name, ReferenceType parent) { super(name, parent); }
    }
