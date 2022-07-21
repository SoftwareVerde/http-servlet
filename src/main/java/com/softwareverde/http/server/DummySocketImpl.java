package com.softwareverde.http.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketImpl;


public class DummySocketImpl extends SocketImpl{
    final InputStream _inputStream;
    final OutputStream _outputStream;

    public DummySocketImpl(InputStream inputStream, OutputStream outputStream){
        _inputStream = inputStream;
        _outputStream = outputStream;
    }

    @Override
    public void setOption(int optID, Object value) throws SocketException {
    }

    @Override
    public Object getOption(int optID) throws SocketException {
        return null;
    }

    @Override
    protected void create(boolean stream) throws IOException {
    }

    @Override
    protected void connect(String host, int port) throws IOException {
    }

    @Override
    protected void connect(InetAddress address, int port) throws IOException {
    }

    @Override
    protected void connect(SocketAddress address, int timeout) throws IOException {
    }

    @Override
    protected void bind(InetAddress host, int port) throws IOException {
        throw new RuntimeException();
    }

    @Override
    protected void listen(int backlog) throws IOException {
        throw new RuntimeException();
    }

    @Override
    protected void accept(SocketImpl s) throws IOException {
        throw new RuntimeException();
    }

    @Override
    protected InputStream getInputStream() throws IOException {
        return _inputStream;
    }

    @Override
    protected OutputStream getOutputStream() throws IOException {
        return _outputStream;
    }

    @Override
    protected int available() throws IOException {
        return _inputStream.available();
    }

    @Override
    protected void close() throws IOException {
        _inputStream.close();
        _outputStream.close();
    }

    @Override
    protected void sendUrgentData(int data) throws IOException {
    }

    
}
