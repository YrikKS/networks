package ru.nsu.org.main.lab4snake.controller;

import ru.nsu.org.main.lab4snake.backend.mcHandlers.MenuHandler;
import ru.nsu.org.main.lab4snake.backend.mcHandlers.MulticastReciever;
import ru.nsu.org.main.lab4snake.backend.node.NetNode;
import ru.nsu.org.main.lab4snake.backend.protoClass.SnakesProto;
import ru.nsu.org.main.lab4snake.view.IView;
import ru.nsu.org.main.lab4snake.view.InfoGame;
import ru.nsu.org.main.lab4snake.view.StateSystem;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

import lombok.Setter;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MenuController implements IController {
    private final ExecutorService mcReceiverThreadPool = Executors.newFixedThreadPool(1);
    @FXML
    private ListView<String> availableGames;
    @Setter
    private IView menuView;

    private String serverInfo;
    private StateSystem state = StateSystem.MENU;

    @FXML
    private void listListener() {
        availableGames.setOnMouseClicked(event-> {
            int index = availableGames.getSelectionModel().getSelectedIndex();
            System.out.println(index);
            if (index >= 0) {
                serverInfo = availableGames.getItems().get(index);
                if (!Objects.equals(serverInfo, "No game found.")) {
                    menuView.sendServerInfoToGameController(serverInfo);
                }
            }
            onGamePressed();
        });
    }

    @FXML
    private void onGamePressed() {
        state = StateSystem.JOIN_GAME;
        mcReceiverThreadPool.shutdown();
        menuView.render(state, null);
    }

    @FXML
    private void onPlayButtonPressed() {
        state = StateSystem.NEW_GAME;
        mcReceiverThreadPool.shutdown();
        menuView.render(state, null);
    }

    @FXML
    public void onExitButtonPressed() {
        state = StateSystem.EXIT;
        mcReceiverThreadPool.shutdown();
        Platform.exit();
    }

    private void addAvailableServer(MenuHandler handler) {
        availableGames.getItems().clear();
        var listAvailableServers  = handler.getAvailableGames();
        if (listAvailableServers.isEmpty()) {
            availableGames.getItems().add("No game found.");
        } else {
            var info = new InfoGame();
            for (var server : listAvailableServers.keySet()) {
                var msg = listAvailableServers.get(server).getKey();
                info.setAmountPlayers(msg.getPlayers().getPlayersList().size());
                info.setWidthField(msg.getConfig().getWidth());
                info.setHeightField(msg.getConfig().getHeight());
                info.setAddr(server.getValue().getHostAddress());
                info.setPort(server.getKey());
                for (var player : msg.getPlayers().getPlayersList()) {
                    if (player.getRole() == SnakesProto.NodeRole.MASTER) {
                        info.setMasterName(player.getName());
                    }
                }
                availableGames.getItems().add(info.toString());
                availableGames.refresh();
            }
        }
    }

    @Override
    public void start() {
        var handler = new MenuHandler();
        MulticastReciever multicastReciever = new MulticastReciever(handler, NetNode.MULTICAST_PORT, NetNode.MULTICAST_ADDR);
        mcReceiverThreadPool.submit(() ->{
            multicastReciever.init();
            while (state == StateSystem.MENU) {
                multicastReciever.run();
                Platform.runLater(() -> {
                    if (handler.isMapChange()) {
                        addAvailableServer(handler);
                    }
                });
            }
            multicastReciever.close();
        });
    }
}
