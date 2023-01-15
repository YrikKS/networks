package ru.nsu.org.main.lab4snake.backend.node;

import ru.nsu.org.main.lab4snake.backend.protoClass.SnakesProto;
import ru.nsu.org.main.lab4snake.backend.protocol.SocketWrap;
import ru.nsu.org.main.lab4snake.model.Player;
import ru.nsu.org.main.lab4snake.model.Snake;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.ConcurrentHashMap;

public class ServerInfo {

    @Getter@Setter
    private Player newPlayer;
    @Getter@Setter
    private SocketWrap newSocket;
    @Getter@Setter
    private ConcurrentHashMap<Integer, Player> players;

    @Getter@Setter
    private ConcurrentHashMap <Integer, Snake> snakes;

    @Getter@Setter
    private ConcurrentHashMap<Long, SnakesProto.GameMessage> sentMessages;
    @Getter@Setter
    private SnakesProto.GameConfig config;
    @Getter@Setter
    private int currId;

    public ServerInfo(SnakesProto.GameConfig config, ConcurrentHashMap<Integer, Player> players,
                      ConcurrentHashMap<Integer, Snake> snakes, Player newPlayer) {
        this.config = config;
        this.players = players;
        this.currId = 0;
        this.snakes = snakes;
        this.newPlayer = newPlayer;
        this.newSocket = null;
    }
}
