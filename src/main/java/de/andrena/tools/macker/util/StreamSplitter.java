/*____________________________________________________________________
 *
 * StreamSplitter.java
 *
 * Copyright 1999 Minnesota Public Radio
 * ____________________________________________________________________
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * (1) Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 * (2) Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in
 *     the documentation and/or other materials provided with the
 *     distribution. 
 *
 * (3) The name of the author may not be used to endorse or promote
 *     products derived from this software without specific prior
 *     written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *____________________________________________________________________*/

package de.andrena.tools.macker.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Continuously funnels the data from an InputStream to a group of
 * OutputStreams.
 * <p>
 * StreamSplitter is a thread, so you can call {@link #run()} to block until the
 * InputStream is exhausted, or {@link Thread#start()} to have the splitter work
 * continuously in the background. You can control the size of the internal
 * buffer and read latency to fine tune for bulk throughput and smooth updating.
 * You'll get decent performance from this class with the right fiddling.
 * <p>
 * You can also use StreamSplitter from a command line, though you are limited
 * to files and standard in/out/err. The examples below show several useful
 * scenarios:
 * <p>
 * <ul>
 * <li><b>Copying a file</b> (<b><code>cp</code></b>):
 * 
 * <pre>
 * (new StreamSplitter(new FileInputStream(srcfile), new FileOutputStream(dstfile))).run();
 * </pre>
 * 
 * From the command line:
 * <code>java org.mpr.util.StreamSplitter srcfile dstfile</code><br>
 * For a large file (almost as fast as <code>cp</code>!):
 * <code>java org.mpr.util.StreamSplitter -b 500000 srcfile dstfile</code>
 * <p>
 * <li><b>Splitting input to a file and output</b> (<b><code>tee</code></b>):
 * 
 * <pre>
 * (new StreamSplitter(System.in, System.out, new FileOutputStream(file))).run();
 * </pre>
 * 
 * From the command line:
 * <code>java org.mpr.util.StreamSplitter in out file</code>
 * <p>
 * <li><b>Following a log file as it grows</b> (<b><code>tail -f</code></b>):
 * 
 * <pre>
 * StreamSplitter splitter = new StreamSplitter(new FileInputStream(file), System.out);
 * splitter.setHaltOnEOF(false);
 * splitter.run();
 * </pre>
 * 
 * From the command line:
 * <code>java org.mpr.util.StreamSplitter -f file out</code><br>
 * For much smoother output than <code>tail -f</code>, try:
 * <code>java org.mpr.util.StreamSplitter -f -l 2 file out</code>
 * <p>
 * <li>See {@link org.mpr.util.TCPTunnel TCPTunnel} for an interesting network
 * example.
 * </ul>
 * 
 * <p align="center">
 * <table cellpadding=4 cellspacing=2 border=0 bgcolor="#338833" width="90%">
 * <tr>
 * <td bgcolor="#EEEEEE">
 * <b>Maturity:</b> This is mature code. It has worked nicely in real-world
 * settings.</td>
 * </tr>
 * <tr>
 * <td bgcolor="#EEEEEE">
 * <b>Plans:</b> There are no current plans to expand or revise this class's
 * functionality. High-volume performance testing and tuning might be useful in
 * the future.</td>
 * </tr>
 * </table>
 * 
 * @author Paul Cantrell
 * @version May 10 2001
 */

public class StreamSplitter extends Thread {
	/** Command line handling */

	static public void main(String[] args) throws IOException {
		int latency = -1, bufferSize = -1;
		boolean verbose = false, haltOnEOF = true;

		String inName;
		InputStream in;
		List outs;
		int arg = 0;

		try {
			while (args[arg].charAt(0) == '-') {
				if (args[arg].startsWith("-v")) {
					arg++;
					System.err.println("Verbose output enabled");
					verbose = true;
				}

				if (args[arg].startsWith("-l")) {
					arg++;
					latency = Integer.parseInt(args[arg++]);
					if (verbose)
						System.err.println("Latency = " + latency + "ms");
				}

				if (args[arg].startsWith("-b")) {
					arg++;
					bufferSize = Integer.parseInt(args[arg++]);
					if (verbose)
						System.err.println("Buffer size = " + bufferSize + " bytes");
				}

				if (args[arg].startsWith("-f")) {
					arg++;
					if (verbose)
						System.err.println("Will not halt on EOF");
					haltOnEOF = false;
				}
			}
			inName = args[arg++];
		} catch (Exception e) {
			System.err
					.println("usage: java org.mpr.util.StreamSplitter [-v(erbose)] [-f(ollow)] [-l(atency) millis] [-b(uffer) bytes] input output*...");
			System.err.println("   where input = in|<filename>");
			System.err.println("        output = out|err|<filename>");
			return;
		}

		in = inName.equals("in") ? System.in : new FileInputStream(inName);
		outs = new LinkedList();
		while (arg < args.length) {
			String outName = args[arg++];
			if (outName.equals("out"))
				outs.add(System.out);
			else if (outName.equals("err"))
				outs.add(System.err);
			else
				outs.add(new FileOutputStream(outName));
		}

		StreamSplitter splitter = new StreamSplitter(in, outs);
		splitter.setVerbose(verbose);
		splitter.setHaltOnEOF(haltOnEOF);
		if (latency > 0)
			splitter.setLatency(latency);
		if (bufferSize > 0)
			splitter.setBufferSize(bufferSize);
		splitter.run();
		if (verbose)
			System.err.println(splitter.getByteCount() + " bytes transferred");
	}

	/**
	 * Creates a new splitter from an input stream to an arbitrary number of
	 * output streams. The <code>out</code> parameter may be empty.
	 */

	public StreamSplitter(InputStream in, OutputStream[] out) {
		this.in = in;
		this.out = new OutputStream[out.length];
		System.arraycopy(out, 0, this.out, 0, out.length);
		byteCount = 0;
		bufferSize = 32768;
		latency = 30;
		running = true;
		done = false;
		haltOnEOF = true;
		verbose = true;
	}

	/**
	 * Creates a new splitter from an input stream to an arbitrary number of
	 * output streams. The <code>out</code> parameter may be empty.
	 */

	public StreamSplitter(InputStream in, Collection outs) {
		this(in, (OutputStream[]) outs.toArray(new OutputStream[outs.size()]));
	}

	/**
	 * Creates a new splitter from an input stream to a single output stream.
	 * The <code>out</code> parameter may be null.
	 */

	public StreamSplitter(InputStream in, OutputStream out) {
		this(in, new OutputStream[] { out });
	}

	/**
	 * Creates a new splitter from an input stream to two output streams. Both
	 * <code>out1</code> and <code>out2</code> may be null.
	 */

	public StreamSplitter(InputStream in, OutputStream out1, OutputStream out2) {
		this(in, new OutputStream[] { out1, out2 });
	}

	/**
	 * Reads bytes from the input stream and writes them to the output streams
	 * until one of the following happens:
	 * <ul>
	 * <li>The input stream terminates and the {@link #setHaltOnEOF(boolean)
	 * haltOnEOF} property is true.
	 * <li>An exception occurs (i.e. socket closed).
	 * <li>The thread is {@link Thread#interrupt() interrupted}.
	 * </ul>
	 * If splitting terminates due to an exception, and if
	 * {@link #setVerbose(boolean) verbose} property is true, then the exception
	 * goes to System.err; otherwise, this method fails quietly.
	 */

	@Override
	public void run() {
		done = false;
		byte[] buf = new byte[bufferSize];

		try {
			while (running) {
				int size = Math.max(1, Math.min(in.available(), buf.length));
				size = in.read(buf, 0, size);
				if (size == -1) {
					if (haltOnEOF)
						break;
				} else {
					for (int n = 0; n < out.length; n++)
						if (out[n] != null) {
							out[n].write(buf, 0, size);
							out[n].flush();
						}
					byteCount += size;
				}
				if (size < buf.length)
					sleep(latency);
			}
		} catch (Exception e) {
			if (verbose) e.printStackTrace(System.err);
		}

		try {
			in.close();
		} catch (Exception e) {
			if (verbose) e.printStackTrace(System.err);
		}
		for (int n = 0; n < out.length; n++) {
			try {
				out[n].close();
			} catch (IOException e) {
				if (verbose) e.printStackTrace(System.err);
			}
		}

		done = true;
	}

	/**
	 * Asks this splitter to halt at its earliest convenience. If the splitter
	 * is blocked waiting for input, it will continue to block; if you need to
	 * stop it immediately, use {@link Thread#interrupt()}. To check whether the
	 * splitter has halted, use {@link #isDone()}.
	 */

	public void halt() {
		running = false;
	}

	/** Returns true if the splitter has run and has halted. */

	public boolean isDone() {
		return done;
	}

	/**
	 * Determines whether the splitter will halt when it reads EOF, or will wait
	 * for more input. This property is true by default.
	 */

	public boolean getHaltOnEOF() {
		return haltOnEOF;
	}

	/**
	 * Determines whether the splitter will halt when it reads EOF, or will wait
	 * for more input. True by default.
	 */

	public void setHaltOnEOF(boolean haltOnEOF) {
		this.haltOnEOF = haltOnEOF;
	}

	/**
	 * Turns debugging output on and off, including exception stack traces. True
	 * by default.
	 */

	public boolean getVerbose() {
		return verbose;
	}

	/**
	 * Turns debugging output on and off, including exception stack traces. True
	 * by default.
	 */

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	/**
	 * Returns the maximum number of bytes the splitter will read or write at
	 * once. The default buffer size is 32k.
	 */

	public int getBufferSize() {
		return bufferSize;
	}

	/**
	 * Sets the maximum number of bytes the splitter will read or write at once.
	 * This only affects future calls to run(); if the splitter is already
	 * running, it will continue to use its current buffer size.
	 */

	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	/**
	 * Returns the time in milliseconds which this splitter will sleep waiting
	 * for new input. The default latency is 30ms.
	 */

	public int getLatency() {
		return latency;
	}

	/**
	 * Sets the time in milliseconds which this splitter will sleep waiting for
	 * new input. A longer latency may speed things up if a lot of data is
	 * moving through the pipe, but may slow things down if the data is arriving
	 * slowly or in bursts.
	 */

	public void setLatency(int latency) {
		this.latency = latency;
	}

	/** Returns the number of bytes this splitter has read from its input. */

	public long getByteCount() {
		return byteCount;
	}

	// --------------------------------
	// Private
	// --------------------------------

	private InputStream in;
	private OutputStream[] out;
	private long byteCount;
	private int bufferSize, latency;
	private boolean done, haltOnEOF, running, verbose;
}
