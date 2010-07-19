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
import java.io.OutputStream;
import java.util.Scanner;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.junit.runner.RunWith;

import static org.easymock.classextension.EasyMock.aryEq;
import static org.easymock.classextension.EasyMock.expect;
import static org.easymock.classextension.EasyMock.expectLastCall;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.expectNew;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

import com.redhat.rhevm.api.common.security.auth.Principal;

@RunWith(PowerMockRunner.class)
@PrepareForTest({PowerShellCmd.class})
public class PowerShellCmdTest extends Assert {

    private static final String SYSWOW64_PATH = "C:\\Windows\\SysWOW64\\WindowsPowerShell\\v1.0\\powershell.exe";
    private static final String SCRIPT = "hello";
    private static final String OUTPUT = "hello there!";
    private static final String ERROR = "Hello!\nHello!\nIs this thing on?\n";
    private static final int EXIT_STATUS = 1234;
    private static final String DOMAIN = "LeinsterHouse";
    private static final String USER = "Jackie";
    private static final String PASSWORD = "HealyRae";
    private static final String LOGIN_USER = "login-user -domain \"" + DOMAIN + "\" -username \"" + USER + "\" -password \"" + PASSWORD + "\";\n";
    private static final String LOGIN_USER_NO_DOMAIN = "login-user -username \"" + USER + "\" -password \"" + PASSWORD + "\";\n";

    @After
    public void tearDown() {
        verifyAll();
    }

    @Test
    public void testSimple() throws Exception {
        setupExpectations(null, SCRIPT, OUTPUT, ERROR, EXIT_STATUS, false);

        PowerShellCmd cmd = new PowerShellCmd();

        assertEquals(EXIT_STATUS, cmd.run(SCRIPT));
        assertEquals(OUTPUT + "\n", cmd.getStdOut());
        assertEquals(ERROR + "\n", cmd.getStdErr());
    }

    @Test
    public void testSysWow64() throws Exception {
        setupExpectations(null, SCRIPT, OUTPUT, ERROR, EXIT_STATUS, true);

        PowerShellCmd cmd = new PowerShellCmd();

        assertEquals(EXIT_STATUS, cmd.run(SCRIPT));
        assertEquals(OUTPUT + "\n", cmd.getStdOut());
        assertEquals(ERROR + "\n", cmd.getStdErr());
    }

    @Test
    public void testAuth() throws Exception {
        setupExpectations(LOGIN_USER, SCRIPT, OUTPUT, ERROR, EXIT_STATUS, false);

        PowerShellCmd cmd = new PowerShellCmd(new Principal(USER, PASSWORD, DOMAIN));

        assertEquals(EXIT_STATUS, cmd.run(SCRIPT));
        assertEquals(OUTPUT + "\n", cmd.getStdOut());
        assertEquals(ERROR + "\n", cmd.getStdErr());
    }

    @Test
    public void testAuthNoDomain() throws Exception {
        setupExpectations(LOGIN_USER_NO_DOMAIN, SCRIPT, OUTPUT, ERROR, EXIT_STATUS, false);

        PowerShellCmd cmd = new PowerShellCmd(new Principal(USER, PASSWORD));

        assertEquals(EXIT_STATUS, cmd.run(SCRIPT));
        assertEquals(OUTPUT + "\n", cmd.getStdOut());
        assertEquals(ERROR + "\n", cmd.getStdErr());
    }

    public void setupExpectations(String login, String input, String output, String error, int exitValue, boolean syswow64) throws Exception {
        File f = createMock(File.class);
        expectNew(File.class, SYSWOW64_PATH).andReturn(f);
        expect(f.exists()).andReturn(syswow64);

        Process p = createMock(Process.class);
        expect(p.waitFor()).andReturn(exitValue);
        expect(p.exitValue()).andReturn(exitValue);
        p.destroy();
        expectLastCall();

        ProcessBuilder pb = createMock(ProcessBuilder.class);
        expectNew(ProcessBuilder.class, syswow64 ? SYSWOW64_PATH : "powershell.exe", "-command", "-").andReturn(pb);
        expect(pb.start()).andReturn(p);

        OutputStream os = createMock(OutputStream.class);
        expect(p.getOutputStream()).andReturn(os).anyTimes();
        if (login != null) {
            os.write(aryEq(login.getBytes()));
            expectLastCall();
        }
        os.write(aryEq(input.getBytes()));
        expectLastCall();
        os.close();
        expectLastCall();

        InputStream is = createMock(InputStream.class);
        expect(p.getInputStream()).andReturn(is);
        Scanner isc = createMock(Scanner.class);
        expectNew(Scanner.class, is).andReturn(isc);
        expect(isc.hasNext()).andReturn(true);
        expect(isc.nextLine()).andReturn(output);
        expect(isc.hasNext()).andReturn(false);
        is.close();
        expectLastCall();

        InputStream es = createMock(InputStream.class);
        expect(p.getErrorStream()).andReturn(es);
        Scanner esc = createMock(Scanner.class);
        expectNew(Scanner.class, es).andReturn(esc);
        expect(esc.hasNext()).andReturn(true);
        expect(esc.nextLine()).andReturn(error);
        expect(esc.hasNext()).andReturn(false);
        es.close();
        expectLastCall();

        replayAll();
    }
}
