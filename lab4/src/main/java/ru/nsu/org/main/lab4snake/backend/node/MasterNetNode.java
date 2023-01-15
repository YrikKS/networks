package ru.nsu.org.main.lab4snake.backend.node;

import org.apache.commons.lang3.exception.ExceptionUtils;
import ru.nsu.org.main.lab4snake.logger.MyLogger;
import ru.nsu.org.main.lab4snake.backend.mcHandlers.MulticastSender;
import ru.nsu.org.main.lab4snake.backend.protoClass.SnakesProto;
import ru.nsu.org.main.lab4snake.backend.protocol.GameMessageWrap;
import ru.nsu.org.main.lab4snake.backend.protocol.SocketWrap;
import ru.nsu.org.main.lab4snake.model.CustomGameConfig;
import ru.nsu.org.main.lab4snake.model.GameModel;
import ru.nsu.org.main.lab4snake.model.Player;
import ru.nsu.org.main.lab4snake.view.StateSystem;
import lombok.Getter;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.concurrent.*;

public class MasterNetNode implements INetHandler {

    private final ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(2);
    private final ExecutorService communicateThreadPool = Executors.newCachedThreadPool();

    @Getter
    private final ConcurrentHashMap<Integer, Player> players;
    private final ConcurrentHashMap<Long, SnakesProto.GameMessage> sentMessages;
    private final ConcurrentHashMap<Integer, Long> lastReceivedSteer;
    private final ConcurrentHashMap<Integer, Long> lastMessageFromPlayer;
    private final ConcurrentHashMap<Integer, Long> lastSendMessagePlayer;
    private Player currDeputy;

    private final GameModel model;
    private final SnakesProto.GameConfig config;

    private final Player master;
    private SocketWrap socket;
    private int currId;
    @Getter
    private long seqNum;
    private boolean isError;

    private StateSystem state = StateSystem.NEW_GAME;

    public MasterNetNode(CustomGameConfig config, GameModel model) {
        this.config = config.convertToProto();
        this.seqNum = 0;
        this.model = model;
        this.currDeputy = null;
        this.master = new Player(config.getLogin(), 0, 0, "127.0.0.1", SnakesProto.NodeRole.MASTER);
        this.model.addNewPlayer(master);
        this.currId = 0;
        this.players = model.getPlayers();
        this.isError = false;
        this.sentMessages = new ConcurrentHashMap<>();
        this.lastReceivedSteer = new ConcurrentHashMap<>();
        this.lastMessageFromPlayer = new ConcurrentHashMap<>();
        this.lastSendMessagePlayer = new ConcurrentHashMap<>();
    }

    private synchronized void incrementSeqNum() {
        ++seqNum;
    }

    @Override
    public void sender(Player player, SnakesProto.GameMessage message) {
        if (socket == null) {
            MyLogger.getLogger().error("SOCKET IS NULL!!!");
            return;
        }
        if (message.getTypeCase() != SnakesProto.GameMessage.TypeCase.ANNOUNCEMENT) {
            if (player != null) {
                lastSendMessagePlayer.put(player.getId(), System.currentTimeMillis());
            }
        }
        switch (message.getTypeCase()) {
            case ACK, ERROR -> sendAckMessage(message, player.getIpAddr(), player.getPort());
            case PING -> {
                sendPingMessage(message, player.getIpAddr(), player.getPort());
            }
            case STEER -> {
                sendSteerMessage(message);
            }
            case STATE -> {
                if (player.getId() != master.getId()) {
                    sendStateMessage(message, player.getIpAddr(), player.getPort());
                }
            }
            case ROLE_CHANGE -> {
                sendRoleChangeMessage(message, player.getIpAddr(), player.getPort());
            }
            default -> {
                MyLogger.getLogger().info("Message of unknown type");
            }
        }
    }

    private void sendPingMessage(SnakesProto.GameMessage message, String receiverIpAddr, int receiverPort) {
        sentMessages.put(message.getMsgSeq(), message);
        socket.send(message, receiverIpAddr, receiverPort);
    }

    private void sendAckMessage(SnakesProto.GameMessage message, String receiverIpAddr, int receiverPort) {
        socket.send(message, receiverIpAddr, receiverPort);
    }

