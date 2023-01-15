package ru.nsu.org.main.lab4snake.view;

import ru.nsu.org.main.lab4snake.backend.protoClass.SnakesProto;

public interface IView {
    void render(StateSystem stateSystem, String message);
    void sendServerInfoToGameController(String message);
    void sendConfigToGameController(SnakesProto.GameConfig config);
}
