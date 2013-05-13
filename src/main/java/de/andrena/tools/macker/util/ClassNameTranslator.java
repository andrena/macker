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

package de.andrena.tools.macker.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClassNameTranslator {
	static public boolean isJavaIdentifier(String className) {
		return legalJavaIdentRE.matcher(className).matches();
	}

	static public List<String> signatureToClassNames(String signature) {
		List<String> names = new ArrayList<String>();
		for (int pos = 0; pos < signature.length();) {
			String remaining = signature.substring(pos);
			Matcher sigMatcher = sigExtractorRE.matcher(remaining);
			if (!sigMatcher.find())
				throw new IllegalArgumentException("Unable to extract type info from: " + remaining);
			if (sigMatcher.group(2) != null)
				names.add(primitiveTypeMap.get(sigMatcher.group(2)));
			if (sigMatcher.group(3) != null)
				names.add(resourceToClassName(sigMatcher.group(3)));
			pos += sigMatcher.end();
		}
		return names;
	}

	static public String typeConstantToClassName(String typeName) {
		Matcher arrayMatcher = arrayExtractorRE.matcher(typeName);
		if (arrayMatcher.matches()) {
			if (arrayMatcher.group(2) != null)
				return primitiveTypeMap.get(arrayMatcher.group(2));
			if (arrayMatcher.group(3) != null)
				return resourceToClassName(arrayMatcher.group(3));
		}
		return resourceToClassName(typeName);
	}

	static public String resourceToClassName(String className) {
		return classSuffixRE.matcher(className).replaceAll("").replace('/', '.').intern();
	}

	static public String classToResourceName(String resourceName) {
		return (resourceName.replace('.', '/') + ".class").intern();
	}

	static private Pattern classSuffixRE, arrayExtractorRE, sigExtractorRE, legalJavaIdentRE;
	static private Map<String, String> primitiveTypeMap;
	static {
		classSuffixRE = Pattern.compile("\\.class$");
		arrayExtractorRE = Pattern.compile("^(\\[+([BSIJCFDZV])|\\[+L([^;]*);)$");
		sigExtractorRE = Pattern.compile("^\\(?\\)?(\\[*([BSIJCFDZV])|\\[*L([^;]*);)");
		String javaIdent = "[\\p{Alpha}$_][\\p{Alnum}$_]*";
		legalJavaIdentRE = Pattern.compile("^(" + javaIdent + ")(\\.(" + javaIdent + "))*$");

		primitiveTypeMap = new HashMap<String, String>();
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
