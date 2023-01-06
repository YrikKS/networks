package ru.nsu.lab5;

import org.xbill.DNS.Record;
import org.xbill.DNS.*;


import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DomainHandler implements Handler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientConnection.class.getName());
    private final DatagramChannel channel;
    private final InetSocketAddress socketAddress;
    private final ByteBuffer readBuff = ByteBuffer.allocate(Message.MAXLENGTH);
    private final ByteBuffer writeBuff = ByteBuffer.allocate(Message.MAXLENGTH);
    private final SelectionKey key;
    private final Deque<Message> deque = new LinkedList<>();
    private final Map<Integer, Map.Entry<SelectionKey, Short>> mapClients = new HashMap<>();

    public DomainHandler(int port, Selector selector) throws IOException {
        channel = DatagramChannel.open();
        channel.configureBlocking(false);
        key = channel.register(selector, 0, this);
        channel.bind(new InetSocketAddress(port));
        socketAddress = ResolverConfig.getCurrentConfig().server();
        channel.connect(socketAddress);
    }

    public void sendRequest(String domainName, Short port, SelectionKey clientKey) {
        try {
            Message dnsRequest = Message.newQuery(Record.newRecord(new Name(domainName + '.'), Type.A, DClass.IN));
            deque.addLast(dnsRequest);
            mapClients.put(dnsRequest.getHeader().getID(), Map.entry(clientKey, port));
            key.interestOpsOr(SelectionKey.OP_WRITE);
            LOGGER.info("Search address to domain: " + domainName);
        } catch (TextParseException e) {
            e.printStackTrace();
            ((ClientConnection) clientKey.attachment()).sendConnectResponse(false);
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

    @Override
    public void handleEvent() {
        try {
            if (key.isReadable()) read(key);
            else if (key.isWritable()) write(key);
        } catch (IOException e) {
            e.printStackTrace();
            close();
        }
    }

    public void read(SelectionKey key) throws IOException {
        if (channel.receive(readBuff) != null) {
            readBuff.flip();
            byte[] data = new byte[readBuff.limit()];
            readBuff.get(data);
            readBuff.clear();
            Message response = new Message(data);
            var session = mapClients.remove(response.getHeader().getID());
            Optional<Record> maybe = response.getSection(Section.ANSWER).stream().findAny();
            if (maybe.isPresent()) {
                LOGGER.info("Found address to domain: " + maybe.get().rdataToString());
                ServerConnection.createServerHandler(session.getKey(),
                        new InetSocketAddress(InetAddress.getByName(maybe.get().rdataToString()),
                                session.getValue()));
            } else {
                ((ClientConnection) session.getKey().attachment()).sendConnectResponse(false);
            }
        }
        if (mapClients.isEmpty()) key.interestOpsAnd(~SelectionKey.OP_READ);
    }

    public void write(SelectionKey key) throws IOException {
        Message dnsRequest = deque.pollFirst();
        while (dnsRequest != null) {
            writeBuff.clear();
            writeBuff.put(dnsRequest.toWire());
            writeBuff.flip();
            if (channel.send(writeBuff, socketAddress) == 0) {
                deque.addFirst(dnsRequest);
                break;
            }
            key.interestOpsOr(SelectionKey.OP_READ);
            dnsRequest = deque.pollFirst();
        }
        key.interestOpsAnd(~SelectionKey.OP_WRITE);
    }
}

