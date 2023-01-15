package ru.nsu.org.main.lab4snake.backend.node;

import ru.nsu.org.main.lab4snake.backend.protoClass.SnakesProto;
import ru.nsu.org.main.lab4snake.model.Player;

public interface INetHandler {
    int MULTICAST_PORT = 9192;
    String MULTICAST_ADDR = "239.192.0.4";

    void start();
    void end();
    void sender(Player player, SnakesProto.GameMessage message) ;
    void receiver();
    SnakesProto.GameMessage getSteerMessage(SnakesProto.Direction direction);
    void openSocket();
    void startReceiver();
}
