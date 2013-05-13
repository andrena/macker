/*______________________________________________________________________________
 *
 * Macker   http://innig.net/macker/
 *
 * Copyright 2003 Paul Cantrell
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

package de.andrena.tools.macker.ant;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import de.andrena.tools.macker.util.StreamSplitter;

/**
 * A task which formats Macker reports using XSLT. Requires Xalan 2 or some
 * other well-behaved XSLT implementation.
 * 
 * @see <a href="http://ant.apache.org/manual/">The Ant manual</a>
 */
public class MackerReportAntTask extends Task {
	public MackerReportAntTask() {
		tFactory = TransformerFactory.newInstance();
	}

	public void setFormat(String formatName) throws BuildException {
		formatUrl = resolveInternalResource(formatName, "format", "xsl");
	}

	public void setFormatFile(File formatFile) throws BuildException {
		formatUrl = resolveFile(formatFile, "format");
	}

	public void setFormatUrl(String formatUrlS) throws BuildException {
		formatUrl = resolveUrl(formatUrlS, "format");
	}

	public void setSkin(String skinName) throws BuildException {
		skinUrl = resolveInternalResource(skinName, "skin", "css");
	}

	public void setSkinFile(File skinFile) throws BuildException {
		skinUrl = resolveFile(skinFile, "skin");
	}

	public void setSkinUrl(String skinUrlS) throws BuildException {
		skinUrl = resolveUrl(skinUrlS, "skin");
	}

	public void setXmlReportFile(File xmlReportFile) throws BuildException {
		reportUrl = resolveFile(xmlReportFile, "report");
	}

	public void setXmlReportUrl(String xmlReportUrlS) throws BuildException {
		reportUrl = resolveUrl(xmlReportUrlS, "report");
	}

	public void setOutputFile(File outputFile) {
		this.outputFile = outputFile;
	}

	private URL resolveFile(File file, String kind) throws BuildException {
		if (!file.exists())
			throw new BuildException(kind + " file " + file + " does not exist");
		if (!file.isFile())
			throw new BuildException(kind + " file " + file + " is not a file");

		try {
			return file.toURL();
		} catch (MalformedURLException murle) {
			throw new BuildException("Invalid " + kind + " file " + file, murle);
		}
	}

	private URL resolveUrl(String urlS, String kind) throws BuildException {
		try {
			return new URL(urlS);
		} catch (MalformedURLException murle) {
			throw new BuildException("Invalid " + kind + " URL " + urlS, murle);
		}
	}

	private URL resolveInternalResource(String name, String kind, String extension) throws BuildException {
		String resourceName = "net/innig/macker/report/" + kind + '/' + name + '.' + extension;
		URL resource = getClass().getClassLoader().getResource(resourceName);
		if (resource == null)
			throw new BuildException("No internal Macker report " + kind + " named \"" + name + "\" (can't find \""
					+ resourceName + "\")");
		return resource;
	}

	@Override
	public void execute() throws BuildException {
		if (reportUrl == null)
			throw new BuildException("xmlReportFile or xmlReportUrl required");
		if (outputFile == null)
			throw new BuildException("outputFile required");

		if (formatUrl == null)
			setFormat("html-basic");
		if (skinUrl == null)
			setSkin("vanilla");

		File outputDir = outputFile.getParentFile();

		try {
			Transformer transformer = tFactory.newTransformer(new StreamSource(formatUrl.openStream()));
			transformer.transform(new StreamSource(reportUrl.openStream()), new StreamResult(new FileOutputStream(
					outputFile)));
		} catch (IOException ioe) {
			throw new BuildException("Unable to process report: " + ioe, ioe);
		} catch (TransformerException te) {
			throw new BuildException("Unable to apply report formatting: " + te.getMessage(), te);
		}

		File skinOutputFile = new File(outputDir, "macker-report.css");
		try {
			new StreamSplitter(skinUrl.openStream(), new FileOutputStream(skinOutputFile)).run();
		} catch (IOException ioe) {
			throw new BuildException("Unable to copy skin to " + skinOutputFile, ioe);
		}
	}

	private URL formatUrl, skinUrl;
	private URL reportUrl;
	private File outputFile;
	private TransformerFactory tFactory;
}
