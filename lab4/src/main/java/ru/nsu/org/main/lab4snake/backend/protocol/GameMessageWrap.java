package ru.nsu.org.main.lab4snake.backend.protocol;

import ru.nsu.org.main.lab4snake.backend.protoClass.SnakesProto;
import lombok.Getter;

public class GameMessageWrap {
    @Getter
    private final String senderAddr;
    @Getter
    private final int port;
    @Getter
    private final SnakesProto.GameMessage message;

    public GameMessageWrap(SnakesProto.GameMessage message, String senderAddr, int port) {
        this.senderAddr = senderAddr;
        this.message = message;
        this.port = port;
    }
}
