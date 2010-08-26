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
import javax.ws.rs.core.Response;

import expectj.ExpectJ;
import expectj.Spawn;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import org.junit.runner.RunWith;

import static org.easymock.classextension.EasyMock.expect;
import static org.easymock.classextension.EasyMock.expectLastCall;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.expectNew;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

import com.redhat.rhevm.api.common.invocation.Current;
import com.redhat.rhevm.api.common.security.auth.Challenger;
import com.redhat.rhevm.api.common.security.auth.Principal;

@RunWith(PowerMockRunner.class)
@PrepareForTest({PowerShellCmd.class})
public class PowerShellCmdTest extends Assert {

    private static final String SYSWOW64_PATH = "C:\\Windows\\SysWOW64\\WindowsPowerShell\\v1.0\\powershell.exe";
    private static final String SCRIPT = "hello";
    private static final String OUTPUT = "hello there!";
    private static final String ERROR = "Hello!\nHello!\nIs this thing on?";
    private static final int EXIT_STATUS = 9;
    private static final String DOMAIN = "LeinsterHouse";
    private static final String USER = "Jackie";
    private static final String PASSWORD = "HealyRae";
    private static final String LOGIN_USER = "login-user -domain \"" + DOMAIN + "\" -username \"" + USER + "\" -password \"" + PASSWORD + "\";\n";
    private static final String LOGIN_FAILED = "Login failed. Please verify your login information";
    private static final String LOGIN_USER_NO_DOMAIN = "login-user -username \"" + USER + "\" -password \"" + PASSWORD + "\";\n";

    @After
    public void tearDown() {
        verifyAll();
    }

    @Test
    public void testSimple() throws Exception {
        PowerShellCmd cmd = new PowerShellCmd(null, setupExpectations(null, false));

        cmd.start();

        assertEquals(EXIT_STATUS, cmd.run(SCRIPT));
        assertEquals(OUTPUT, cmd.getStdOut());
        assertEquals(ERROR, cmd.getStdErr());

        cmd.stop();
    }

    @Test
    public void testSysWow64() throws Exception {
        PowerShellCmd cmd = new PowerShellCmd(null, setupExpectations(null, true));

        cmd.start();

        assertEquals(EXIT_STATUS, cmd.run(SCRIPT));
        assertEquals(OUTPUT, cmd.getStdOut());
        assertEquals(ERROR, cmd.getStdErr());

        cmd.stop();
    }

    @Test
    public void testAuth() throws Exception {
        PowerShellCmd cmd = new PowerShellCmd(new Principal(USER, PASSWORD, DOMAIN), setupExpectations(LOGIN_USER, false));

        cmd.start();

        assertEquals(EXIT_STATUS, cmd.run(SCRIPT));
        assertEquals(OUTPUT, cmd.getStdOut());
        assertEquals(ERROR, cmd.getStdErr());

        cmd.stop();
    }

    @Test
    public void testUnAuth() throws Exception {
        PowerShellCmd cmd = new PowerShellCmd(new Principal(USER, PASSWORD, DOMAIN), setupExpectations(LOGIN_USER, false, LOGIN_FAILED));
        cmd.start();

        assertEquals(EXIT_STATUS, cmd.run(SCRIPT));
        assertEquals(OUTPUT, cmd.getStdOut());
        assertEquals(LOGIN_FAILED, cmd.getStdErr());

        try {
            cmd.complete(EXIT_STATUS, SCRIPT);
            fail("expected WebApplicationException on login failure");
        } catch (WebApplicationException wae) {
            assertNotNull(wae.getResponse());
        }
    }

    @Test
    public void testAuthNoDomain() throws Exception {
        PowerShellCmd cmd = new PowerShellCmd(new Principal(USER, PASSWORD), setupExpectations(LOGIN_USER_NO_DOMAIN, false));

        cmd.start();

        assertEquals(EXIT_STATUS, cmd.run(SCRIPT));
        assertEquals(OUTPUT, cmd.getStdOut());
        assertEquals(ERROR, cmd.getStdErr());

        cmd.stop();
    }

    private String getScript() {
        return "Write-Host \"<output>\";\n  $result = Invoke-Expression '" + SCRIPT + "';\n  $result | ConvertTo-XML -As String -Depth 5;\n  $status = 0;\nWrite-Host \"</output> $status\";\n";
    }

    private String getOutput() {
        return "<output>\n" + OUTPUT + "</output> " + Integer.toString(EXIT_STATUS) + "\n";
    }

    public Current setupExpectations(String login, boolean syswow64) throws Exception {
        return setupExpectations(login, syswow64, ERROR);
    }

    public Current setupExpectations(String login, boolean syswow64, String error) throws Exception {
        Current current = createMock(Current.class);
        if (LOGIN_FAILED.equals(error)) {
            Challenger challenger = createMock(Challenger.class);
            expect(current.get(Challenger.class)).andReturn(challenger);
            Response response = createMock(Response.class);
            expect(challenger.getChallenge()).andReturn(response);
        }
        File f = createMock(File.class);
        expectNew(File.class, SYSWOW64_PATH).andReturn(f);
        expect(f.exists()).andReturn(syswow64);

        Spawn spawn = createMock(Spawn.class);
        ExpectJ ej = createMock(ExpectJ.class);
        expectNew(ExpectJ.class).andReturn(ej);
        expect(ej.spawn((syswow64 ? SYSWOW64_PATH : "powershell.exe") + " -command -")).andReturn(spawn);

        if (login != null) {
            spawn.send(login);
            expectLastCall();
        }

        spawn.send(getScript());
        expectLastCall();

        spawn.expect("</output>");

        spawn.stop();
        expectLastCall();

        expect(spawn.getCurrentStandardOutContents()).andReturn(getOutput());
        expect(spawn.getCurrentStandardErrContents()).andReturn(error);

        replayAll();
        return current;
    }
}
