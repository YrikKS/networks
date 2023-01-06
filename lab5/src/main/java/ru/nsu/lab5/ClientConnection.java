package ru.nsu.lab5;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientConnection extends Connection {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientConnection.class.getName());
    private static final int BUFFER_SIZE = 4096;
    private final SocketChannel clientChannel;
    private final DomainHandler domainHandler;
    private final SelectionKey key;
    private SelectionKey serverKey;
    private final ByteBuffer readBuffer = ByteBuffer.allocate(BUFFER_SIZE);
    public ByteBuffer writeBuffer;
    private OperationType type = OperationType.HELLO;
    private Request headerRequest;

    private ClientConnection(DomainHandler domainHandler, SelectionKey key) throws IOException {
        this.domainHandler = domainHandler;
        clientChannel = ((ServerSocketChannel) key.channel()).accept();
        clientChannel.configureBlocking(false);
        this.key = clientChannel.register(key.selector(), SelectionKey.OP_READ, this);
        LOGGER.info("Accept new client");
    }

    public static ClientConnection createClientHandler(DomainHandler domainHandler, SelectionKey key) throws IOException {
        return new ClientConnection(domainHandler, key);
    }

    @Override
    public void handleEvent() {
        try {
            if (key.isReadable()) read();
            else if (key.isWritable()) write();
        } catch (Exception e) {
            close();
        }
    }

    @Override
    public void close() {
        try {
            clientChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setServer(SelectionKey serverKey) {
        this.serverKey = serverKey;
    }

    @Override
    public void linkBuffer(ByteBuffer serverBuff) {
        writeBuffer = serverBuff;
    }

    public void sendConnectResponse(boolean connected) {
        writeBuffer = connected ? ByteBuffer.wrap(headerRequest.getResponse()) : ByteBuffer.wrap(headerRequest.getDisconnectResponse());
        key.interestOps(SelectionKey.OP_WRITE);
    }

    private void read() throws IOException {
        int counted = clientChannel.read(readBuffer);
        if (counted == -1) {
            if (serverKey == null) {
                close();
                return;
            }
            setDisconnect();
        }
        switch (type) {
            case HELLO -> readHello();
            case HEADER -> readHeader();
            case MESSAGE -> readRequest();
        }
    }

    private void readHello() throws IOException {
        Request request = new Request(readBuffer, type);
        if (!request.isRequest()) return;
        if (!request.checkVersion()) {
            close();
            return;
        }
        if (!request.isValid()) setDisconnect();
        key.interestOps(SelectionKey.OP_WRITE);
        writeBuffer = ByteBuffer.wrap(request.getResponse());
        readBuffer.clear();
        LOGGER.info("Read hello " + clientChannel.getLocalAddress());
    }

    private void readHeader() throws IOException {
        headerRequest = new Request(readBuffer, type);
        if (!headerRequest.isRequest()) return;
        if (!headerRequest.isValid()) {
            setDisconnect();
            writeBuffer = ByteBuffer.wrap(headerRequest.getResponse());
            key.interestOps(SelectionKey.OP_WRITE);
            return;
        }
        key.interestOps(0);
        switch (headerRequest.getAddressType()) {
            case 0x01 -> ServerConnection.createServerHandler(key,
                    new InetSocketAddress(InetAddress.getByAddress(headerRequest.getAddress()),
                            headerRequest.getPort()));
            case 0x03 -> domainHandler.sendRequest(new String(headerRequest.getAddress()),
                    headerRequest.getPort(), key);
        }
        readBuffer.clear();
        LOGGER.info("Read header " + clientChannel.getLocalAddress());
    }

    private void readRequest() throws IOException {
        readBuffer.flip();
        key.interestOpsAnd(~SelectionKey.OP_READ);
        serverKey.interestOpsOr(SelectionKey.OP_WRITE);
        LOGGER.info("Read client message " + clientChannel.getLocalAddress());
    }

    private void write() throws IOException {
        clientChannel.write(writeBuffer);
        if (writeBuffer.remaining() != 0) return;
        switch (type) {
            case HELLO -> writeHello();
            case HEADER -> writeHeader();
            case MESSAGE -> writeResponse();
        }
    }

    private void writeHello() throws IOException {
        if (getDisconnect()) {
            close();
            return;
        }
        key.interestOps(SelectionKey.OP_READ);
        type = OperationType.HEADER;
        LOGGER.info("Write hello " + clientChannel.getLocalAddress());
    }

    private void writeHeader() throws IOException {
        if (getDisconnect() || serverKey == null) {
            close();
            return;
        }
        readBuffer.clear();
        ((ServerConnection) serverKey.attachment()).linkBuffer(readBuffer);
        key.interestOps(SelectionKey.OP_READ);
        serverKey.interestOps(SelectionKey.OP_READ);
        type = OperationType.MESSAGE;
        LOGGER.info("Write header " + clientChannel.getLocalAddress());
    }

    private void writeResponse() throws IOException {
        if (((ServerConnection) serverKey.attachment()).getDisconnect()) {
            LOGGER.info("Client shutdownOutput " + clientChannel.getLocalAddress());
            clientChannel.shutdownOutput();
            serverKey.interestOpsAnd(~SelectionKey.OP_READ);
            if (getDisconnect()) {
                LOGGER.info("Close connection " + clientChannel.getLocalAddress());
                close();
                ((ServerConnection) serverKey.attachment()).close();
                return;
            }
        } else {
            serverKey.interestOpsOr(SelectionKey.OP_READ);
            LOGGER.info("Write client message " + clientChannel.getLocalAddress());
        }
        writeBuffer.clear();
        key.interestOpsAnd(~SelectionKey.OP_WRITE);
    }
}
