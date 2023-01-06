package ru.nsu.lab5;


import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Proxy {
    private static final Logger LOGGER = LoggerFactory.getLogger(Proxy.class.getName());
    private final Selector selector;
    private final ServerHandler server;

    private Proxy(int port) throws IOException {
        selector = SelectorProvider.provider().openSelector();
        server = new ServerHandler(selector, port);
    }

    public static void run(int port) throws IOException {
        Proxy proxy = new Proxy(port);
        LOGGER.info("Start proxy...");
        proxy.run();
    }

    private void run() {
        try {
            while (selector.select() > -1) {
                selector.selectedKeys().forEach(this::handleEvent);
                selector.selectedKeys().clear();
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            close();
        }
    }

    private void handleEvent(SelectionKey key) {
        if (key.isValid()) ((Handler) key.attachment()).handleEvent();
    }

    private void close() {
        try {
            LOGGER.info("Close proxy");
            server.close();
            selector.close();
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
        }
    }
}
