package com.redhat.rhevm.api.powershell.expectj;

import java.io.IOException;
import java.net.UnknownHostException;

import com.jcraft.jsch.Channel;

/**
 * This class is the starting point of the ExpectJ Utility. This class
 * acts as factory for all {@link Spawn}s.
 *
 * @author	Sachin Shekar Shetty
 */
public class ExpectJ {
    /** Default timeout, -1 indicating wait for indefinite time */
    private long m_lDefaultTimeOutSeconds = -1;

    /**
     * Create a new ExpectJ with specified timeout setting.
     * @param defaultTimeoutSeconds default time out in seconds for the expect
     * commands on the spawned process.  -1 default time out indicates
     * indefinite timeout.
     */
    public ExpectJ(long defaultTimeoutSeconds) {
        m_lDefaultTimeOutSeconds = defaultTimeoutSeconds;
    }

    /**
     * Create a new ExpectJ with an infinite timeout.
     */
    public ExpectJ() {
        // This constructor intentionally left blank
    }

    /**
     * This method launches a {@link Spawnable}. Further expect commands can be
     * invoked on the returned {@link Spawn} object.
     *
     * @param spawnable spawnable to be executed
     * @return The newly spawned process
     * @throws IOException if the spawning fails
     */
    public Spawn spawn(Spawnable spawnable) throws IOException {
        return new Spawn(spawnable, m_lDefaultTimeOutSeconds);
    }

    /**
     * This method spawns a new process. Further expect commands can be invoked
     * on the returned {@link Spawn} object.
     *
     * @param command command to be executed
     * @return The newly spawned process
     * @throws IOException if the process spawning fails
     * @see Runtime#exec(String)
     */
    public Spawn spawn(final String command) throws IOException {
        return spawn(new ProcessSpawn(new Executor() {
            public Process execute() throws IOException {
                return Runtime.getRuntime().exec(command);
            }

            public String toString() {
                return command;
            }
        }));
    }

    /**
     * This method spawns a new process. Further expect commands can be invoked
     * on the returned {@link Spawn} object.
     *
     * @param executor Will be called upon to start the new process
     * @return The newly spawned process
     * @throws IOException if the process spawning fails
     * @see Runtime#exec(String[])
     */
    public Spawn spawn(Executor executor) throws IOException
    {
        return spawn(new ProcessSpawn(executor));
    }

    /**
     * This method spawns a telnet connection to the given host and port number.
     * Further expect commands can be invoked on the returned {@link Spawn}
     * object.
     *
     * @param hostName The name of the host to connect to.
     * @param port The remote port to connect to.
     * @return The newly spawned telnet session.
     * @throws IOException if the telnet spawning fails
     * @throws UnknownHostException if you specify a bogus host name
     *
     * @see TelnetSpawn
     * @see #spawn(String, int, String, String)
     * @see #spawn(Channel)
     */
    public Spawn spawn(String hostName, int port)
    throws IOException
    {
        return spawn(new TelnetSpawn(hostName, port));
    }

    /**
     * This method creates a spawn that controls an SSH connection.
     *
     * @param channel The SSH channel to control.
     *
     * @return A spawn controlling the SSH channel.
     *
     * @throws IOException If taking control over the SSH channel fails.
     *
     * @see #spawn(String, int, String, String)
     *
     * @see SshSpawn#SshSpawn(Channel)
     */
    public Spawn spawn(Channel channel) throws IOException {
        return spawn(new SshSpawn(channel));
    }

    /**
     * This method creates a spawn that controls an SSH connection.
     *
     * @param remoteHostName The remote host to connect to.
     *
     * @param remotePort The remote port to connect to.
     *
     * @param userName The user name with which to authenticate
     *
     * @param password The password with which to authenticate
     *
     * @return A spawn controlling the SSH channel.
     *
     * @throws IOException If taking control over the SSH channel fails.
     *
     * @see #spawn(Channel)
     *
     * @see SshSpawn#SshSpawn(String, int, String, String)
     */
    public Spawn spawn(String remoteHostName, int remotePort, String userName, String password) throws IOException {
        return spawn(new SshSpawn(remoteHostName, remotePort, userName, password));
    }
}
