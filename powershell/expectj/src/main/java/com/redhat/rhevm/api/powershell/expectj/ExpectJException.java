package com.redhat.rhevm.api.powershell.expectj;

/**
 * This class extends the Exception class and encapsulates other exceptions.
 *
 * @author	Sachin Shekar Shetty
 */
public class ExpectJException extends Exception {
    /**
     * Create a new exception with an explanatory message.
     * @param message An explanation of what went wrong.
     */
    ExpectJException(String message) {
        super(message);
    }

    /**
     * Create a new exception with an explanatory message and a reference to an exception
     * that made us throw this one.
     * @param message An explanation of what went wrong.
     * @param cause Another exception that is the reason to throw this one.
     */
    ExpectJException(String message, Throwable cause) {
        super(message, cause);
    }
}
