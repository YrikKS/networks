package ru.nsu.org.main.lab4snake;

import ru.nsu.org.main.lab4snake.controller.MenuController;
import ru.nsu.org.main.lab4snake.view.View;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;


public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Snake");
        primaryStage.initStyle(StageStyle.DECORATED);
        primaryStage.setResizable(false);

        FXMLLoader menuLoader = new FXMLLoader(View.class.getResource("menu.fxml"));
        primaryStage.setScene(new Scene(menuLoader.load()));
        MenuController menuController = menuLoader.getController();
        View menu = new View(primaryStage);
        menuController.setMenuView(menu);
        menuController.start();
        primaryStage.setOnCloseRequest(t -> menuController.onExitButtonPressed());
        primaryStage.show();
    }
}
