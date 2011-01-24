package com.redhat.rhevm.api.powershell.expectj;

/**
 * Timeout while waiting for a {@link Spawn}.
 * @author johan.walles@gmail.com
 */
public class TimeoutException extends Exception {
    /**
     * Create a new exception with an explanatory message.
     * @param message An explanation of what went wrong.
     */
    TimeoutException(String message) {
        super(message);
    }

    /**
     * Create a new exception with an explanatory message and a reference to an exception
     * that made us throw this one.
     * @param message An explanation of what went wrong.
     * @param cause Another exception that is the reason to throw this one.
     */
    TimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
