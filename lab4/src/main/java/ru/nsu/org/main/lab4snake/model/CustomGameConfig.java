package ru.nsu.org.main.lab4snake.model;

import org.apache.commons.lang3.exception.ExceptionUtils;
import ru.nsu.org.main.lab4snake.backend.protoClass.SnakesProto;
import lombok.Getter;
import ru.nsu.org.main.lab4snake.logger.MyLogger;

import java.io.*;
import java.util.Properties;

public class CustomGameConfig {

    @Getter
    private String login;
    @Getter
    private int width;
    @Getter
    private int height;
    @Getter
    private int foodStatic;
    @Getter
    private float foodPerPlayer;
    @Getter
    private int stateDelay;
    @Getter
    private int pingDelay;
    @Getter
    private float deadProbFood;
    @Getter
    private int nodeTimeout;

    public void initConfig() {
        Properties config = getConfig();
        this.login = config.getProperty("user");
        this.width = Integer.parseInt(config.getProperty("width"));
        this.height = Integer.parseInt(config.getProperty("height"));
        this.foodStatic = Integer.parseInt(config.getProperty("food_static"));
        this.foodPerPlayer = Float.parseFloat(config.getProperty("food_per_player"));
        this.stateDelay = Integer.parseInt(config.getProperty("state_delay_ms"));
        this.deadProbFood = Float.parseFloat(config.getProperty("dead_prob_food"));
        this.pingDelay = Integer.parseInt(config.getProperty("ping_delay_ms"));
        this.nodeTimeout = Integer.parseInt(config.getProperty("node_timeout_ms"));

    }

    private Properties getConfig() {
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream("src/main/resources/config.properties")) {
            prop.load(input);
        } catch (Exception e) {
            MyLogger.getLogger().error(ExceptionUtils.getStackTrace(e));
            return null;
        }
        return prop;
    }

    public SnakesProto.GameConfig convertToProto() {
        return SnakesProto.GameConfig.newBuilder().setWidth(width).setHeight(height).
                setFoodStatic(foodStatic).setFoodPerPlayer(foodPerPlayer).setStateDelayMs(stateDelay).
                setDeadFoodProb(deadProbFood).setPingDelayMs(pingDelay).setNodeTimeoutMs(nodeTimeout).build();
    }
}
