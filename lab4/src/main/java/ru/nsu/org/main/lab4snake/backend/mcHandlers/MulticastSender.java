package ru.nsu.org.main.lab4snake.backend.mcHandlers;

import org.apache.commons.lang3.exception.ExceptionUtils;
import ru.nsu.org.main.lab4snake.backend.node.MasterNetNode;
import ru.nsu.org.main.lab4snake.backend.protocol.SocketWrap;
import lombok.Setter;
import ru.nsu.org.main.lab4snake.logger.MyLogger;

import java.net.DatagramSocket;
import java.net.SocketException;

public class MulticastSender {

    private final String mcAddr;
    private final int mcPort;
    private final DatagramSocket datagramSocket;
    @Setter
    private MasterNetNode master;
    private SocketWrap socket;

    public MulticastSender(MasterNetNode master, DatagramSocket socket, String mcAddr, int mcPort) {
        this.master = master;
        this.datagramSocket = socket;
        this.mcAddr = mcAddr;
        this.mcPort = mcPort;
    }

    public void init() {
        try {
            this.socket = new SocketWrap(datagramSocket);
        } catch (SocketException e) {
            MyLogger.getLogger().error(ExceptionUtils.getStackTrace(e));
        }
    }

    public void run() {
        var message = master.createAnnouncementMessage();
        socket.send(message, mcAddr, mcPort);
    }

}
