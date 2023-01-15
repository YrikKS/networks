package ru.nsu.org.main.lab4snake.backend.node;

import org.apache.commons.lang3.exception.ExceptionUtils;
import ru.nsu.org.main.lab4snake.logger.MyLogger;
import ru.nsu.org.main.lab4snake.backend.protoClass.SnakesProto;
import ru.nsu.org.main.lab4snake.backend.protoClass.SnakesProto.GameMessage.TypeCase.*;
import ru.nsu.org.main.lab4snake.backend.protocol.SocketWrap;
import ru.nsu.org.main.lab4snake.model.GameModel;
import ru.nsu.org.main.lab4snake.model.Player;
import ru.nsu.org.main.lab4snake.view.IView;
import ru.nsu.org.main.lab4snake.view.StateSystem;
import javafx.application.Platform;
import lombok.Getter;
import lombok.Setter;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.*;

public class NetNode implements INetHandler {
    public static final int TIMEOUT_MS = 1000;

    private final ExecutorService communicationThreadPool = Executors.newCachedThreadPool();

    private final ConcurrentHashMap<Long, SnakesProto.GameMessage> sentMessages = new ConcurrentHashMap<>();
    private final SnakesProto.GameConfig config;

    private ServerInfo serverInfo;
    private Player me;
    @Setter
    private IView gameView;
    @Getter
    private long seqNum;
    private SocketWrap socket;

    private final GameModel model;
    private StateSystem state = StateSystem.JOIN_GAME;
    private int serverPort;
    private String serverAddr;

    private Long lastTimeReceiveMessage = (long) 0;
    private Long lastTimeSendMessage = (long) 0;

    public NetNode(IView gameView, SnakesProto.GameConfig config,
                   String serverAddr, int serverPort, String name, GameModel model) {
        this.config = config;
        this.seqNum = 0;
        this.serverAddr = serverAddr;
        this.serverPort = serverPort;
        this.gameView = gameView;
        this.me = new Player(name, -1, 0, "", SnakesProto.NodeRole.NORMAL);
        this.model = model;
        this.serverInfo = new ServerInfo(null, null, null, me);
    }

    public synchronized void incrementSeqNum() {
        ++seqNum;
    }

    @Override
    public void sender(Player player, SnakesProto.GameMessage message) {
        if (socket == null) {
            MyLogger.getLogger().error("SOCKET IS NULL!!!");
            return;
        }
        lastTimeSendMessage = System.currentTimeMillis();
        switch (message.getTypeCase()) {
            case ACK -> sendAckMessage(message);
            case JOIN -> sendJoinMessage(message);
            case PING -> sendPingMessage(message);
            case STEER -> sendSteerMessage(message);
        }
    }

    private void sendAckMessage(SnakesProto.GameMessage message) {
        socket.send(message, serverAddr, serverPort);
    }

    private void sendJoinMessage(SnakesProto.GameMessage message) {
        sentMessages.put(message.getMsgSeq(), message);
        while (sentMessages.containsKey(message.getMsgSeq())) {
            socket.send(message, serverAddr, serverPort);
            try {
                Thread.sleep(TIMEOUT_MS);
            } catch (InterruptedException ignored) {
            }
        }
    }

    private void sendSteerMessage(SnakesProto.GameMessage message) {
        sentMessages.put(message.getMsgSeq(), message);
        while (sentMessages.containsKey(message.getMsgSeq())) {
            socket.send(message, serverAddr, serverPort);
            try {
                Thread.sleep(config.getPingDelayMs());
            } catch (InterruptedException ignored) {
            }
        }
    }

    private void sendPingMessage(SnakesProto.GameMessage message) {
        sentMessages.put(message.getMsgSeq(), message);
        socket.send(message, serverAddr, serverPort);
    }

    @Override
    public SnakesProto.GameMessage getSteerMessage(SnakesProto.Direction direction) {
        var steerMessage = SnakesProto.GameMessage.SteerMsg.newBuilder()
                .setDirection(direction)
                .build();
        var gameMessage = SnakesProto.GameMessage.newBuilder()
                .setSteer(steerMessage)
                .setSenderId(me.getId())
                .setMsgSeq(seqNum)
                .build();
        incrementSeqNum();
        return gameMessage;
    }