    private void sendSteerMessage(SnakesProto.GameMessage message) {
        sentMessages.put(message.getMsgSeq(), message);

        while (sentMessages.containsKey(message.getMsgSeq())) {
            socket.send(message, master.getIpAddr(), master.getPort());
            try {
                Thread.sleep(config.getPingDelayMs());
            } catch (InterruptedException ignored) {
            }
        }
    }

    private void sendPing() {
        for (var id : lastSendMessagePlayer.keySet()) {
            if (id != master.getId()) {
                var timePassed = System.currentTimeMillis() - lastSendMessagePlayer.get(id);
                if (timePassed > config.getPingDelayMs()) {
                    var pingMessage = getPingMessage(id);
                    sender(players.get(id), pingMessage);
                }
            }
        }
    }

    private SnakesProto.GameMessage getPingMessage(int playerId) {
        var pingMessage = SnakesProto.GameMessage.PingMsg.newBuilder()
                .build();
        var message = SnakesProto.GameMessage.newBuilder()
                .setPing(pingMessage)
                .setSenderId(master.getId())
                .setReceiverId(playerId)
                .setMsgSeq(seqNum)
                .build();
        incrementSeqNum();
        return message;
    }

    private void sendStateMessage(SnakesProto.GameMessage message, String receiverIpAddr, int receiverPort) {
        sentMessages.put(message.getMsgSeq(), message);
        while (sentMessages.containsKey(message.getMsgSeq())) {
            socket.send(message, receiverIpAddr, receiverPort);
            try {
                Thread.sleep(config.getPingDelayMs());
            } catch (InterruptedException ignored) {
            }
        }
    }

    @Override
    public void receiver() {
        if (socket == null) {
            MyLogger.getLogger().error("SOCKET IS NULL!!!");
            return;
        }
        var message = socket.receive();
        if (message == null) {
            return;
        }
        var seqNumRecv = message.getMessage().getMsgSeq();
        if (message.getMessage().getTypeCase() != SnakesProto.GameMessage.TypeCase.JOIN) {
            lastMessageFromPlayer.put(message.getMessage().getSenderId(), System.currentTimeMillis());
        }

        switch (message.getMessage().getTypeCase()) {
            case ACK -> {
                var receivedMessage = sentMessages.get(seqNumRecv);
                switch (receivedMessage.getTypeCase()) {
                    case STEER, STATE, ROLE_CHANGE, PING -> {
                        sentMessages.remove(seqNumRecv);
                    }
                    default -> MyLogger.getLogger().info("Ack of unknown message type");
                }
            }
            case JOIN -> receiveJoinMessage(message);
            case PING -> receivePingMessage(message);
            case STEER -> receiveSteerMessage(message);
            case ROLE_CHANGE -> {}
            default -> MyLogger.getLogger().info("Message of unknown type");
        }
    }

    private void receivePingMessage(GameMessageWrap message) {
        var pingMessage = message.getMessage();
        var currPlayer = players.get(pingMessage.getSenderId());
        sender(currPlayer, getAckMessage(message.getMessage().getMsgSeq(), currPlayer.getId()));
    }

    private void sendRoleChangeMessage(SnakesProto.GameMessage message, String receiverIp, int receiverPort) {
        sentMessages.put(message.getMsgSeq(), message);

        while (sentMessages.containsKey(message.getMsgSeq())) {
            socket.send(message, receiverIp, receiverPort);
            try {
                Thread.sleep(config.getPingDelayMs());
            } catch (InterruptedException ignored) {
            }
        }
    }

    private void choseNewDeputy() {
        for (var player : players.values()) {
            if (player.getId() != master.getId()) {
                communicateThreadPool.submit(() -> sender(player, getRoleChangeMessage(SnakesProto.NodeRole.DEPUTY, player.getId())));
                break;
            }
        }
    }

    private void receiveSteerMessage(GameMessageWrap message) {
        var steerMessage = message.getMessage();
        var currPlayer = players.get(steerMessage.getSenderId());
        if (players.get(message.getMessage().getSenderId()).getRole() != SnakesProto.NodeRole.VIEWER) {
            if (lastReceivedSteer.get(message.getMessage().getSenderId()) == null ||
                    lastReceivedSteer.get(message.getMessage().getSenderId()) < steerMessage.getMsgSeq()) {
                model.changeSnakesDirection(currPlayer, steerMessage.getSteer().getDirection());
                lastReceivedSteer.put(message.getMessage().getSenderId(), message.getMessage().getMsgSeq());
            }
        }
        sender(currPlayer, getAckMessage(message.getMessage().getMsgSeq(), currPlayer.getId()));
    }

