package com.redhat.rhevm.api.powershell.expectj;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Serves strings on a telnet port with 500ms between each string.
 *
 * @author johan.walles@gmail.com
 */
class StringServer {
    /**
     * The server socket we're listening on.
     */
    private ServerSocket listener;

    /**
     * Create a new string server.  This server will drop all connections
     * immediately.
     *
     * @throws IOException If we're unable to open a port to listen to.
     */
    public StringServer() throws IOException {
        launchListenerThread(null);
    }

    /**
     * Create a new strings producer.
     *
     * @param strings The strings to produce.
     *
     * @throws IOException If we're unable to open a port to listen to.
     */
    public StringServer(String strings[]) throws IOException {
        launchListenerThread(strings);
    }

    /**
     * Launch the socket listener thread.
     *
     * @param strings The strings to produce.
     *
     * @throws IOException If we're unable to open a port to listen to.
     */
    private void launchListenerThread(final String strings[])
    throws IOException
    {
        listener = new ServerSocket(0);

        String threadName =
            "Connections dropper , (port " + listener.getLocalPort() + ")";
        new Thread(new Runnable() {
            public void run() {
                try {
                    while (true) {
                        Socket incoming = listener.accept();
                        if (strings != null ) {
                            OutputStreamWriter out =
                                new OutputStreamWriter(incoming.getOutputStream());
                            for (int i = 0; i < strings.length; i++) {
                                Thread.sleep(500);
                                out.append(strings[i]);
                                out.flush();
                            }
                        }
                        incoming.close();
                    }
                } catch (IOException e) {
                    // Just quit listening if we fail, it probably means
                    // somebody closed our socket on purpose.
                } catch (InterruptedException e) {
                    // Just quit listening if we get interrupted.
                }
            }
        }, threadName).start();
    }

    /**
     * Which port are we listening to?
     *
     * @return The local port we're listening to.
     */
    public int getListeningPort() {
        return listener.getLocalPort();
    }

    /**
     * Stop listening for connections.
     *
     * @throws IOException if we can't stop raving
     */
    public void close() throws IOException {
        listener.close();
    }
}