    @Override
    public void receiver() {
        if (socket == null) {
            MyLogger.getLogger().error("SOCKET IS NULL!!!");
            return;
        }
        var receivedMessage = socket.receive();
        lastTimeReceiveMessage = System.currentTimeMillis();
        long seqNumRecv = receivedMessage.getMessage().getMsgSeq();
//        System.out.println(SnakesProto.GameMessage.TypeCase.ACK);
        switch (receivedMessage.getMessage().getTypeCase()) {
            case ACK -> {
                var sentMessage = sentMessages.get(seqNumRecv);
                switch (sentMessage.getTypeCase()) {
                    case STEER -> {
                        //remove all steer messages, if seqNumRecv steer bigger then they
                        for (var messageSeqNum : sentMessages.keySet()) {
                            if (messageSeqNum <= seqNumRecv && sentMessages.get(messageSeqNum).getTypeCase() == SnakesProto.GameMessage.TypeCase.STEER) {
                                sentMessages.remove(messageSeqNum);
                            }
                        }
                    }
                    case JOIN -> {
                        sentMessages.remove(seqNumRecv);
                        me.setId(receivedMessage.getMessage().getReceiverId());
                    }
                    case PING -> sentMessages.remove(seqNumRecv);
                }
            }
            case PING -> {
                sender(null, getAckMessage(receivedMessage.getMessage().getMsgSeq(), me.getId()));
            }
            case ERROR -> {
                sentMessages.remove(receivedMessage.getMessage().getMsgSeq());
                Platform.runLater(() -> gameView.render(StateSystem.ERROR_LOAD_GAME, receivedMessage.getMessage().getError().getErrorMessage()));
            }
            case STATE -> {
                var stateMessage = receivedMessage.getMessage().getState();
                sender(null, getAckMessage(receivedMessage.getMessage().getMsgSeq(), receivedMessage.getMessage().getReceiverId()));
                if (model.getConfig() == null) {
                    model.addConfig(stateMessage.getState().getConfig());
                }
                if (stateMessage.getState().getStateOrder() > model.getStateOrder()) {
                    model.updateClientModel(receivedMessage.getMessage().getState().getState());
                    updateServerInfo();
                }
            }
            case ROLE_CHANGE -> {
                var roleChangeMessage = receivedMessage.getMessage().getRoleChange();
                me.setRole(roleChangeMessage.getReceiverRole());
                sender(null, getAckMessage(receivedMessage.getMessage().getMsgSeq(), receivedMessage.getMessage().getReceiverId()));
            }
        }


    }

    private void updateServerInfo() {
        serverInfo.setConfig(model.getConfig());
        serverInfo.setSnakes(model.getSnakes());
        serverInfo.setPlayers(model.getPlayers());
    }

    @Override
    public void openSocket() {
        try {
            socket = new SocketWrap(new DatagramSocket());
            socket.getSocket().setSoTimeout(TIMEOUT_MS);
        } catch (SocketException e) {
            MyLogger.getLogger().error(ExceptionUtils.getStackTrace(e));
        }
    }

    @Override
    public void startReceiver() {
        communicationThreadPool.submit(() -> {
            while (state == StateSystem.JOIN_GAME) {
                receiver();
            }
            socket.getSocket().close();
        });
    }

    private void sendFirstJoin() {
        communicationThreadPool.submit(() -> sender(null, getJoinMessage(me.getName())));
    }

    @Override
    public void start() {
        startReceiver();
        me.setPort(socket.getSocket().getLocalPort());
        serverInfo.setNewSocket(socket);
        if (me.getRole() == SnakesProto.NodeRole.NORMAL) {
            sendFirstJoin();
        }
    }

    @Override
    public void end() {
        state = StateSystem.MENU;
        if (socket == null) {
            MyLogger.getLogger().error("SOCKET IS NULL!!!");
            return;
        }
        communicationThreadPool.shutdown();
    }

    public SnakesProto.GameMessage getJoinMessage(String name) {
        var joinMsg = SnakesProto.GameMessage.JoinMsg.newBuilder()
                .setName(name)
                .build();
        var sendMessage = SnakesProto.GameMessage.newBuilder()
                .setJoin(joinMsg)
                .setMsgSeq(seqNum)
                .build();
        incrementSeqNum();
        return sendMessage;
    }

    private SnakesProto.GameMessage getAckMessage(long msgSeq, int receiverId) {
        var ackMessage = SnakesProto.GameMessage.AckMsg.newBuilder().build();
        return SnakesProto.GameMessage.newBuilder()
                .setAck(ackMessage)
                .setMsgSeq(msgSeq)
                .setSenderId(me.getId())
                .setReceiverId(receiverId)
                .build();
    }
}