    private void receiveJoinMessage(GameMessageWrap message) {
        var newPlayer = addNewPlayer(message.getMessage().getJoin().getName(),
                message.getSenderAddr(), message.getPort());
        //can add player
        if (!isError) {
            sender(newPlayer, getAckMessage(message.getMessage().getMsgSeq(), newPlayer.getId()));
            model.updateModel();
            //choose deputy
            if (currDeputy == null) {
                communicateThreadPool.submit(() -> sender(newPlayer, getRoleChangeMessage(SnakesProto.NodeRole.DEPUTY, newPlayer.getId())));
                currDeputy = newPlayer;
            }
        } else {
            sender(newPlayer, getErrorMessage(message.getMessage().getMsgSeq(), "SORRY, BUT NUMBER OF PLAYERS EXCEEDED. PLEASE CONNECT LATER...."));
        }
    }

    private void createMulticastSender() {
        var multicastSender = new MulticastSender(this, socket.getSocket(), MULTICAST_ADDR, MULTICAST_PORT);
        multicastSender.init();
        scheduledThreadPool.scheduleAtFixedRate(multicastSender::run, 1, 1, TimeUnit.SECONDS);
    }

    @Override
    public void openSocket() {
        try {
            this.socket = new SocketWrap(new DatagramSocket());
            socket.getSocket().setSoTimeout(500);
        } catch (SocketException e) {
            MyLogger.getLogger().error(ExceptionUtils.getStackTrace(e));
        }
    }

    @Override
    public void startReceiver() {
        communicateThreadPool.submit(() -> {
            while (state == StateSystem.NEW_GAME) {
                receiver();
            }
        });
    }

    private void startGame() {
        communicateThreadPool.submit(this::gameLoop);
    }

    private void checkConnection() {
        for (var player : lastMessageFromPlayer.keySet()) {
            if (player != master.getId()) {
                var timePassed = System.currentTimeMillis() - lastMessageFromPlayer.get(player);
                if (timePassed > config.getNodeTimeoutMs()) {
                    players.remove(player);
                    if (model.getSnakes().get(player) != null) {
                        model.getSnakes().get(player).setState(SnakesProto.GameState.Snake.SnakeState.ZOMBIE);
                    }
                    lastMessageFromPlayer.remove(player);
                    lastSendMessagePlayer.remove(player);
                    lastReceivedSteer.remove(player);
                    for (var message : sentMessages.keySet()) {
                        if (sentMessages.get(message).getReceiverId() == player) {
                            sentMessages.remove(message);
                        }
                    }
                    if (players.get(player) != null && players.get(player).getRole() == SnakesProto.NodeRole.DEPUTY) {
                        choseNewDeputy();
                    }
                }
            }
        }
    }

    private void gameLoop() {
        while (state == StateSystem.NEW_GAME) {
            model.oneTurnGame();
            checkConnection();
            communicateThreadPool.submit(() -> {
                for (var player : players.keySet()) {
                    var stateMessage = getGameStateMessage(player);
                    sender(players.get(player), stateMessage);
                }
            });
            try {
                Thread.sleep(config.getStateDelayMs());
            } catch (InterruptedException ignored) {
            }

        }
    }

    private void createSenderPing() {
        scheduledThreadPool.scheduleAtFixedRate(this::sendPing, config.getPingDelayMs(), config.getPingDelayMs(), TimeUnit.MILLISECONDS);
    }

    @Override
    public void start() {
        master.setPort(socket.getSocket().getLocalPort());
        createMulticastSender();
        startReceiver();
        startGame();
        createSenderPing();
    }

    @Override
    public synchronized void end() {
        state = StateSystem.EXIT;
        scheduledThreadPool.shutdown();
        communicateThreadPool.shutdown();
    }

