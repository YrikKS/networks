package ru.nsu.org.main.lab4snake.controller;

import javafx.application.Platform;
import ru.nsu.org.main.lab4snake.logger.MyLogger;
import ru.nsu.org.main.lab4snake.backend.node.INetHandler;
import ru.nsu.org.main.lab4snake.backend.node.MasterNetNode;
import ru.nsu.org.main.lab4snake.backend.node.NetNode;
import ru.nsu.org.main.lab4snake.backend.protoClass.SnakesProto;
import ru.nsu.org.main.lab4snake.model.CustomGameConfig;
import ru.nsu.org.main.lab4snake.model.GameModel;
import ru.nsu.org.main.lab4snake.view.StateSystem;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.TextArea;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class GameController implements IController{
    private final ExecutorService threadPool = Executors.newCachedThreadPool();
    @Getter @FXML
    private TextArea textField;
    @Setter
    private GameModel model;
    @Setter
    private IListenerView gameView;
    private INetHandler node;

    @Setter
    private StateSystem state;

    @Getter @FXML
    private Canvas board;

    //for client
    @Setter
    private int serverPort;
    @Setter
    private String serverAddr;
    @Setter
    private SnakesProto.GameConfig serverConfig;


    @FXML
    private void addKeyListener() {
        board.getScene().setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case W, UP -> {
                    var steerMessage = node.getSteerMessage(SnakesProto.Direction.UP);
                    threadPool.submit(()-> {
                        node.sender(null, steerMessage);
                    });
                }
                case A, LEFT-> {
                    var steerMessage = node.getSteerMessage(SnakesProto.Direction.LEFT);
                    threadPool.submit(()-> {
                        node.sender(null, steerMessage);
                    });
                }
                case D, RIGHT -> {
                    var steerMessage = node.getSteerMessage(SnakesProto.Direction.RIGHT);
                    threadPool.submit(()-> {
                        node.sender(null, steerMessage);
                    });
                }
                case S, DOWN -> {
                    var steerMessage = node.getSteerMessage(SnakesProto.Direction.DOWN);
                    threadPool.submit(()-> {
                        node.sender(null, steerMessage);
                    });
                }
            }
        });
    }

    @FXML
    private void onExitButtonPressed() {
        state = StateSystem.MENU;
        gameView.render(state, null);
        node.end();
        gameView.stopListen(model);
        threadPool.shutdown();
    }

    public void onExitWindowButtonPressed() {
        node.end();
        gameView.stopListen(model);
        threadPool.shutdown();
        Platform.exit();
    }

    @Override
    public void start() throws Exception {
        CustomGameConfig config = new CustomGameConfig();
        addKeyListener();
        config.initConfig();
        switch (state) {
            case JOIN_GAME -> {
                model = new GameModel();
                gameView.listen(model);
                node = new NetNode(gameView, serverConfig, serverAddr, serverPort, config.getLogin(), model);
                gameView.render(StateSystem.LOAD_GAME, "CONNECTING TO THE SERVER");
                node.openSocket();
                node.start();
            }
            case NEW_GAME -> {
                MyLogger.getLogger().info("Start game on client!");
                model = new GameModel(config);
                gameView.listen(model);
                node = new MasterNetNode(config, model);
                node.openSocket();
                node.start();
            }
        }
    }
}
