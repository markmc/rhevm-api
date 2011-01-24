package com.redhat.rhevm.api.powershell.expectj;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.mockito.Mockito;


import junit.framework.TestCase;

/**
 * Verify that the different expect() methods of {@link Spawn} work as expected.
 * @author johan.walles@gmail.com
 */
public class TestExpect extends TestCase {
    /**
     * The number of file handles available to us.
     *
     * @see #getLeakTestIterations()
     */
    private int availableFileHandles = 0;

    /**
     * We can {@link Runtime#exec(String)} this to start an FTP client.
     */
    private String ftpBinary;

    /**
     * How many iterations must we run a leak test before we can be confident
     * we aren't leaking any file handles?
     *
     * @return The number of file handles available plus some marigin.
     *
     * @throws IOException If counting the number of file handles fails.
     */
    private int getLeakTestIterations() throws IOException {
        if (availableFileHandles == 0) {
            File dummy = File.createTempFile("testExpectJ", ".tmp");
            dummy.deleteOnExit();
            List fileHandles = new LinkedList();
            try {
                while (true) {
                    fileHandles.add(new FileReader(dummy));
                }
            } catch (IOException e) {
                // Assume the IOE was because we ran out of file handles
                Iterator iter = fileHandles.iterator();
                while (iter.hasNext()) {
                    FileReader fileHandle = (FileReader)iter.next();
                    fileHandle.close();
                }
                availableFileHandles = fileHandles.size() + 42;
            } finally {
                assertTrue(dummy.delete());
            }
        }

        return availableFileHandles;
    }

    /**
     * Generate a Spawn producing the indicated output on stdout.
     * @param strings The strings to print to stdout.  Strings will be produced with
     * 500ms between each.
     * @return A new Spawn.
     * @throws Exception when things go wrong.
     */
    private Spawn getSpawn(String strings[]) throws Exception {
        return new ExpectJ().spawn(new StagedSpawnable(strings));
    }

    /**
     * Test that we can find simple strings.
     * @throws Exception if things go wrong.
     */
    public void testExpectStrings() throws Exception {
        Spawn testMe = getSpawn(new String[] {"flaska", "gris"});
        testMe.expect("flaska");
        testMe.expect("gris");
    }

    /**
     * Test that we can find simple strings with an unexpected string in between.
     * @throws Exception if things go wrong.
     */
    public void testExpectStringsWithExtraData() throws Exception {
        Spawn testMe =
            getSpawn(new String[] {"flaska", "nyckel", "gris"});
        testMe.expect("flaska");
        testMe.expect("gris");
    }

    /**
     * Verify that we get an exception if we don't get any match.
     *
     * @throws Exception if testing goes exceptionally wrong.
     */
    public void testNoMatchFound() throws Exception {
        Spawn spawn = getSpawn(new String[0]);
        try {
            spawn.expect("won't be found");
            fail("Expected IO exception if stream closed before match");
        } catch (IOException e) {
            // Expected exception intentionally ignored
        }
    }

    /**
     * Test that we get notified about closes.
     * @throws Exception if things go wrong.
     */
    public void testExpectClose() throws Exception {
        Spawn testMe = getSpawn(new String[] {"flaska", "gris"});
        testMe.expectClose();
    }

    /**
     * Verify {@link Spawn#getCurrentStandardOutContents()}.
     *
     * @throws Exception if things go wrong.
     */
    public void testGetStdOut() throws Exception {
        Spawn testMe = getSpawn(new String[] {"flaska", "gris"});
        testMe.expect("flaska");
        testMe.expect("gris");
        assertEquals("flaskagris", testMe.getCurrentStandardOutContents());
        testMe.expectClose();
        assertEquals("flaskagris", testMe.getCurrentStandardOutContents());
    }

    /**
     * Test that we time out properly when we don't find what we're looking for.
     * @throws Exception if things go wrong.
     */
    public void testTimeout() throws Exception {
        // Test longer duration output than timeout
        Spawn testMe =
            getSpawn(new String[] {"flaska", "nyckel", "gris", "hink", "bil", "stork"});
        Date beforeTimeout = new Date();
        try {
            testMe.expect("klubba", 1);
            fail("expect() should have timed out");
        } catch (TimeoutException expected) {
            // Ignoring expected exception
        }
        Date afterTimeout = new Date();

        long msElapsed = afterTimeout.getTime() - beforeTimeout.getTime();
        if (msElapsed < 900 || msElapsed > 1100) {
            fail("expect() should have timed out after 1s, timed out in "
                 + msElapsed
                 + "ms");
        }

        testMe =
            getSpawn(new String[] {"flaska", "nyckel", "gris", "hink", "bil", "stork"});
        beforeTimeout = new Date();
        try {
            testMe.expectClose(1);
            fail("expectClose() should have timed out");
        } catch (TimeoutException expected) {
            // Ignoring expected exception
        }
        afterTimeout = new Date();

        msElapsed = afterTimeout.getTime() - beforeTimeout.getTime();
        if (msElapsed < 900 || msElapsed > 1100) {
            fail("expectClose() should have timed out after 1s, timed out in "
                 + msElapsed
                 + "ms");
        }
    }

