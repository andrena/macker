package de.andrena.tools.macker.rule;

import java.io.IOException;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class MackerEntityResolver implements EntityResolver {

	private static final String MACKER_PUBLIC_ID = "-//innig//DTD Macker 0.4//EN";

	public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
		if (MACKER_PUBLIC_ID.equals(publicId)) {
			return new InputSource(RuleSetBuilder.MACKER_DTD.openStream());
		}
		return null;
	}
}
