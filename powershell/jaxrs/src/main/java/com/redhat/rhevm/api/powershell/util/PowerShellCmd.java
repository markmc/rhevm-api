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

import expectj.ExpectJ;
import expectj.Spawn;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.redhat.rhevm.api.common.security.auth.Principal;

public class PowerShellCmd {
    private static final Log log = LogFactory.getLog(PowerShellCmd.class);

    private static final String QUOTE = "'";

    private Principal principal;
    private ExpectJ expectj;

    // REVISIT: if the stdout/stderr buffers go over a
    //          certain size we should re-create it

    private Spawn process;
    private OutputBuffer stdout = new OutputBuffer();
    private OutputBuffer stderr = new OutputBuffer();

    public PowerShellCmd(Principal principal, ExpectJ expectj) {
        this.principal = principal;
        this.expectj = expectj;
    }

    public PowerShellCmd(Principal principal) {
        this(principal, new ExpectJ());
    }

    public PowerShellCmd() {
        this(null);
    }

    public int run(String script) {
        log.info("Running '" + script + "'");

        script = addConvertToXml(script);

        try {
            process.send(script.toString());
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

        stdout.update(process.getCurrentStandardOutContents());
        stderr.update(process.getCurrentStandardErrContents());

        return stdout.getStatus();
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

        if (principal != null && !principal.equals(Principal.NONE)) {
            try {
                process.send(buildLogin());
            } catch (java.io.IOException ex) {
                throw new PowerShellException("Writing login-user to powershell process failed", ex);
            }
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
        // REVIST: I can't get the commented out bits to work
        StringBuilder buf = new StringBuilder();
        buf.append("Write-Host \"<output>\";\n");
        //buf.append("try {\n");
        buf.append("  $result = Invoke-Expression " + escape(command) + ";\n");
        buf.append("  $result | ConvertTo-XML -As String -Depth 5;\n");
        buf.append("  $status = 0;\n");
        //buf.append("} catch {\n");
        //buf.append("  ConvertTo-XML $_ -As String -Depth 1;\n");
        //buf.append("  $status = 1;\n");
        //buf.append("}\n");
        buf.append("Write-Host \"</output> $status\";\n");
        return buf.toString();
    }

    private class OutputBuffer {
        private String buf;
        private int index;
        private int status;

        public void update(String contents) {
            buf = contents.substring(index);
            index = contents.length();

            if (buf.contains("<output>")) {
                buf = buf.substring(buf.indexOf("<output>") + 8);
            }
            if (buf.contains("</output>")) {
                int i = buf.indexOf("</output>");
                status = Integer.parseInt(buf.substring(i + 10, i + 11));
                buf = buf.substring(0, i);
            }
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

    private static void handleExitStatus(int exitstatus, String command) {
        if (exitstatus != 0) {
            throw new PowerShellException("Command '" + command + "' exited with status=" + exitstatus);
        }
    }

    public static String runCommand(PowerShellPool pool, String command) {
        PowerShellCmd runner = pool.get();

        try {
            int exitstatus = runner.run(command);

            if (!runner.getStdErr().isEmpty()) {
                log.warn(runner.getStdErr());
            }

            handleExitStatus(exitstatus, command);

            return runner.getStdOut();
        } finally {
            pool.add(runner);
        }
    }
}