    /**
     * Create a spawned process. This method hopefully works on Windows as well.
     * @return a Spawn representing a process
     * @throws Exception on trouble
     */
    private Spawn getSpawnedProcess() throws Exception {
        if (ftpBinary != null) {
            return new ExpectJ(5).spawn(ftpBinary);
        }

        // Try a couple of different FTP binaries
        IOException throwMe = null;
        try {
            Spawn spawn = new ExpectJ(5).spawn("/bin/ftp");
            ftpBinary = "/bin/ftp";
            return spawn;
        } catch (IOException e) {
            // IOException probably means "binary not found"
            throwMe = e;
        }

        try {
            Spawn spawn = new ExpectJ(5).spawn("ftp.exe");
            ftpBinary = "ftp.exe";
            return spawn;
        } catch (IOException e) {
            // This exception intentionally ignored
        }

        try {
            Spawn spawn = new ExpectJ(5).spawn("/usr/bin/ftp");
            ftpBinary = "/usr/bin/ftp";
            return spawn;
        } catch (IOException e) {
            // This exception intentionally ignored
        }

        try {
            Spawn spawn = new ExpectJ(5).spawn("/usr/bin/lftp");
            ftpBinary = "/usr/bin/lftp";
            return spawn;
        } catch (IOException e) {
            // This exception intentionally ignored
        }

        // Report problem
        throw throwMe;
    }

    /**
     * Verify that stopping a process spawn works as it should.
     * @throws Exception on trouble
     */
    public void testStopProcess() throws Exception {
        Spawn process = getSpawnedProcess();
        assertFalse(process.isClosed());

        // Process should be closed after it has been stopped
        process.stop();
        assertTrue("Process didn't close after calling stop()",
                   process.isClosed());

        // A closed process should return at once on expectClose()
        Date beforeExpectClose = new Date();
        process.expectClose();
        Date afterExpectClose = new Date();
        long dms = afterExpectClose.getTime() - beforeExpectClose.getTime();
        assertTrue(dms < 10);

        // Process should still be closed
        assertTrue(process.isClosed());
    }

    /**
     * Verify that waiting for a process spawn to finish and then stopping it
     * works as it should.
     *
     * @throws Exception on trouble
     */
    public void testFinishProcess1() throws Exception {
        Spawn process = getSpawnedProcess();
        assertFalse(process.isClosed());

        process.send("quit\n");
        process.expectClose();
        process.stop();
        assertTrue("Process wasn't closed after stop", process.isClosed());

        assertEquals(0, process.getExitValue());
    }

    /**
     * Verify that waiting for a process spawn to finish works as it should.
     * @throws Exception on trouble
     */
    public void testFinishProcess2() throws Exception {
        Spawn process = getSpawnedProcess();
        assertFalse(process.isClosed());

        // Process should be closed after it finishes
        process.send("quit\n");
        process.expectClose();
        assertTrue("Process wasn't closed after finishing", process.isClosed());

        assertEquals(0, process.getExitValue());
    }

    /**
     * Spawn a ton of stuff in the hope that we'll get an exception if we leak
     * resources somewhere.
     * @throws Exception on trouble.
     */
    private void spawnLeaks1() throws Exception {
        Spawnable dummySpawn = (Spawnable)Mockito.mock(Spawnable.class);
        Mockito.when(Boolean.valueOf(dummySpawn.isClosed())).thenReturn(Boolean.TRUE);
        InputStream empty = new ByteArrayInputStream(new byte[0]);
        Mockito.when(dummySpawn.getStdout()).thenReturn(empty);
        for (int i = 0; i < getLeakTestIterations(); i++) {
            try {
                new ExpectJ().spawn(dummySpawn).expectClose(1);
            } catch (Exception e) {
                throw new Exception("Leak test 1 failed after " + i + " iterations", e);
            }
        }
    }

    /**
     * Spawn a ton of stuff in the hope that we'll get an exception if we leak
     * resources somewhere.
     * @throws Exception on trouble.
     */
    private void sawnLeaks2() throws Exception {
        for (int i = 0; i < getLeakTestIterations(); i++) {
            try {
                getSpawn(new String[0]).expectClose(1);
            } catch (Exception e) {
                throw new Exception("Leak test 2 failed after " + i + " iterations", e);
            }
        }
    }

