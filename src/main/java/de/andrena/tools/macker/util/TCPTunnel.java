/*____________________________________________________________________
 *
 * TCPTunnel.java
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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Forwards TCP connections from a local port to a remote machine, optionally
 * logging the passing traffic for observation. You can use TCPTunnel from your
 * Java code, or as a command line tool. Possible uses include:
 * <ul>
 * <li>debugging a conversation between a browser and a web server by examining
 * the headers;
 * <li>mirroring a telnet session to a file so that many people can watch a
 * delicate operation at the command line;
 * <li>logging the traffic for a particular service to scan for bugs or security
 * breaches;
 * <li>opening an easily-configurable hole between network boundaries.
 * </ul>
 * Here are a few examples to give you a feel for how it works:
 * <ul>
 * <li><b>A simple HTTP tunnel</b>:
 * <p>
 * <code>&nbsp;&nbsp;
                java org.mpr.util.TCPTunnel -m 31415 www.mpr.org 80
            </code>
 * <p>
 * This tells TCPTunnel to listen for connections on port 31415, and tunnel them
 * to www.mpr.org, port 80. (31415 is just an arbitrary high port number that's
 * probably not in use; 80 is the standard port for HTTP.) Point a browser at
 * <code>http://localhost:31415</code>, and you'll see MPR's lovely home page.
 * The <code>-m</code> option tells the tunnel to allow multiple simultaneous
 * connections, which is a good idea if you're not logging traffic. Try the same
 * command without <code>-m</code>, and see how much slower the images load.
 * <p>
 * <li><b>Watching a browser talk to a web server</b>:
 * <p>
 * <code>&nbsp;&nbsp;
                java org.mpr.util.TCPTunnel -v -o out 31415 www.mpr.org 80
            </code>
 * <p>
 * Point a browser <code>http://localhost:31415</code>, and watch a whole mess
 * o'HTTP scroll by! The <code>-o out</code> option tells TCPTunnel to log the
 * network traffic in both directions to the console; <code>-v</code> enables
 * verbose information on when connections open, how they're terminated, and
 * other such good stuff.
 * <p>
 * If that output is too much of a mess and you really only want to see the
 * browser's requests, not the responses, try:
 * <p>
 * <code>&nbsp;&nbsp;
                java org.mpr.util.TCPTunnel -v -o http 31415 www.mpr.org 80 &amp;
            <br>&nbsp;&nbsp;
                java org.mpr.util.StreamSplitter -f http_in.log out
            </code>
 * <p>
 * The first line logs the traffic to two files (the <code>-o http</code> option
 * gives the name for the files), and the second line dumps the input log to the
 * console as it grows. (The <code>&amp;</code> on the first line is the
 * UN*X-style of running the command in the background; substitute as
 * appropriate for your OS.)
 * <p>
 * Note that this trick won't always work perfectly -- as you'll see if you try
 * these examples, your browser thinks the web server's hostname is
 * "localhost:31415", and tells it so in the "Host" header. If the web server
 * uses this header to differentiate multiple sites with the same IP, it will
 * become confused.
 * <p>
 * <li><b>Mirroring a telnet session for your all buddies to watch</b>:
 * <p>
 * <code>&nbsp;&nbsp;
                java org.mpr.util.TCPTunnel -o telnet_mirror -l 2 31415 some-machine 23 &amp;
            <br>&nbsp;&nbsp;
                telnet localhost 31415
            </code>
 * <p>
 * Here we're using port 23 (telnet) instead of 80 (HTTP). The <code>-l 2</code>
 * option sets a very low read latency, which makes the tunnel more responsive
 * to your keystrokes. (Be careful if you do this one -- your login password
 * will end up in telnet_mirror_in.log in plaintext! You may want to delete that
 * file after logging in.) Now anybody who wants to watch what you're doing can
 * just follow along with the log:
 * <p>
 * <code>&nbsp;&nbsp;
                java org.mpr.util.StreamSplitter -l 1 -f telnet_mirror_out.log out
            </code>
 * <p>
 * That's good clean fun for the whole family!
 * </ul>
 * <p>
 * You could easily modify this class to filter traffic, do TCP broadcasts, and
 * other such insanity -- the source license allows modification, so go to it!
 * And, if you're a good soul, share your cool mods with the world.
 * 
 * <p align="center">
 * <table cellpadding=4 cellspacing=2 border=0 bgcolor="#338833" width="90%">
 * <tr>
 * <td bgcolor="#EEEEEE">
 * <b>Maturity:</b> This is mature code. It has worked very well in several
 * different real-world settings. I have seen a connection-dangling problem when
 * this class is used as an HTTP proxy; I'd like to track this down.</td>
 * </tr>
 * <tr>
 * <td bgcolor="#EEEEEE">
 * <b>Plans:</b> Though there are possibilities for interesting derived
 * utilities (mentioned above), there are no current plans to expand or revise
 * this class's functionality -- except to track down the connection dangling
 * problem mentioned above. High-volume performance testing and tuning might be
 * useful in the future.</td>
 * </tr>
 * </table>
 */

