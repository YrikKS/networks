package ru.nsu.org.main.lab4snake.view;

import lombok.Getter;
import lombok.Setter;

import java.net.InetAddress;

public class InfoGame {
    @Getter @Setter
    private String masterName;
    @Getter @Setter
    private int amountPlayers;
    @Getter @Setter
    private int widthField;
    @Getter @Setter
    private int heightField;
    @Getter @Setter
    private int port;
    @Getter @Setter
    private String addr;

    @Override
    public String toString() {

        return String.format("""
                Game master: %s
                Players amount: %s
                Field size: %s x %s
                Port: %s
                Address: %s
                """, masterName, amountPlayers, widthField, heightField, port, addr);
    }

}
