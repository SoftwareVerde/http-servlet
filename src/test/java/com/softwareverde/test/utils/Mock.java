package com.softwareverde.test.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Mock {
    public static class Invocation {
        final List<Object> _parameters = new ArrayList<Object>();

        public Invocation() { }
        public Invocation(Object... parameters) {
            for (final Object parameter : parameters) {
                _parameters.add(parameter);
            }
        }

        public <T> void addParameter(final T parameter) {
            _parameters.add(parameter);
        }

        public <T> T getParameter(final Integer i) {
            return (T) _parameters.get(i);
        }
    }

    private static String _getMockedFunctionName() {
        final StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        return stackTraceElements[3].getMethodName();
    }

    public static class UnexpectedInvocationException extends RuntimeException {
        public UnexpectedInvocationException() {
            super("Unexpected invocation of function: "+ _getMockedFunctionName());
        }
    }

    private Map<String, List<Invocation>> _invocations = new HashMap<String, List<Invocation>>();
    private Map<String, List<Object>> _returnValues = new HashMap<String, List<Object>>();
    private Map<String, Integer> _returnValueCount = new HashMap<String, Integer>();

    public List<Invocation> getInvocations(final String functionName) {
        return _invocations.get(functionName);
    }

    public void setReturnValue(final String functionName, final Object returnValue) {
        if (! _returnValues.containsKey(functionName)) {
            _returnValues.put(functionName, new ArrayList<Object>());
            _returnValueCount.put(functionName, 0);
        }

        _returnValues.get(functionName).add(returnValue);
    }

    public void recordInvocation() {
        final String functionName = _getMockedFunctionName();

        if (! _invocations.containsKey(functionName)) {
            _invocations.put(functionName, new ArrayList<Invocation>());
        }

        _invocations.get(functionName).add(null);
    }
    public void recordInvocation(Object... parameters) {
        final String functionName = _getMockedFunctionName();

        if (! _invocations.containsKey(functionName)) {
            _invocations.put(functionName, new ArrayList<Invocation>());
        }

        final Invocation invocation = new Invocation();
        for (final Object parameter : parameters) {
            invocation.addParameter(parameter);
        }

        _invocations.get(functionName).add(invocation);
    }

    public <T> T getReturnValue() {
        final String functionName = _getMockedFunctionName();

        if (! _returnValues.containsKey(functionName)) {
            throw new UnexpectedInvocationException();
        }

        final Integer invocationCount = _returnValueCount.get(functionName);
        _returnValueCount.put(functionName, invocationCount + 1);

        return (T) _returnValues.get(functionName).get(invocationCount);
    }
}
