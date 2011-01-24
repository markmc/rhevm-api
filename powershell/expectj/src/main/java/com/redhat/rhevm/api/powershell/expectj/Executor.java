package com.redhat.rhevm.api.powershell.expectj;

import java.io.IOException;

/**
 * This interface exists for people who want control over how processes are
 * launched.
 * <p>
 * Implementors are encouraged to implement {@link #toString()} for logging purposes.
 *
 * @see ExpectJ#spawn(String)
 * @author Johan Walles, johan.walles@gmail.com
 */
public interface Executor {
    /**
     * Creates a new process. This will only be called once.
     * @return The new process.
     * @throws IOException if there's a problem starting the new process.
     * @see #toString()
     */
    Process execute() throws IOException;

    /**
     * Describes what {@link #execute()} created.
     * @return A short description of what {@link #execute()} returns.
     */
    public String toString();
}
