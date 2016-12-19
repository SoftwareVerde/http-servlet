package com.softwareverde.util.timer;

public class LoggingTimer extends Timer {
    protected String _timerTitle;
    protected Long _maxDuration;

    public LoggingTimer(final String timerTitle, final Long maxDuration) {
        _timerTitle = timerTitle;
        _maxDuration = maxDuration;
    }

    @Override
    public void stop() {
        super.stop();

        final Long duration = _getDuration();
        if (duration > _maxDuration) {
            System.err.println(_timerTitle + " - Exceeded duration. "+ duration +"ms, Max: "+ _maxDuration +"ms.\n");
        }
    }
}
