package ru.nsu.org.main.lab4snake.backend.protocol;

import org.apache.commons.lang3.exception.ExceptionUtils;
import ru.nsu.org.main.lab4snake.logger.MyLogger;
import ru.nsu.org.main.lab4snake.backend.protoClass.SnakesProto;
import lombok.Getter;

import java.io.IOException;
import java.net.*;

public class SocketWrap implements IOWrap {
    private static final int MAX_SIZE = 8192;
    @Getter
    private final DatagramSocket socket;

    public SocketWrap(DatagramSocket socket) throws SocketException {
        this.socket = socket;
    }

    @Override
    public void send(SnakesProto.GameMessage message, String receiver, int receiverPort) {
        try {
            InetAddress addr = InetAddress.getByName(receiver);
            var packet = new DatagramPacket(message.toByteArray(), message.getSerializedSize(), addr, receiverPort);
            if (message.getTypeCase() != SnakesProto.GameMessage.TypeCase.ANNOUNCEMENT) {
                MyLogger.getLogger().info("Send to " + addr + ", " + receiverPort + " " + message.getTypeCase());
            }
            socket.send(packet);
        } catch (IOException e) {
            MyLogger.getLogger().error(ExceptionUtils.getStackTrace(e));
        }
    }

    @Override
    public GameMessageWrap receive() {
        if (socket == null) {
            MyLogger.getLogger().info("SOCKET IS NULL!!!");
            return null;
        }
        try {
            byte[] receiveRowByte = new byte[MAX_SIZE];
            DatagramPacket packet = new DatagramPacket(receiveRowByte, receiveRowByte.length);
            MyLogger.getLogger().info(String.format("Trying receive message... on port=%d, addr=%s", socket.getLocalPort(), socket.getLocalAddress()));
            socket.receive(packet);
            MyLogger.getLogger().info(String.format("Receive message... on port=%d, addr=%s", socket.getLocalPort(), socket.getLocalAddress()));

            byte[] receivedBytes = new byte[packet.getLength()];
            System.arraycopy(receiveRowByte, 0, receivedBytes, 0, packet.getLength());
            var message = SnakesProto.GameMessage.parseFrom(receivedBytes);
            MyLogger.getLogger().info("Get message from " + packet.getAddress() + " " + packet.getPort() + " " + message.getTypeCase());
            return new GameMessageWrap(message, packet.getAddress().getHostAddress(), packet.getPort());
        } catch (SocketTimeoutException e) {
            MyLogger.getLogger().info("Socket timeout");
        } catch (IOException e) {
            MyLogger.getLogger().error(ExceptionUtils.getStackTrace(e));
        }
        return null;
    }
}
