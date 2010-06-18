/*
 * Copyright © 2010 Red Hat, Inc.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.redhat.rhevm.api.powershell.util;

import java.io.File;
import java.io.InputStream;
import java.util.Scanner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PowerShellCmd {
    private static final Log log = LogFactory.getLog(PowerShellCmd.class);

    private String script;

    public PowerShellCmd(String script) {
        this.script = script;
    }

    private String stdout, stderr;

    public int runAndWait(Process p) {
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

        while (true) {
            try {
                p.waitFor();
                stdoutThread.join();
                stderrThread.join();
                break;
            } catch	(java.lang.InterruptedException ex) {
                /* with waitFor(), this is needed to clear
                 * the interrupted status flag:
                 *   http://bugs.sun.com/view_bug.do?bug_id=6420270
                 */
                Thread.interrupted();
            }
        }

        stdout = stdoutThread.getOutput();
        stderr = stderrThread.getOutput();

        return p.exitValue();
    }

    /* On Windows 2008 R2, which is always 64-bit, the PowerShell bindings
     * are only installed in the  32-bit "Windows on Windows" environment.
     */
    private static final String SYSWOW64_PATH = "C:\\Windows\\SysWOW64\\WindowsPowerShell\\v1.0\\powershell.exe";

    private String findPowerShell() {
        if (new File(SYSWOW64_PATH).exists()) {
            return SYSWOW64_PATH;
        } else {
            return "powershell.exe";
        }
    }

    public int run() {
        log.debug("Running '" + script + "'");

        ProcessBuilder pb = new ProcessBuilder(findPowerShell(), "-command", "-");

        Process p;

        try {
            p = pb.start();
        } catch	(java.io.IOException ex) {
            throw new PowerShellException("Launching powershell failed", ex);
        }

        try {
            return runAndWait(p);
        } finally {
            p.destroy();
        }
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

        private StringBuilder outputBuffer = new StringBuilder();
        public String getOutput() {
            return outputBuffer.toString();
        }

        @Override
        public void run() {
            try {
                Scanner sc = new Scanner(is);
                while (sc.hasNext()) {
                    outputBuffer.append(sc.nextLine() + "\n");
                }
            } finally {
                try {
                    this.is.close();
                } catch (java.io.IOException ex) {
                    log.error("Exception while closing input stream", ex);
                }
            }
        }
    }

    public static String runCommand(String command) {
        PowerShellCmd cmd = new PowerShellCmd(command);

        int exitstatus = cmd.run();

        if (!cmd.getStdErr().isEmpty()) {
            log.warn(cmd.getStdErr());
        }

        if (exitstatus != 0) {
            throw new PowerShellException("Command '" + command + "' exited with status=" + exitstatus);
        }

        return cmd.getStdOut();
    }
}
