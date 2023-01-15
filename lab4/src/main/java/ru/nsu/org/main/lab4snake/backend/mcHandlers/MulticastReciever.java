package ru.nsu.org.main.lab4snake.backend.mcHandlers;

import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import ru.nsu.org.main.lab4snake.backend.protoClass.SnakesProto;
import lombok.Getter;
import ru.nsu.org.main.lab4snake.logger.MyLogger;

import java.io.IOException;
import java.net.*;

public class MulticastReciever {
    private final static int SIZE = 8192;
    private final static int TIMEOUT_MS = 2000;

    private final MenuHandler handler;
    private final int mcPort;
    private final String mcAddr;

    @Getter
    private MulticastSocket socket;
    private final byte[] buf = new byte[SIZE];

    public MulticastReciever(MenuHandler handler, int mcPort, String mcAddr) {
        this.handler = handler;
        this.mcPort = mcPort;
        this.mcAddr = mcAddr;
    }

    public void init() {
        try {
            this.socket = new MulticastSocket(mcPort);
            socket.setSoTimeout(TIMEOUT_MS);
            InetAddress group = InetAddress.getByName(mcAddr);
            socket.joinGroup(group);
        } catch (IOException e) {
            MyLogger.getLogger().error(ExceptionUtils.getStackTrace(e));
        }
    }

    public void run() {
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        try {
            socket.receive(packet);
        } catch (SocketTimeoutException ignore) {
            handler.checkAliveServers();
            return;
        } catch (IOException unknownHostException) {
            unknownHostException.printStackTrace();
        }

        byte[] receivedBytes = new byte[packet.getLength()];
        System.arraycopy(buf, 0, receivedBytes, 0, packet.getLength());
        try {
            handler.changeListAvailableServer(SnakesProto.GameMessage.parseFrom(receivedBytes).getAnnouncement(), packet.getPort(), packet.getAddress());
            handler.checkAliveServers();
        } catch (InvalidProtocolBufferException e) {
            MyLogger.getLogger().error(ExceptionUtils.getStackTrace(e));
        }
    }

    public void close() {
        try {
            InetAddress group = InetAddress.getByName(mcAddr);
            socket.leaveGroup(group);
            socket.close();
        } catch (IOException e) {
            socket.close();
            MyLogger.getLogger().error(ExceptionUtils.getStackTrace(e));
        }
    }
}
