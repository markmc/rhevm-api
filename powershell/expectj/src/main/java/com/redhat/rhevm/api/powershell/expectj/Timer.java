package com.redhat.rhevm.api.powershell.expectj;


/**
 * This class acts like a timer and invokes the listener on time-out.
 */
class Timer implements Runnable {
    /**
     * The time interval in milliseconds up to which the process
     * should be allowed to run.
     */
    private long timeOut = 0;

    /**
     * The entity that wants to be notified on timeout.
     */
    private TimerEventListener listener = null;

    /**
     * The waiting thread.
     */
    private Thread thread = null;

    /**
     * Timer not started.
     */
    public static final int NOT_STARTED = 0;

    /**
     * Timer started and still running.
     */
    public static final int STARTED     = 1;

    /**
     * Timer timed out.
     */
    public static final int TIMEDOUT    = 2;

    /**
     * Timer interrupted.
     */
    public static final int INTERRUPTED = 3;

    /**
     * Stores the current status of Timer
     */
    private int currentStatus = NOT_STARTED;

    /**
     * Are we there yet?
     */
    private boolean done = false;

    /**
     * Constructor
     *
     * @param timeOut  Time interval after which the listener will be
     *                 invoked
     * @param listener Object implementing the TimerEventListener
     *                 interface
     */
    public Timer(long timeOut, TimerEventListener listener) {

        if (timeOut < 1) {
            throw new IllegalArgumentException("Time-Out value cannot be < 1");
        }
        if (listener == null ) {
            throw new IllegalArgumentException("Listener cannot be null");
        }
        this.timeOut = timeOut * 1000;
        this.listener = listener;

     }

    /**
     * Starts the timer
     */
    public void startTimer() {
        thread = new Thread(this, "ExpectJ Timer Thread, " + timeOut + "ms");
        currentStatus = STARTED;
        thread.start();
    }

    /**
     * Return timer status.  Can be one of {@link #NOT_STARTED}, {@link #STARTED},
     * {@link #TIMEDOUT} or {@link #INTERRUPTED}.
     *
     * @return the status of the timer
     */
    public int getStatus() {
        return currentStatus;
    }

    /**
     * Close the timer prematurely.  The event listener won't get any
     * notifications.
     */
    public void close() {
        synchronized (this) {
            done = true;
            this.notify();
        }
    }

    /**
     * This is the timer thread main.
     */
    public void run() {
        try {
            // Sleep for the specified time
            synchronized (this) {
                this.wait(timeOut);
                if (done) {
                    // We've been nicely asked to quit
                    return;
                }

                // Jag Utha Shaitan, Its time to invoke the listener
                currentStatus = TIMEDOUT;
                listener.timerTimedOut();
            }
        } catch (InterruptedException iexp) {
            currentStatus = INTERRUPTED;
            listener.timerInterrupted(iexp);
        }
    }
}
