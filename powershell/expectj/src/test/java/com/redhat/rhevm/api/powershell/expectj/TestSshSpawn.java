package com.redhat.rhevm.api.powershell.expectj;

import java.io.InputStream;
import java.io.OutputStream;

import org.mockito.Mockito;

import com.jcraft.jsch.Channel;

import junit.framework.TestCase;

/**
 * Verify {@link SshSpawn}.
 *
 * @author johan.walles@gmail.com
 */
public class TestSshSpawn extends TestCase {
    /**
     * Verify that a channel session holds together.
     *
     * @throws Exception If testing goes exceptionally bad.
     */
    public void testSshSpawnChannel() throws Exception {
        InputStream inputStream = (InputStream)Mockito.mock(InputStream.class);
        OutputStream outputStream = (OutputStream)Mockito.mock(OutputStream.class);

        Channel channel = (Channel)Mockito.mock(Channel.class);
        Mockito.when(channel.getInputStream()).thenReturn(inputStream);
        Mockito.when(channel.getOutputStream()).thenReturn(outputStream);

        Spawnable testMe = new SshSpawn(channel);

        // This should be a no-op
        testMe.start();

        assertSame(inputStream, testMe.getStdout());
        assertSame(outputStream, testMe.getStdin());
        assertNull(testMe.getStderr());

        assertFalse(testMe.isClosed());
        ((Channel)(Mockito.verify(channel))).isClosed();

        testMe.stop();
        ((Channel)(Mockito.verify(channel))).disconnect();

        assertTrue(testMe.isClosed());

        assertTrue(testMe.getExitValue() == 0);
    }
}