public class TCPTunnel extends Thread {
	/** Command line handling */

	static public void main(String[] args) throws IOException {
		int localPort, remotePort;
		String remoteHost;
		OutputStream inTraffic = null, outTraffic = null;
		boolean multi = false, verbose = false;
		int latency = -1, bufferSize = -1;

		try {
			int arg = 0;

			while (args[arg].charAt(0) == '-') {
				if (args[arg].startsWith("-v")) {
					arg++;
					System.err.println("Verbose output enabled");
					verbose = true;
				}

				if (args[arg].startsWith("-o")) {
					arg++;
					String logName = args[arg++];
					if (logName.equals("out")) {
						if (verbose)
							System.err.println("Outputting traffic to console");
						inTraffic = outTraffic = System.out;
					} else {
						if (verbose)
							System.err.println("Outputting traffic to " + logName + "_in.log and " + logName
									+ "_out.log");
						inTraffic = new FileOutputStream(logName + "_in.log");
						outTraffic = new FileOutputStream(logName + "_out.log");
					}
				}

				if (args[arg].startsWith("-m")) {
					arg++;
					if (verbose)
						System.err.println("Allowing multiple simultaneous connections");
					multi = true;
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
			}

			localPort = Integer.parseInt(args[arg++]);
			remoteHost = args[arg++];
			remotePort = Integer.parseInt(args[arg++]);
		} catch (Exception e) {
			System.err.println("usage: java org.mpr.util.TCPTunnel [options] local-port remote-host remote-port");
			System.err.println("  where options include:");
			System.err.println("    [-o(utput) {out|<file>}]  Log traffic to console or a file");
			System.err.println("    [-m(ulti)]                Allow multiple simultaneous connections");
			System.err.println("    [-v(erbose)]              Print connect/disconnect/error information");
			System.err.println("    [-l(atency) millis]       Read latency (set low for faster response; default=30)");
			System.err
					.println("    [-b(uffer) bytes]         Read buffer (set high for better throughput; default=32768)");
			return;
		}

		TCPTunnel tunnel = new TCPTunnel(localPort, remoteHost, remotePort, inTraffic, outTraffic);
		tunnel.setVerbose(verbose);
		tunnel.setMultiConnection(multi);
		if (latency > 0)
			tunnel.setLatency(latency);
		if (bufferSize > 0)
			tunnel.setBufferSize(bufferSize);
		tunnel.run();
	}

	/**
	 * Creates a new TCP tunnel from the local machine to a remote one.
	 * 
	 * @param localPort
	 *            The local port to forward connections from.
	 * @param remoteHost
	 *            The name or IP of the remote machine to forward connections
	 *            to.
	 * @param remotePort
	 *            The port on the remote machine to forward connections to.
	 */

	public TCPTunnel(int localPort, String remoteHost, int remotePort) throws IOException {
		this(localPort, remoteHost, remotePort, null, null);
	}

	/**
	 * Creates a new TCP tunnel from the local machine to a remote one,
	 * splitting a copy of the network traffic to local streams for observation.
	 * 
	 * @param localPort
	 *            The local port to forward connections from.
	 * @param remoteHost
	 *            The name or IP of the remote machine to forward connections
	 *            to.
	 * @param remotePort
	 *            The port on the remote machine to forward connections to.
	 * @param inTraffic
	 *            Logs the traffic from local to remote.
	 * @param outTraffic
	 *            Logs the traffic from remote to local.
	 */

	public TCPTunnel(int localPort, String remoteHost, int remotePort, OutputStream inTraffic, OutputStream outTraffic)
			throws IOException {
		this.localPort = localPort;
		this.remoteHost = remoteHost;
		this.remotePort = remotePort;
		this.inTraffic = inTraffic;
		this.outTraffic = outTraffic;
		listener = new ServerSocket(localPort);
		verbose = false;
		multi = true;
		latency = bufferSize = -1;
	}

	/** Turns debugging output on and off. False by default. */

	public boolean getVerbose() {
		return verbose;
	}

	/** Turns debugging output on and off. False by default. */

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	/** Allows or disallows multiple simultaneous connections. True by default. */

	public boolean getMultiConnection() {
		return multi;
	}

	/**
	 * Allows or disallows multiple simultaneous connections. True by default.
	 * If this property is false, the tunnel forces connections to queue up
	 * single file, which makes for cleaner logs.
	 */

	public void setMultiConnection(boolean multi) {
		this.multi = multi;
	}

	/**
	 * Returns the maximum number of bytes the tunneler will hold in each
	 * internal buffer. The default buffer size is 32k.
	 */

	public int getBufferSize() {
		return bufferSize;
	}

	/**
	 * Returns the maximum number of bytes the tunneler will hold in each
	 * internal buffer.
	 */

	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	/**
	 * Returns the time in milliseconds which this tunnel will sleep waiting for
	 * new input. The default latency is 30ms.
	 */

	public int getLatency() {
		return latency;
	}

	/**
	 * Sets the time in milliseconds which this tunnel will sleep waiting for
	 * new input. A longer latency may speed things up if a lot of data is
	 * moving through the pipe, but may slow things down if the data is arriving
	 * slowly or in bursts.
	 */

	public void setLatency(int latency) {
		this.latency = latency;
	}

	/**
	 * Begins forwarding connections. You can run this in the background by
	 * calling {@link java.lang.Thread#start()}. This method will continue
	 * indefinitely until one of the following happens:
	 * <ul>
	 * <li>An exception occurs binding to the local port, or opening a socket to
	 * the remote host/port. Note that this method will <i>not</i> terminate due
	 * to exceptions during traffic forwarding (such as the remote host closing
	 * the connection). The sorts of things that <i>will</i> terminate this
	 * method are the local port being in use, a DNS error on the remote host
	 * name, connection refused by remote, etc.
	 * <li>The thread is {@link java.lang.Thread#interrupt() interrupted}.
	 * </ul>
	 */

	@Override
	public void run() {
		try {
			while (true) {
				// Get connection

				if (verbose)
					System.err.println("TCPTunnel listening...");
				SocketTunnel tunnel = new SocketTunnel(listener.accept(), remoteHost, remotePort);
				if (multi)
					tunnel.start();
				else
					tunnel.run();
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
			System.err.println("TCPTunnel stopped");
		}
	}

	/**
	 * Tunnels traffic between two sockets, closing both when either closes or
	 * gives an error.
	 */

	private class SocketTunnel extends Thread {
		public SocketTunnel(Socket remoteSock, String bounceHost, int bouncePort) {
			this.remoteSock = remoteSock;
			this.bounceHost = bounceHost;
			this.bouncePort = bouncePort;
		}

		@Override
		public void run() {
			// Log connection

			PrintWriter outMsg = null, inMsg = null;
			if (verbose) {
				if (outTraffic != null)
					outMsg = new PrintWriter(new OutputStreamWriter(outTraffic));
				if (inTraffic != null)
					inMsg = new PrintWriter(new OutputStreamWriter(inTraffic));

				String msg = "Got connection on port " + localPort + " from " + remoteSock.getInetAddress().toString()
						+ " at " + new java.util.Date();

				System.err.println(msg);
				if (outMsg != null)
					outMsg.println(msg);
				if (inMsg != null)
					inMsg.println(msg);
			}

			StreamSplitter splitIn = null, splitOut = null;
			try {
				// Connect

				bounceSock = new Socket(bounceHost, bouncePort);

				// Set up splitters

				OutputStream[] inSplits = { inTraffic, bounceSock.getOutputStream() }, outSplits = { outTraffic,
						remoteSock.getOutputStream() };
				splitIn = new StreamSplitter(remoteSock.getInputStream(), inSplits);
				splitOut = new StreamSplitter(bounceSock.getInputStream(), outSplits);
				splitIn.setVerbose(verbose);
				splitOut.setVerbose(verbose);
				if (latency != -1) {
					splitIn.setLatency(latency);
					splitOut.setLatency(latency);
				}
				if (bufferSize != -1) {
					splitIn.setBufferSize(bufferSize);
					splitOut.setBufferSize(bufferSize);
				}

				// Chug

				splitIn.start();
				splitOut.start();
				while (!splitIn.isDone() && !splitOut.isDone())
					Thread.sleep(162);
			} catch (Exception e) {
				if (verbose)
					e.printStackTrace(System.err);
			}

			// Close sockets

			try {
				remoteSock.close();
			} catch (Exception e) {
				if (verbose)
					e.printStackTrace(System.err);
			}

			try {
				bounceSock.close();
			} catch (Exception e) {
				if (verbose)
					e.printStackTrace(System.err);
			}

			splitIn.interrupt();
			splitOut.interrupt();

			// Log closure

			if (verbose) {
				String msg = "Connection from " + remoteSock.getInetAddress().toString() + " closed at "
						+ new java.util.Date() + "  In bytes: " + (splitIn != null ? splitIn.getByteCount() : 0)
						+ "  Out bytes: " + (splitOut != null ? splitOut.getByteCount() : 0);

				System.err.println(msg);
				if (outMsg != null)
					outMsg.println(msg);
				if (inMsg != null)
					inMsg.println(msg);
			}
		}

		private Socket remoteSock, bounceSock;
		private String bounceHost;
		private int bouncePort;
	}

	private int localPort, remotePort;
	private String remoteHost;
	private ServerSocket listener;
	private OutputStream inTraffic, outTraffic;
	private boolean verbose, multi;
	private int latency, bufferSize;
}
