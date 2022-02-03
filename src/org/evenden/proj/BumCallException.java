package org.evenden.proj;

public class BumCallException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private int errno;

    public BumCallException(int errno) {
        this.errno = errno;
    }

    public int getErrno() {
        return errno;
    }

}