    /**
     * Verify {@link TelnetSpawn} behavior.
     *
     * @throws Exception if testing goes really bad
     */
    public void testTelnetSpawn() throws Exception {
        StringServer dropper = new StringServer();
        try {
            Spawn spawn =
                new ExpectJ().spawn("127.0.0.1", dropper.getListeningPort());

            // Expecting close when already closed should return immediately
            spawn.expectClose();

            // Stopping a closed spawn should be a no-op
            spawn.stop();

            // Telnet exit value is always 0
            assertEquals(0, spawn.getExitValue());
        } finally {
            dropper.close();
            dropper = null;
        }

        StringServer stager = new StringServer(new String[] {
            "gris", "hej"
        });

        try {
            Spawn spawn =
                new ExpectJ().spawn("127.0.0.1", stager.getListeningPort());
            spawn.expect("gris");
            assertEquals("gris", spawn.getCurrentStandardOutContents());
            Thread.sleep(1000);  // 1000ms = twice the interval used by the StringServer
            assertEquals("grishej", spawn.getCurrentStandardOutContents());
            spawn.expectClose();
            assertEquals("grishej", spawn.getCurrentStandardOutContents());

            // Stopping a closed spawn should be a no-op
            spawn.stop();

            // Telnet exit value is always 0
            assertEquals(0, spawn.getExitValue());
        } finally {
            stager.close();
            stager = null;
        }
    }

    /**
     * Create a bunch of telnet spawns in the hope that we'll get an exception
     * if we leak resources somewhere.
     *
     * @throws Exception on trouble.
     */
    private void spawnLeaks3() throws Exception {
        StringServer dropper = new StringServer();
        for (int i = 0; i < getLeakTestIterations(); i++) {
            try {
                new ExpectJ().spawn("127.0.0.1", dropper.getListeningPort()).expectClose(1);
            } catch (Exception e) {
                throw new Exception("Leak test 3 failed after " + i + " iterations", e);
            }
        }
        dropper.close();
    }

    /**
     * Spawn a ton of processes in the hope that we'll get an exception if we
     * leak resources somewhere.
     * @throws Exception on trouble.
     */
    private void spawnLeaks4() throws Exception {
        // Spawn several processes in parallel as it goes a lot faster than
        // doing one at a time.  More than 10 didn't help.
        final int PARALLELLISM = 10;

        long t0 = System.currentTimeMillis();
        long lastProgressUpdate = t0;
        for (int i = 0; i < getLeakTestIterations(); i += PARALLELLISM) {
            try {
                Spawn spawns[] = new Spawn[PARALLELLISM];
                for (int j = 0; j < spawns.length; j++) {
                    spawns[j] = getSpawnedProcess();
                }

                // Exit FTP nicely
                for (int j = 0; j < spawns.length; j++) {
                    spawns[j].send("quit\n");
                }

                for (int j = 0; j < spawns.length; j++) {
                    spawns[j].expectClose(1);
                }

                long now = System.currentTimeMillis();
                if (now - lastProgressUpdate > 3000) {
                    // This takes a while, print some progress every 3s...
                    double dSeconds = (now - t0) / 1000.0;
                    double hz = (i + 1) / dSeconds;
                    int iterLeft = getLeakTestIterations() - i - 1;
                    double eta = iterLeft / hz;
                    System.out.format("%4d/%d iterations done in %.1fs at %.1fHz, ETA: %.1fs\n",
                        new Object[] {
                        Integer.valueOf(i + 1),
                        Integer.valueOf(getLeakTestIterations()),
                        Double.valueOf(dSeconds),
                        Double.valueOf(hz),
                        Double.valueOf(eta)
                    });
                    lastProgressUpdate = now;
                }
            } catch (Exception e) {
                throw new Exception("Leak test 4 failed after " + i + " iterations", e);
            }
        }
    }

    /**
     * Spawn a ton of processes and then stop them in the hope that we'll get an
     * exception if we leak resources somewhere.
     *
     * @throws Exception on trouble.
     */
    private void spawnLeaks5() throws Exception {
        // Spawn several processes in parallel as it goes a lot faster than
        // doing one at a time.  More than 10 didn't help.
        final int PARALLELLISM = 10;

        long t0 = System.currentTimeMillis();
        long lastProgressUpdate = t0;
        for (int i = 0; i < getLeakTestIterations(); i += PARALLELLISM) {
            try {
                Spawn spawns[] = new Spawn[PARALLELLISM];
                for (int j = 0; j < spawns.length; j++) {
                    spawns[j] = getSpawnedProcess();
                }

                // Kill it!
                for (int j = 0; j < spawns.length; j++) {
                    spawns[j].stop();
                }

                long now = System.currentTimeMillis();
                if (now - lastProgressUpdate > 3000) {
                    // This takes a while, print some progress every 3s...
                    double dSeconds = (now - t0) / 1000.0;
                    double hz = (i + 1) / dSeconds;
                    int iterLeft = getLeakTestIterations() - i - 1;
                    double eta = iterLeft / hz;
                    System.out.format("%4d/%d iterations done in %.1fs at %.1fHz, ETA: %.1fs\n",
                        new Object[] {
                        Integer.valueOf(i + 1),
                        Integer.valueOf(getLeakTestIterations()),
                        Double.valueOf(dSeconds),
                        Double.valueOf(hz),
                        Double.valueOf(eta)
                    });
                    lastProgressUpdate = now;
                }
            } catch (Exception e) {
                throw new Exception("Leak test 5 failed after " + i + " iterations", e);
            }
        }
    }
}
