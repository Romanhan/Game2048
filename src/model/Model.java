package model;

import view.Tile;

import java.lang.reflect.Field;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class Model {
    private static final int FIELD_WIDTH = 4;
    private Tile[][] gameTiles;
    private int score;
    private int maxTile;

    public Model() {
        resetGameTiles();
        this.score = 0;
        this.maxTile = 0;
    }

    public int getScore() {
        return score;
    }

    public int getMaxTile() {
        return maxTile;
    }

    public void setMaxTile(int maxTile) {
        this.maxTile = maxTile;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public Tile[][] getGameTiles() {
        return gameTiles;
    }

    // Make list of empty Tile by checking gameTile array and returning it
    private List<Tile> getEmptyTiles() {
        List<Tile> checkTile = new ArrayList<>();

        for (Tile[] tileArray : gameTiles) {
            for (Tile tile : tileArray) {
                if (tile.isEmpty()) {
                    checkTile.add(tile);
                }
            }
        }
        return checkTile;
    }

    // Adding new random number on the empty Tile, by checking the list of empty Tile from getEmptyTiles list
    private void addTile() {
        List<Tile> emptyTiles = getEmptyTiles();
        if (!emptyTiles.isEmpty()) {
            int index = (int) (Math.random() * emptyTiles.size()) % emptyTiles.size();
            Tile emptyTile = emptyTiles.get(index);
            emptyTile.value = Math.random() < 0.9 ? 2 : 4;
        }
    }

    public void resetGameTiles() {
        gameTiles = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                gameTiles[i][j] = new Tile();
            }
        }
        addTile();
        addTile();
    }

    // Compress Tile array if it can be done, only one array line to the left
    private boolean compressTiles(Tile[] tiles) {
        int insertPosition = 0;
        boolean arrayChange = false;
        for (int i = 0; i < FIELD_WIDTH; i++) {
            if (!tiles[i].isEmpty()) {
                if (i != insertPosition) {
                    tiles[insertPosition] = tiles[i];
                    tiles[i] = new Tile();
                    arrayChange = true;
                }
                insertPosition++;
            }
        }
        return arrayChange;
    }

    // Merge Tiles if it can be done, one array line to the left
    private boolean mergeTiles(Tile[] tiles) {
        boolean wasMarge = false;
        LinkedList<Tile> tilesList = new LinkedList<>();
        for (int i = 0; i < FIELD_WIDTH; i++) {
            if (tiles[i].isEmpty()) {
                continue;
            }
            if (i < FIELD_WIDTH - 1 && tiles[i].value == tiles[i + 1].value) {
                int updateValue = tiles[i].value * 2;
                if (updateValue > maxTile) {
                    maxTile = updateValue;
                    wasMarge = true;
                }
                score += updateValue;
                tilesList.addLast(new Tile(updateValue));
                tiles[i + 1].value = 0;
            } else {
                tilesList.addLast(new Tile(tiles[i].value));
            }
            tiles[i].value = 0;
        }
        for (int i = 0; i < tilesList.size(); i++) {
            tiles[i] = tilesList.get(i);
        }
        return wasMarge;
    }

    // Move array counter-clockwise left
    private void arrayMoveLeft() {
        Tile[][] movedArray = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        int k = FIELD_WIDTH - 1;
        int m = 0;

        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                movedArray[k][m] = gameTiles[i][j];
                k--;
                if (k < 0) {
                    k = FIELD_WIDTH - 1;
                }
            }
            m++;
            if (m > FIELD_WIDTH - 1) {
                m = 0;
            }
        }
        gameTiles = movedArray;
    }

    // Move Tiles left, if moved add new Tile
    public void left() {
        boolean moveFlag = false;
        for (int i = 0; i < FIELD_WIDTH; i++) {
            if (compressTiles(gameTiles[i]) | mergeTiles(gameTiles[i])) {
                moveFlag = true;
            }
        }
        if (moveFlag) {
            addTile();
        }
    }

    // For moving we rotate array, move left and then rotating it back
    public void up() {
        arrayMoveLeft();
        left();
        arrayMoveLeft();
        arrayMoveLeft();
        arrayMoveLeft();
    }

    public void down() {
        arrayMoveLeft();
        arrayMoveLeft();
        arrayMoveLeft();
        left();
        arrayMoveLeft();
    }

    public void right() {
        arrayMoveLeft();
        arrayMoveLeft();
        left();
        arrayMoveLeft();
        arrayMoveLeft();
    }

    private int getEmptyTilesCount() {
        return getEmptyTiles().size();
    }

    private boolean isFull() {
        return getEmptyTilesCount() == 0;
    }

    // Check if there are two empty Tiles in a row or in a column, if yes canMove
    public boolean canMove() {
        if (!isFull()) {
            return true;
        }
        for (int x = 0; x < FIELD_WIDTH; x++) {
            for (int y = 0; y < FIELD_WIDTH; y++) {
                Tile t = gameTiles[x][y];
                if ((x < FIELD_WIDTH - 1 && t.value == gameTiles[x + 1][y].value)
                    || (y < FIELD_WIDTH - 1 && t.value == gameTiles[x][y + 1].value)){
                    return true;
                }
            }
        }
        return false;
    }

}
