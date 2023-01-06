package ru.nsu.lab5;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerConnection extends Connection {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientConnection.class.getName());
    private static final int BUFFER_SIZE = 4096;
    private final SocketChannel channel;
    private final SelectionKey clientKey;
    private final SelectionKey key;
    private final ByteBuffer readBuffer = ByteBuffer.allocate(BUFFER_SIZE);
    private ByteBuffer writeBuffer;

    private ServerConnection(SelectionKey clientKey, SocketAddress address) throws IOException {
        this.clientKey = clientKey;
        channel = SocketChannel.open();
        channel.configureBlocking(false);
        key = channel.register(clientKey.selector(), SelectionKey.OP_CONNECT, this);
        channel.connect(address);
        LOGGER.info("Start connect server: " + address.toString());
    }

    public static ServerConnection createServerHandler(SelectionKey clientKey, SocketAddress address) throws IOException {
        return new ServerConnection(clientKey, address);
    }

    @Override
    public void linkBuffer(ByteBuffer clientBuffer) {
        writeBuffer = clientBuffer;
        ((ClientConnection) clientKey.attachment()).linkBuffer(readBuffer);
    }

    @Override
    public void handleEvent() {
        try {
            if (key.isReadable()) readRequest();
            else if (key.isWritable()) writeResponse();
            else if (key.isConnectable()) connect();
        } catch (Exception e) {
            close();
        }
    }

    @Override
    public void close() {
        try {
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readRequest() throws IOException {
        int counted = channel.read(readBuffer);
        if (counted == -1) setDisconnect();
        readBuffer.flip();
        key.interestOpsAnd(~SelectionKey.OP_READ);
        clientKey.interestOpsOr(SelectionKey.OP_WRITE);
        LOGGER.info("Read server message " + channel.getLocalAddress());
    }

    private void writeResponse() throws IOException {
        channel.write(writeBuffer);
        if (writeBuffer.remaining() != 0) return;
        if (((ClientConnection) clientKey.attachment()).getDisconnect()) {
            LOGGER.info("Server shutdownOutput " + channel.getLocalAddress());
            channel.shutdownOutput();
            clientKey.interestOpsAnd(~SelectionKey.OP_READ);
            if (getDisconnect()) {
                LOGGER.info("Close connection " + channel.getLocalAddress());
                close();
                ((ClientConnection) clientKey.attachment()).close();
                return;
            }
        } else {
            clientKey.interestOpsOr(SelectionKey.OP_READ);
            LOGGER.info("Write server message " + channel.getLocalAddress());
        }
        writeBuffer.clear();
        key.interestOpsAnd(~SelectionKey.OP_WRITE);
    }

    private void connect() {
        if (!channel.isConnectionPending()) return;
        try {
            if (!channel.finishConnect()) return;
            ((ClientConnection) clientKey.attachment()).setServer(key);
            ((ClientConnection) clientKey.attachment()).sendConnectResponse(true);
            key.interestOps(0);
            LOGGER.info("Connect server success: " + channel.getRemoteAddress());
        } catch (IOException e) {
            ((ClientConnection) clientKey.attachment()).sendConnectResponse(false);
            close();
        }
    }
}
