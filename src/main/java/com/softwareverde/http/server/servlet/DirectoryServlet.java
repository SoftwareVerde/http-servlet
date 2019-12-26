package com.softwareverde.http.server.servlet;

import com.softwareverde.http.server.servlet.content.ContentTypeResolver;
import com.softwareverde.http.server.servlet.request.Request;
import com.softwareverde.http.server.servlet.response.Response;
import com.softwareverde.util.IoUtil;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class DirectoryServlet implements Servlet {
    public interface ErrorHandler {
        Response onFileNotFound(Request request);
    }

    protected Response _serveFilePath(final String filePath, final Request request) {
        final boolean shouldUseIndexFile = ( (_indexFile != null) && ((filePath.isEmpty()) || (filePath.charAt(filePath.length() - 1) == '/')) );

        final String fileName;
        {
            if (_serveRecursive) {
                fileName = filePath + (shouldUseIndexFile ? _indexFile : "");
            }
            else {
                final StringBuilder pathBuilder = new StringBuilder();
                try {
                    pathBuilder.append("/");
                    pathBuilder.append(
                        Paths.get(
                            new URI(filePath)
                                .getPath()
                        ).getFileName()
                    );
                    pathBuilder.append(shouldUseIndexFile ? _indexFile : "");
                }
                catch (final URISyntaxException exception) {
                    pathBuilder.setLength(0); // Clear the pathBuilder...
                    pathBuilder.append(filePath);
                }
                fileName = pathBuilder.toString();
            }
        }

        if (_servedFiles.containsKey(fileName)) {
            final File servedFile = _servedFiles.get(fileName);

            if (servedFile.isFile()) {
                final Response response = new Response();

                final String extension = _parseExtension(servedFile.getName());
                if (_contentTypeResolver.isKnownExtension(extension)) {
                    final String contentType = _contentTypeResolver.getContentType(extension);
                    response.addHeader(Response.Headers.CONTENT_TYPE, contentType);
                }

                response.setCode(Response.Codes.OK);
                response.setContent(IoUtil.getFileContents(servedFile));

                return response;
            }
        }

        final ErrorHandler errorHandler = _errorHandler;
        if (errorHandler != null) {
            return errorHandler.onFileNotFound(request);
        }

        final Response response = new Response();
        response.setCode(Response.Codes.NOT_FOUND);
        response.setContent("Not found.");
        return response;
    }

    private final File _rootDirectory;
    private Map<String, File> _servedFiles = null;
    private Boolean _serveRecursive = false;
    private String _indexFile = null;
    private ContentTypeResolver _contentTypeResolver = new ContentTypeResolver();
    private ErrorHandler _errorHandler;

    private void _indexServedFiles(final File directory, final Map<String, File> servedFiles) {
        final File[] directoryFiles = directory.listFiles();
        if (directoryFiles != null) {
            for (final File file : directoryFiles) {
                if (file.isFile()) {
                    final String fileUri = file.getPath().substring(_rootDirectory.getPath().length());

                    final String fileUriWithForwardSlashes;
                    { // Windows uses backslashes for separators, which will not match any web uri. Therefore, they are translated here.
                        if (File.separator.equals("\\")) {
                            fileUriWithForwardSlashes = fileUri.replace("\\", "/");
                        }
                        else {
                            fileUriWithForwardSlashes = fileUri;
                        }
                    }

                    servedFiles.put(fileUriWithForwardSlashes, file);
                }
                else if (_serveRecursive && file.isDirectory()) {
                    _indexServedFiles(file, servedFiles);
                }
            }
        }
    }

    private void _reIndexFiles() {
        _servedFiles = new HashMap<String, File>();
        _indexServedFiles(_rootDirectory, _servedFiles);
    }

    private String _parseExtension(final String filename) {
        if (! filename.contains(".")) { return ""; }
        return filename.substring(filename.lastIndexOf("."), filename.length());
    }

    public DirectoryServlet(final File directory) {
        _rootDirectory = directory;

        _reIndexFiles();
    }

    public void setShouldServeDirectories(final Boolean shouldServeDirectories) {
        _serveRecursive = shouldServeDirectories;
        _reIndexFiles();
    }

    public void setIndexFile(final String indexFile) {
        _indexFile = indexFile;
    }

    public void setErrorHandler(final ErrorHandler errorHandler) {
        _errorHandler = errorHandler;
    }

    @Override
    public Response onRequest(final Request request) {
        return _serveFilePath(request.getFilePath(), request);
    }

    public Response serveFile(final String filePath, final Request originalRequest) {
        return _serveFilePath(filePath, originalRequest);
    }
}
