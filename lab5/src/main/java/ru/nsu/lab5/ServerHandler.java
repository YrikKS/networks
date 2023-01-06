package ru.nsu.lab5;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerHandler implements Handler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerHandler.class.getName());
    private final ServerSocketChannel channel;
    private final SelectionKey key;
    private final DomainHandler domainHandler;

    public ServerHandler(Selector selector, int port) throws IOException {
        domainHandler = new DomainHandler(port, selector);
        channel = ServerSocketChannel.open();
        channel.bind(new InetSocketAddress(port));
        channel.configureBlocking(false);
        key = channel.register(selector, SelectionKey.OP_ACCEPT, this);
        LOGGER.info("Create server");
    }

    @Override
    public void handleEvent() {
        try {
            ClientConnection.createClientHandler(domainHandler, key);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            close();
        }
    }

    @Override
    public void close() {
        try {
            domainHandler.close();
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
