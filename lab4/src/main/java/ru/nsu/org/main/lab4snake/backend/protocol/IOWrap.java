package ru.nsu.org.main.lab4snake.backend.protocol;

import ru.nsu.org.main.lab4snake.backend.protoClass.SnakesProto;

public interface IOWrap {
    void send(SnakesProto.GameMessage message, String receiver, int receiverPort);
    GameMessageWrap receive();
}
