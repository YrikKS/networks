package ru.nsu.lab5;


import java.nio.ByteBuffer;

public abstract class Connection implements Handler {
    private boolean disconnect = false;

    abstract void linkBuffer(ByteBuffer clientBuffer);

    public void setDisconnect() {
        disconnect = true;
    }

    public boolean getDisconnect() {
        return disconnect;
    }
}
