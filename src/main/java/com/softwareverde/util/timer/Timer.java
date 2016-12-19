package com.softwareverde.util.timer;

public class Timer {
    protected Long _startTime;
    protected Long _endTime;

    protected Long _getDuration() {
        if (_endTime != null && _startTime != null) {
            return (_endTime - _startTime);
        }
        return Long.MAX_VALUE;
    }

    public Timer() { }

    public void start() {
        _startTime = System.currentTimeMillis();
    }

    public void stop() {
        _endTime = System.currentTimeMillis();
    }

    public Long getDuration() {
        return _getDuration();
    }
}
