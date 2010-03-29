package com.redhat.rhevm.api.powershell;

import java.io.InputStream;
import java.util.Scanner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PowerShellCmd
{
	private final Log log = LogFactory.getLog(this.getClass());

	private String script;

	public PowerShellCmd(String script) {
		this.script = script;
	}

	private String stdout, stderr;

	public int run() {
		log.debug("Running '" + script + "'");

		ProcessBuilder pb = new ProcessBuilder("powershell", "-command", "-");

		Process p;

		try {
			p = pb.start();
		} catch	(java.io.IOException ex) {
			throw new PowerShellException("Launching powershell failed", ex);
		}

		try {
			p.getOutputStream().write(script.getBytes());
			p.getOutputStream().close();
		} catch (java.io.IOException ex) {
			throw new PowerShellException("Writing to powershell process failed", ex);
		}

		StreamReader stdoutThread = new StreamReader(p.getInputStream());
		StreamReader stderrThread = new StreamReader(p.getErrorStream());

		stdoutThread.start();
		stderrThread.start();

		int exitstatus;

		try {
			exitstatus = p.waitFor();
		} catch	(java.lang.InterruptedException ex) {
			// FIXME: error handling
			throw new PowerShellException("FIXME", ex);
		}

		try {
			stdoutThread.join();
			stderrThread.join();
		} catch (java.lang.InterruptedException ex) {
			// FIXME: error handling
			throw new PowerShellException("FIXME", ex);
		}

		stdout = stdoutThread.getOutput();
		stderr = stderrThread.getOutput();

		return exitstatus;
	}

	public String getStdOut() {
		return this.stdout;
	}

	public String getStdErr() {
		return this.stderr;
	}

	private class StreamReader extends Thread {
		private InputStream is;

		public StreamReader(InputStream is) {
			this.is = is;
		}

		private String output = new String();
		public String getOutput() {
			return output;
		}

		@Override
		public void run() {
			Scanner sc = new Scanner(is);
			while (sc.hasNext()) {
				output += sc.nextLine() + "\n";
			}
		}
	}
}