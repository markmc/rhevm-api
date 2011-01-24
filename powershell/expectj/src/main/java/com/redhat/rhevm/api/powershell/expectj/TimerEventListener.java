package com.redhat.rhevm.api.powershell.expectj;

/**
 * This interface represents the events triggered by Timer class
 */
interface TimerEventListener {
    /**
     * Called when the timer times out.
     */
    void timerTimedOut();

    /**
     * This method is invoked by the Timer, when the timer thread
     * receives an interrupted exception.
     *
     * @param reason Why we were interrupted.
     */
    void timerInterrupted(InterruptedException reason);
}
