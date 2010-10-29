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

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import expectj.ExpectJ;
import expectj.Spawn;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.redhat.rhevm.api.common.invocation.Current;
import com.redhat.rhevm.api.common.security.auth.Challenger;
import com.redhat.rhevm.api.common.security.auth.Principal;

public class PowerShellCmd {
    private static final Log log = LogFactory.getLog(PowerShellCmd.class);

    private static final String LOGIN_FAILED = "Login failed. Please verify your login information";
    private static final String TIMED_OUT = "Service is down, or timeout exceeded.";
    private static final String QUOTE = "'";

    private Principal principal;
    private Current current;
    private ExpectJ expectj;

    // REVISIT: if the stdout/stderr buffers go over a
    //          certain size we should re-create it

    private Spawn process;
    private OutputBuffer stdout = new OutputBuffer(true);
    private OutputBuffer stderr = new OutputBuffer();

    public PowerShellCmd(Principal principal, ExpectJ expectj, Current current) {
        this.principal = principal;
        this.expectj = expectj;
        this.current = current;
    }

    public PowerShellCmd(Principal principal, Current current) {
        this(principal, new ExpectJ(), current);
    }

    public PowerShellCmd() {
        this(null, null);
    }

    public int run(String script) {
        log.info("Running '" + script + "'");

        script = addConvertToXml(script);

        String command = isTimedOut(process)
                         ? buildLogin() + script.toString()
                         : script.toString();

        try {
            process.send(command);
        } catch (java.io.IOException ex) {
            throw new PowerShellException("Writing to powershell process failed", ex);
        }

        try {
            process.expect("</output>");
        } catch (expectj.TimeoutException te) {
            // should never happen
        } catch (java.io.IOException ex) {
            throw new PowerShellException("Reading from powershell process failed", ex);
        }

        /* REVISIT: this ugly hack is needed because ExpectJ seems to sometimes
         *          return before the </output> marker has been written.
         */
        while (!stdout.update(process.getCurrentStandardOutContents())) {}
        stderr.update(process.getCurrentStandardErrContents());

        return stdout.getStatus();
    }

    private static boolean isTimedOut(Spawn process) {
        boolean timeout = false;
        String out = process.getCurrentStandardOutContents();
        if (out.length() > TIMED_OUT.length()) {
            int tail = out.length() - TIMED_OUT.length() - 2;
            timeout = out.substring(tail)
                         .replaceAll("\\r", "")
                         .replaceAll("\\n", "")
                         .endsWith(TIMED_OUT);
        }
        return timeout;
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

    private String buildLogin() {
        StringBuilder loginUser = new StringBuilder();

        loginUser.append("logout-user; ");
        loginUser.append("login-user");
        if (principal.getDomain() != null) {
            loginUser.append(" -domain "   + PowerShellUtils.escape(principal.getDomain()));
        }
        if (principal.getUser() != null) {
            loginUser.append(" -username " + PowerShellUtils.escape(principal.getUser()));
        }
        if (principal.getSecret() != null) {
            loginUser.append(" -password " + PowerShellUtils.escape(principal.getSecret()));
        }
        loginUser.append(";\n");

        return loginUser.toString();
    }

    public void start() {
        String command = findPowerShell() + " -command -";

        try {
            process = expectj.spawn(command);
        } catch (java.io.IOException ex) {
            throw new PowerShellException("Launching powershell failed", ex);
        }

        try {
            process.send(buildLogin());
        } catch (java.io.IOException ex) {
            throw new PowerShellException("Writing login-user to powershell process failed", ex);
        }
    }

    public void stop() {
        if (process != null) {
            process.stop();
            process = null;
        }
    }

    private String escape(String arg) {
        arg = arg.replace(QUOTE, QUOTE + QUOTE);
        return new StringBuffer(QUOTE).append(arg).append(QUOTE).toString();
    }

    private String addConvertToXml(String command) {
        StringBuilder buf = new StringBuilder();
        buf.append("write-host \"<output>\"; ");
        buf.append("try { ");
        buf.append("$result = invoke-expression " + escape(command) + "; ");
        buf.append("$result | convertto-xml -as String -depth 5; ");
        buf.append("$status = 0 ");
        buf.append("} catch { ");
        buf.append("convertto-xml -as String -depth 1 $_; ");
        buf.append("$status = 1 ");
        buf.append("} ");
        buf.append("write-host \"</output> $status\"\n");
        return buf.toString();
    }

    private class OutputBuffer {
        private String buf;
        private int index;
        private int status;
        private boolean requireOutputMarker;

        public OutputBuffer(boolean requireOutputMarker) {
            this.requireOutputMarker = requireOutputMarker;
        }

        public OutputBuffer() {
            this(false);
        }

        public boolean update(String contents) {
            String tmp = contents.substring(index);

            if (requireOutputMarker && !tmp.contains("</output>")) {
                return false;
            }

            buf = tmp;
            index = contents.length();

            if (buf.contains("<output>")) {
                buf = buf.substring(buf.indexOf("<output>") + 8);
            }
            if (buf.contains("</output>")) {
                int i = buf.indexOf("</output>");
                status = Integer.parseInt(buf.substring(i + 10, i + 11));
                buf = buf.substring(0, i);
            }

            return true;
        }

        public String get() {
            return buf;
        }

        public int getStatus() {
            return status;
        }
    }

    public String getStdOut() {
        return stdout.get().trim();
    }

    public String getStdErr() {
        return stderr.get().trim();
    }

    public void complete(int exitstatus, String command) {
        if (!getStdErr().isEmpty()) {
            log.warn(getStdErr());
            handleAuth(this);
        }

        handleExitStatus(exitstatus, command);
    }

    private static void handleExitStatus(int exitstatus, String command) {
        if (exitstatus != 0) {
            throw new PowerShellException("Command '" + command + "' exited with status=" + exitstatus);
        }
    }

    private static void handleAuth(PowerShellCmd runner) {
        if (runner.getStdErr().contains(LOGIN_FAILED)) {
            runner.process.stop();
            // allow pool to drain for this Principal, starvation
            // will not occur even if the user persists in using the
            // same (currently invalid) credentials, as the
            // low-water-mark logic will ensure further shells are
            // created on demand
            if (runner.current != null) {
                Challenger challenger = runner.current.get(Challenger.class);
                if (challenger != null) {
                    throw new WebApplicationException(challenger.getChallenge());
                }
            }
            throw new WebApplicationException(Status.UNAUTHORIZED);
        }
    }

    public static String runCommand(PowerShellPool pool, String command) {
        PowerShellCmd runner = pool.get();

        try {
            runner.complete(runner.run(command), command);
            return runner.getStdOut();
        } finally {
            pool.add(runner);
        }
    }
}
