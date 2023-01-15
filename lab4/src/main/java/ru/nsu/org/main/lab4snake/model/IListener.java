package ru.nsu.org.main.lab4snake.model;


public interface IListener {
    void modelChanged(GameModel model);
    void modelChanged(String message);
    void listen(IModel model);
    void stopListen(IModel model);
}
