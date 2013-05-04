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
 
package net.innig.macker.util;

import java.util.*;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

public class ClassNameTranslator
    {
    static public boolean isJavaIdentifier(String className)
        { return legalJavaIdentRE.match(className); }
    
    static public List signatureToClassNames(String signature)
        {
        List names = new ArrayList();
        for(int pos = 0; pos < signature.length(); )
            {
            String remaining = signature.substring(pos);
            if(!sigExtractorRE.match(remaining))
                throw new IllegalArgumentException("Unable to extract type info from: " + remaining);
            if(sigExtractorRE.getParen(2) != null)
                names.add((String) primitiveTypeMap.get(sigExtractorRE.getParen(2)));
            if(sigExtractorRE.getParen(3) != null)
                names.add(resourceToClassName(sigExtractorRE.getParen(3)));
            pos += sigExtractorRE.getParenEnd(0);
            }
        return names;
        }
    
    static public String typeConstantToClassName(String typeName)
        {
        if(arrayExtractorRE.match(typeName))
            {
            if(arrayExtractorRE.getParen(2) != null)
                return (String) primitiveTypeMap.get(arrayExtractorRE.getParen(2));
            if(arrayExtractorRE.getParen(3) != null)
                return resourceToClassName(arrayExtractorRE.getParen(3));
            }
        return resourceToClassName(typeName);
        }
    
    static public String resourceToClassName(String className)
        { return slashRE.subst(classSuffixRE.subst(className, ""), ".").intern(); }
    
    static public String classToResourceName(String resourceName)
        { return (dotRE.subst(resourceName, "/") + ".class").intern(); }
    
    static private RE classSuffixRE, slashRE, dotRE, arrayExtractorRE, sigExtractorRE, legalJavaIdentRE;
    static private Map/*<String,String>*/ primitiveTypeMap;
    static
        {
        try {
            classSuffixRE = new RE("\\.class$");
            slashRE = new RE("/");
            dotRE = new RE("\\.");
            arrayExtractorRE = new RE(        "^(\\[+([BSIJCFDZV])|\\[+L([^;]*);)$");
            sigExtractorRE   = new RE("^\\(?\\)?(\\[*([BSIJCFDZV])|\\[*L([^;]*);)");
            legalJavaIdentRE = new RE("^([:javastart:][:javapart:]*)(\\.([:javastart:][:javapart:]*))*$");
            }
        catch(RESyntaxException rese)
            { throw new RuntimeException("Can't initialize ClassNameTranslator: " + rese); } 
        
        primitiveTypeMap = new HashMap();
        primitiveTypeMap.put("B", "byte");
        primitiveTypeMap.put("S", "short");
        primitiveTypeMap.put("I", "int");
        primitiveTypeMap.put("J", "long");
        primitiveTypeMap.put("C", "char");
        primitiveTypeMap.put("F", "float");
        primitiveTypeMap.put("D", "double");
        primitiveTypeMap.put("Z", "boolean");
        primitiveTypeMap.put("V", "void");
        }
    }
