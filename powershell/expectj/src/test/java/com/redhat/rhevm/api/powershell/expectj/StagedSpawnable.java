package com.redhat.rhevm.api.powershell.expectj;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;

import com.redhat.rhevm.api.powershell.expectj.AbstractSpawnable;
import com.redhat.rhevm.api.powershell.expectj.ExpectJException;
import com.redhat.rhevm.api.powershell.expectj.Spawnable;

/**
 * A spawnable implementation using a {@link StagedStringProducer} to produce data on
 * its stdout stream.
 *
 * @author johan.walles@gmail.com
 */
public class StagedSpawnable extends AbstractSpawnable implements Spawnable {
    /**
     * Write some stuff to a stream, with 500ms pauses between each write.
     */
    private class StagedStringProducer {
        /**
         * The user will read data from here.
         */
        private PipedInputStream inputStream;

        /**
         * The string producing thread.
         */
        private ProducerThread producerThread;

        /**
         * Thread that writes data to the pipe in stages.
         * @author johan.walles@gmail.com
         */
        private class ProducerThread
        extends Thread {
            /**
             * Write these strings to the output stream.
             */
            private String writeUs[];

            /**
             * Write strings to here.
             */
            OutputStream destination;

            /**
             * Are we done?
             */
            private boolean done = false;

            /**
             * @param destination Where to write data to.
             * @param stringsToWrite The data to write.
             */
            public ProducerThread(OutputStream destination, String stringsToWrite[]) {
                this.destination = destination;
                this.writeUs = stringsToWrite;
                this.setName("ExpectJ String Producer");
            }

            public void run() {
                PrintStream output = new PrintStream(this.destination);
                boolean justStarting = true;
                for (int i = 0; i < writeUs.length ; i++) {
                    if (!justStarting) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            // This exception intentionally ignored
                        }
                    }
                    justStarting = false;

                    String writeMe = writeUs[i];
                    if (writeMe != null) {
                        output.append(writeMe);
                    }
                }
                output.close();
                setDone();
                onClose();
            }

            /**
             * Call this when we've written all data.
             */
            private synchronized void setDone()
            {
                this.done = true;
            }

            /**
             * Are we done yet?
             * @return true if we're done writing all data.  False otherwise.
             */
            public synchronized boolean isDone()
            {
                return done;
            }
        }

        /**
         * Construct a staged string producer.
         * <p>
         * Strings will be produced with a 500ms delay between each.  There will be no delay
         * before the first or after the last one.  A null entry means "don't create any
         * string here".
         * <p>
         * Strings can be read from the {@link #getStringStream()} stream.
         *
         * @param stringsToProduce The strings to produce.
         * @exception IOException If an IO error occurs.
         */
        public StagedStringProducer(String stringsToProduce[])
        throws IOException
        {
            this.inputStream = new PipedInputStream();
            OutputStream outputStream = new PipedOutputStream(this.inputStream);
            this.producerThread = new ProducerThread(outputStream, stringsToProduce);
            this.producerThread.start();
        }

        /**
         * @return A reference to the stream on which we'll produce data.
         */
        public InputStream getStringStream() {
            return this.inputStream;
        }

        /**
         * @return true if we have finished producing data.  False otherwise.
         */
        public boolean done() {
            return this.producerThread.isDone();
        }
    }

    /**
     * We use this to produce strings.
     */
    private StagedStringProducer stringProducer;

    /**
     * Construct a staged string spawn.  The spawn will produce the requested strings
     * on its {@link #getStdout()} stream.
     * <p>
     * Strings will be produced with a 500ms delay between each.  There will be no delay
     * before the first or after the last one.  A null entry means "don't create any
     * string here".
     *
     * @see #StagedSpawnable(String...)
     * @param stringsToProduce The strings to produce.
     * @throws IOException on trouble creating the string spawn.
     */
    public StagedSpawnable(String stringsToProduce[])
    throws IOException
    {
        this.stringProducer = new StagedStringProducer(stringsToProduce);
    }

    public void stop() {
        // This method intentionally left blank
    }

    public void start() {
        // This method intentionally left blank
    }

    public boolean isClosed() {
        return stringProducer.done();
    }

    public OutputStream getStdin() {
        return null;
    }

    public InputStream getStdout() {
        return this.stringProducer.getStringStream();
    }

    public int getExitValue() throws ExpectJException {
        return 0;
    }

    public InputStream getStderr() {
        return null;
    }
}
