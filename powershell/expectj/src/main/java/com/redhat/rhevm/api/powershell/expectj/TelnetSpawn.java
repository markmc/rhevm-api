package com.redhat.rhevm.api.powershell.expectj;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * A Spawnable for controlling a telnet session using ExpectJ.
 * @author Johan Walles
 */
class TelnetSpawn extends AbstractSpawnable implements Spawnable {
    /**
     * A reference to the remote host.
     */
    private InetAddress m_remoteHost;

    /**
     * The port we're talking to on the remote host.
     */
    private int m_remotePort;

    /**
     * Our communications channel to the remote host.
     */
    private Socket m_socket;

    /**
     * Use this to read data from the remote host.
     */
    private InputStream m_fromSocket;

    /**
     * Use this to write data to the remote host.
     */
    private OutputStream m_toSocket;

    /**
     * Construct a new telnet spawn.
     * @param remoteHostName The remote host to connect to.
     * @param remotePort The remote port to connect to.
     * @throws UnknownHostException If the name of the remote host cannot be looked up
     */
    public TelnetSpawn(String remoteHostName, int remotePort) throws UnknownHostException {
        m_remotePort = remotePort;
        m_remoteHost = InetAddress.getByName(remoteHostName);
    }

    public void start() throws IOException {
        m_socket = new Socket(m_remoteHost, m_remotePort);
        m_fromSocket = m_socket.getInputStream();
        m_toSocket = m_socket.getOutputStream();
    }

    public InputStream getStdout() {
        return m_fromSocket;
    }

    public OutputStream getStdin() {
        return m_toSocket;
    }

    public InputStream getStderr() {
        return null;
    }

    public boolean isClosed() {
        if (m_socket != null) {
            if (m_socket.isClosed()) {
                // We've been disconnected, shut down
                stop();
            }
        }
        return m_socket == null;
    }

    public int getExitValue() {
        return 0;
    }

    public void stop() {
        if (m_socket == null) {
            return;
        }

        try {
            m_socket.close();
        } catch (IOException ignored) {
            // Failure: When your best just isn't good enough.
        }
        m_socket = null;
        m_fromSocket = null;
        m_toSocket = null;
    }
}