    private SnakesProto.GameMessage getRoleChangeMessage(SnakesProto.NodeRole newRole, int receiverId) {
        var roleChangeMessage = SnakesProto.GameMessage.RoleChangeMsg.newBuilder()
                .setSenderRole(SnakesProto.NodeRole.MASTER)
                .setReceiverRole(newRole)
                .build();
        var message = SnakesProto.GameMessage.newBuilder()
                .setRoleChange(roleChangeMessage)
                .setMsgSeq(seqNum)
                .setSenderId(master.getId())
                .setReceiverId(receiverId)
                .build();
        incrementSeqNum();
        return message;
    }

    public SnakesProto.GameMessage createAnnouncementMessage() {
        ArrayList<SnakesProto.GamePlayer> listPlayers = new ArrayList<>();
        for (var player : model.getPlayers().values()) {
            listPlayers.add(player.convertToProto());
        }
        SnakesProto.GamePlayers clients = SnakesProto.GamePlayers.newBuilder().addAllPlayers(listPlayers).build();
        var announcementMessage = SnakesProto.GameMessage.AnnouncementMsg.newBuilder().
                setPlayers(clients)
                .setConfig(config)
                .build();
        var sendMessage = SnakesProto.GameMessage.newBuilder()
                .setAnnouncement(announcementMessage)
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
                .setSenderId(master.getId())
                .setReceiverId(receiverId)
                .build();
    }

    private SnakesProto.GameMessage getErrorMessage(long msgSeq, String error) {
        var errorMsg = SnakesProto.GameMessage.ErrorMsg.newBuilder().setErrorMessage(error).build();
        return SnakesProto.GameMessage.newBuilder()
                .setError(errorMsg)
                .setMsgSeq(msgSeq)
                .setSenderId(master.getId())
                .build();
    }

    @Override
    public SnakesProto.GameMessage getSteerMessage(SnakesProto.Direction direction) {
        var steerMessage = SnakesProto.GameMessage.SteerMsg.newBuilder()
                .setDirection(direction)
                .build();
        var gameMessage = SnakesProto.GameMessage.newBuilder()
                .setSteer(steerMessage)
                .setSenderId(master.getId())
                .setMsgSeq(seqNum)
                .build();
        incrementSeqNum();
        return gameMessage;
    }

    private Player addNewPlayer(String name, String ipAddress, int port) {
        var player = new Player(name, ++currId, port, ipAddress, SnakesProto.NodeRole.NORMAL);
        isError = !model.addNewPlayer(player);
        return player;
    }

    private SnakesProto.GameMessage getGameStateMessage(int playerId) {
        ArrayList<SnakesProto.GameState.Snake> protoSnakes = new ArrayList<>();
        ArrayList<SnakesProto.GameState.Coord> foodProto = new ArrayList<>();
        for (var snake : model.getSnakes().values()) {
            protoSnakes.add(snake.convertToProtoSnake());
        }
        for (var singleFood : model.getFood()) {
            var point = singleFood.getPlace();
            foodProto.add(point.convertPointToCoord());
        }
        var gamePLayers = SnakesProto.GamePlayers.newBuilder();
        for (var player : players.values()) {
            gamePLayers.addPlayers(players.get(player.getId()).convertToProto());
            if (!model.getSnakes().containsKey(player.getId())
                    && player.getId() != master.getId() && player.getRole() != SnakesProto.NodeRole.VIEWER) {
                players.get(player.getId()).setRole(SnakesProto.NodeRole.VIEWER);
                communicateThreadPool.submit(() -> sender(player, getRoleChangeMessage(SnakesProto.NodeRole.VIEWER, player.getId())));
            }
        }
        var updatedPlayers = gamePLayers.build();
        var gameState = SnakesProto.GameState.newBuilder()
                .setStateOrder(model.getStateOrder())
                .setConfig(config)
                .addAllSnakes(protoSnakes)
                .setPlayers(updatedPlayers)
                .addAllFoods(foodProto)
                .build();
        var stateMessage = SnakesProto.GameMessage.StateMsg.newBuilder()
                .setState(gameState)
                .build();
        var gameMessage = SnakesProto.GameMessage.newBuilder()
                .setState(stateMessage)
                .setMsgSeq(seqNum)
                .setSenderId(master.getId())
                .setReceiverId(playerId)
                .build();
        incrementSeqNum();
        return gameMessage;
    }
}
