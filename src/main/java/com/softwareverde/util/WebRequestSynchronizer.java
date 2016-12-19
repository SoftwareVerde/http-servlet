package com.softwareverde.util;

public class WebRequestSynchronizer {
    private Runnable _onComplete;
    private final Integer _requiredCount;
    private Integer _completeCount = 0;

    public WebRequestSynchronizer(final Integer totalRequests, final Runnable onComplete) {
        _requiredCount = totalRequests;
        _onComplete = onComplete;
    }

    synchronized public void onComplete() {
        _completeCount += 1;
        if (_completeCount.equals(_requiredCount)) {
            _onComplete.run();
        }
    }
}
