package de.andrena.tools.macker.util.io;

import java.io.Closeable;
import java.io.IOException;

/**
 * Stream helper class.
 * @author tneumann
 */
public class StreamUtil {

	public static final void closeStream(Closeable inputStream) {
		if (inputStream != null) {
			try {
				inputStream.close();
			} catch (IOException ioe) {
				// nothing we can do
			}
		}
	}
}
