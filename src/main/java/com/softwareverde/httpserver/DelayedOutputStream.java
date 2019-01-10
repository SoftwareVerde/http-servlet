package com.softwareverde.httpserver;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;

class DelayedOutputStream extends OutputStream {
    private final LinkedList<Integer> _bytes = new LinkedList<Integer>();
    private final OutputStream _rawOutputStream;
    private Boolean _isDelayed = true;

    public DelayedOutputStream(final OutputStream rawOutputStream) {
        _rawOutputStream = rawOutputStream;
    }

    @Override
    public void write(final int b) throws IOException {
        if (_isDelayed) {
            _bytes.addLast(b);
        }
        else {
            _rawOutputStream.write(b);
        }
    }

    public void delay() {
        _isDelayed = true;
    }

    public void resume() throws IOException {
        while (! _bytes.isEmpty()) {
            final Integer b = _bytes.removeFirst();
            _rawOutputStream.write(b);
        }

        _isDelayed = false;
    }

    public byte[] getDelayedPayload() {
        final byte[] bytes = new byte[_bytes.size()];
        int index = 0;
        for (final Integer b : _bytes) {
            bytes[index] = (byte) b.intValue();
            index += 1;
        }
        return bytes;
    }

    public void clearDelayedPayload() {
        _bytes.clear();
    }

    public OutputStream getRawOutputStream() {
        return _rawOutputStream;
    }

    public Boolean isDelayed() {
        return _isDelayed;
    }
}
