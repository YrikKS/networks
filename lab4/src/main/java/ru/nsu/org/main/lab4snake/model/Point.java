package ru.nsu.org.main.lab4snake.model;

import ru.nsu.org.main.lab4snake.backend.protoClass.SnakesProto;
import lombok.Getter;
import lombok.Setter;

public class Point  {
    @Getter @Setter
    private int x;
    @Getter @Setter
    private int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public SnakesProto.GameState.Coord convertPointToCoord() {
        return SnakesProto.GameState.Coord.newBuilder().setX(x).setY(y).build();
    }

    public void convertCoordToPoint(SnakesProto.GameState.Coord coord) {
        this.x = coord.getX();
        this.y = coord.getY();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Point)) {
            return false;
        }
	    Point point = (Point) obj;
        return this.x == point.getX()
                && this.y == point.getY();
    }
}
