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
    private final File _rootDirectory;
    private Map<String, File> _servedFiles = null;
    private Boolean _serveRecursive = false;
    private String _indexFile = null;
    private ContentTypeResolver _contentTypeResolver = new ContentTypeResolver();

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

    @Override
    public Response onRequest(final Request request) {
        final String filePath = request.getFilePath();

        final Boolean shouldUseIndexFile = ( (_indexFile != null) && ((filePath.isEmpty()) || (filePath.charAt(filePath.length() - 1) == '/')) );

        final String fileName;
        {
            if (_serveRecursive) {
                fileName = filePath + (shouldUseIndexFile ? _indexFile : "");
            }
            else {
                String tmp = filePath;
                try {
                    tmp = "/"+ Paths.get(new URI(filePath).getPath()).getFileName().toString() + (shouldUseIndexFile ? _indexFile : "");
                }
                catch (final URISyntaxException e) { }
                fileName = tmp;
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

        final Response response = new Response();
        response.setCode(Response.Codes.NOT_FOUND);
        response.setContent("Not found.");
        return response;
    }
}
