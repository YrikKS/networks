package ru.nsu.org.main.lab4snake.model;

import ru.nsu.org.main.lab4snake.backend.protoClass.SnakesProto;
import lombok.Getter;
import lombok.Setter;

public class Player {

    @Getter@Setter
    private String name;
    @Getter@Setter
    private int score;
    @Getter@Setter
    private int id;
    @Getter@Setter
    private String ipAddr;
    @Getter@Setter
    private int port;
    @Getter@Setter
    private SnakesProto.NodeRole role;

    public Player(SnakesProto.GamePlayer player) {
        this.name = player.getName();
        this.score = player.getScore();
        this.id = player.getId();
        this.ipAddr = player.getIpAddress();
        this.port = player.getPort();
        this.role = player.getRole();
    }

    public Player(String name, int score, int id, int port, String ipAddr, SnakesProto.NodeRole role) {
        this.name = name;
        this.score = score;
        this.id = id;
        this.ipAddr = ipAddr;
        this.port = port;
        this.role = role;
    }

    public Player(String name, int id, int port, String ipAddr, SnakesProto.NodeRole role) {
        this.name = name;
        this.score = 0;
        this.id = id;
        this.ipAddr = ipAddr;
        this.port = port;
        this.role = role;
    }

    public void incrementScore() {
        score = score + 1;
    }

    public SnakesProto.GamePlayer convertToProto() {
        return SnakesProto.GamePlayer.newBuilder()
                .setName(name)
                .setId(id)
                .setIpAddress(ipAddr)
                .setPort(port)
                .setRole(role)
                .setScore(score).build();
    }
}
