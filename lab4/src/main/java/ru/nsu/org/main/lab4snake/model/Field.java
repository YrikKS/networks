package ru.nsu.org.main.lab4snake.model;

import ru.nsu.org.main.lab4snake.view.Tile;
import lombok.Getter;

import java.util.ArrayList;


public class Field {
    private final static int FIND_WINDOW_SIZE = 5;
    private final static int LOCAL_MIDDLE = (FIND_WINDOW_SIZE - 1) / 2;
    @Getter
    private final int width;
    @Getter
    private final int height;
    private final Tile[] tileArray;
    @Getter
    private final ArrayList<Point> emptyPoints;
    @Getter
    private boolean isThereFreeSpace;

    Field(int width, int height) {
        this.width = width;
        this.height = height;
        this.isThereFreeSpace = true;
        this.emptyPoints = new ArrayList<>();
        this.tileArray = new Tile[width * height];
        initField();
    }

    public synchronized void initField() {
        for (int y = 0; y < this.height; ++y) {
            for (int x = 0; x < this.width; ++x) {
                this.tileArray[y * width + x] = Tile.BOARD;
                emptyPoints.add(new Point(x, y));
            }
        }
    }

    private boolean isFree(Point begin, Point end) {
        for (int y = begin.getY(); y < end.getY(); ++y) {
            for (int x = begin.getX(); x < end.getX(); ++x) {
                if (!isPointFree(new Point(x, y))) {
                    return false;
                }
            }
        }
        return true;
    }

    public int countFood() {
        int foodOnField = 0;
        for (var tile : tileArray) {
            if (tile == Tile.FOOD) {
                ++foodOnField;
            }
        }
        return foodOnField;
    }


    public Point findEmptySpace() {
        // middle of the empty space
        Point middle = new Point(-1, -1);
        // coordinates for window
        Point beginWindow = new Point(-1, -1);
        Point endWindow = new Point(-1, -1);
        // loop for shifting window 5x5
        for (int yWindow = 0; yWindow < height - FIND_WINDOW_SIZE; ++yWindow) {
            for (int xWindow = 0; xWindow < width - FIND_WINDOW_SIZE; ++xWindow) {
                beginWindow.setX(xWindow);
                beginWindow.setY(yWindow);
                endWindow.setX(xWindow + FIND_WINDOW_SIZE);
                endWindow.setY(yWindow + FIND_WINDOW_SIZE);
                if (isFree(beginWindow, endWindow)) {
                    isThereFreeSpace = true;
                    // find free window
                    middle.setX(beginWindow.getX() + LOCAL_MIDDLE);
                    middle.setY(beginWindow.getY() + LOCAL_MIDDLE);
                    return middle;
                } else {
                    isThereFreeSpace = false;
                }
            }
        }
        return middle;
    }

    public int getAmountEmptyPoint() {
        return emptyPoints.size();
    }

    public Point getEmptyPoint(int index) {
        return emptyPoints.get(index);
    }

    public void addEmptyPont(Point point) {
        emptyPoints.add(point);
    }

    public void deleteEmptyPoint(Point point) {
        emptyPoints.remove(point);
    }

    public synchronized void setTile(Point point, Tile tile) {
        tileArray[point.getY() * width + point.getX()] = tile;
    }

    public synchronized Tile getTile(Point point) {
        return tileArray[point.getY() * width + point.getX()];
    }

    public synchronized Tile getTile(int x, int y) {
        return tileArray[y * width + x];
    }

    public boolean isPointFree(Point point) {
        return tileArray[point.getY() * width + point.getX()] == Tile.BOARD;
    }

    public boolean isSnake(Point point) {
        return (tileArray[point.getY() * width + point.getX()] == Tile.SNAKE_BODY)
                || tileArray[point.getY() * width + point.getX()] == Tile.SNAKE_HEAD || tileArray[point.getY() * width + point.getX()] == Tile.MY_SNAKE_HEAD;
    }
}

