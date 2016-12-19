package com.softwareverde.example.server;

import com.softwareverde.httpserver.ApiServer;
import com.softwareverde.httpserver.DirectoryServlet;

import java.io.File;

public class Main {
    private static void _exitFailure() {
        System.exit(1);
    }

    private static void _printError(final String errorMessage) {
        System.err.println(errorMessage);
    }

    private static void _printUsage() {
        _printError("Usage: java -jar " + System.getProperty("java.class.path") + " <configuration-file>");
    }

    private static Configuration _loadConfigurationFile(final String configurationFilename) {
        final File configurationFile =  new File(configurationFilename);
        if (! configurationFile.isFile()) {
            _printError("Invalid configuration file.");
            _exitFailure();
        }

        return new Configuration(configurationFile);
    }

    public static void main(final String[] commandLineArguments) {
        if (commandLineArguments.length != 1) {
            _printUsage();
            _exitFailure();
        }

        final String configurationFilename = commandLineArguments[0];

        final Configuration configuration = _loadConfigurationFile(configurationFilename);

        System.out.println("[Starting Server]");

        final Configuration.ServerProperties serverProperties = configuration.getServerProperties();

        final ApiServer<Account> apiServer = new ApiServer<Account>();
        apiServer.setPort(serverProperties.getPort());

        apiServer.setTlsPort(serverProperties.getTlsPort());
        apiServer.setCertificate(serverProperties.getTlsCertificateFile(), serverProperties.getTlsKeyFile());
        apiServer.enableEncryption(true);
        apiServer.redirectToTls(true);

        { // Demo Api
            final ApiServer.Endpoint<Account> apiEndpoint = new DemoApi(apiServer);
            apiEndpoint.setStrictPathEnabled(true);
            apiServer.addEndpoint("/api/demo", apiEndpoint);
        }

        { // Static Content
            final File servedDirectory = new File(serverProperties.getRootDirectory() +"/");
            final DirectoryServlet<Account> indexEndpoint = new DirectoryServlet<Account>(apiServer, servedDirectory);
            indexEndpoint.setShouldServeDirectories(true);
            indexEndpoint.setIndexFile("index.html");
            apiServer.addEndpoint("/", indexEndpoint);
        }

        apiServer.start();

        System.out.println("[Server Online]");

        System.out.println();
        while (true) {
            try { Thread.sleep(500); } catch (final Exception e) { }
        }
    }
}