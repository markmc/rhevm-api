package com.redhat.rhevm.api.powershell.expectj;


/**
 * Base class for spawnables providing an {@link #onClose()} method that should
 * be called on close.
 *
 * @author johan.walles@gmail.com
 */
public abstract class AbstractSpawnable implements Spawnable {
    /**
     * If non-null, will be notified {@link #onClose()}.
     */
    private CloseListener closeListener;

    public void setCloseListener(CloseListener closeListener) {
        synchronized (this) {
            this.closeListener = closeListener;
        }
    }

    /**
     * Call the close listener if we have one.
     */
    protected final void onClose() {
        synchronized (this) {
            if (closeListener != null) {
                closeListener.onClose();
            }
        }
    }
}
