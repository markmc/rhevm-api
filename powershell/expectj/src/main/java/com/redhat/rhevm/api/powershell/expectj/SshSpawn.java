package com.redhat.rhevm.api.powershell.expectj;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * A Spawnable for controlling an SSH session using ExpectJ.
 */
public class SshSpawn extends AbstractSpawnable implements Spawnable {
    /**
     * A reference to the remote host.
     */
    private String m_remoteHost;

    /**
     * The port we're talking to on the remote host.
     */
    private int m_remotePort;

    /**
     * Our communications channel to the remote host.
     */
    private Session m_session = null ;

    /**
     * Use this to read data from the remote host.
     */
    private OutputStream m_fromSocket;

    /**
     * Use this to write data to the remote host.
     */
    private InputStream m_toSocket;

    /**
     * The username with which to authenticate
     */
    private String m_username = null ;

    /**
     * The password with which to authenticate
     */
    private String m_password = null ;

    /**
     * The JSch Channel of type "shell"
     */
    private Channel m_channel = null ;

    /**
     * Construct a new SSH spawn.
     * @param remoteHostName The remote host to connect to.
     * @param remotePort The remote port to connect to.
     * @param username The user name with which to authenticate
     * @param password The password with which to authenticate
     */
    public SshSpawn(String remoteHostName, int remotePort, String username, String password) {
        m_remotePort = remotePort;
        m_remoteHost = remoteHostName ;
        this.m_username = username ;
        this.m_password = password ;
    }

    /**
     * Takes control over an existing SSH channel.
     *
     * @param channel The channel we should control.  If this channel isn't
     * already connected, {@link Channel#connect()} will be called.
     *
     * @throws IOException If connecting the channel fails.
     */
    public SshSpawn(Channel channel) throws IOException {
        if (!channel.isConnected()) {
            try {
                channel.connect();
            } catch (JSchException e) {
                throw new IOException("Failed connecting the channel", e) ;
            }
        }

        this.m_channel = channel;
        m_toSocket = m_channel.getInputStream();
        m_fromSocket = m_channel.getOutputStream();
    }

    public void start() throws IOException {
        if (m_toSocket != null) {
            // We've probably been created by the SshSpawn(Channel) constructor,
            // or start() has already been called.  No need to do anything
            // anyway.
            return;
        }

    	try {
			m_session = new JSch().getSession(m_username, m_remoteHost, m_remotePort) ;
			m_session.setPassword(m_password) ;
			m_session.setConfig("StrictHostKeyChecking", "no");
			m_session.connect() ;
			m_channel = m_session.openChannel("shell") ;
			m_channel.connect() ;
		} catch (JSchException e) {
			throw new IOException("Unable to establish SSH session/channel", e) ;
		}
        m_toSocket = m_channel.getInputStream() ;
        m_fromSocket = m_channel.getOutputStream();
    }

    public InputStream getStdout() {
        return m_toSocket;
    }

    public OutputStream getStdin() {
        return m_fromSocket;
    }

    public InputStream getStderr() {
        return null;
    }

    public boolean isClosed() {
        if (m_channel != null) {
            if (m_channel.isClosed()) {
                // We've been disconnected, shut down
                stop();
            }
        }
        return m_channel == null;
    }

    public int getExitValue() {
        return 0;
    }

    public void stop() {
        if (m_channel == null) {
            return;
        }

        m_channel.disconnect();
		m_channel = null;

		if (m_session != null) {
		    m_session.disconnect();
		    m_session = null;
		}
        m_toSocket = null;
        m_fromSocket = null;
    }
}
