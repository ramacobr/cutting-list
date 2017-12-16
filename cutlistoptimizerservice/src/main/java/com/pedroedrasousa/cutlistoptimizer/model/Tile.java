package com.pedroedrasousa.cutlistoptimizer.model;

/**
 * Model representing a tile in 2D space.
 *
 * @author Pedro Edra Sousa
 */
public class Tile {

    private final int x1;

    private final int x2;

    private final int y1;

    private final int y2;

    public Tile(TileDimensions tileDimensions) {
        this.x1 = 0;
        this.x2 = tileDimensions.getWidth();
        this.y1 = 0;
        this.y2 = tileDimensions.getHeight();
    }

    public Tile(int x1, int x2, int y1, int y2) {
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
    }

    public Tile(Tile tile) {
        this.x1 = tile.x1;
        this.x2 = tile.x2;
        this.y1 = tile.y1;
        this.y2 = tile.y2;
    }

    public int getX1() {
        return x1;
    }

    public int getX2() {
        return x2;
    }

    public int getY1() {
        return y1;
    }

    public int getY2() {
        return y2;
    }

    public int getWidth() {
        return x2 - x1;
    }

    public int getHeight() {
        return y2 - y1;
    }

    public long getArea() {
        return (long)getWidth() * (long)getHeight();
    }

    public int getMaxSide() {
        return Math.max(getWidth(), getHeight());
    }

    public boolean isHorizontal() {
        return this.getWidth() > this.getHeight();
    }

    public boolean isVertical() {
        return this.getHeight() > this.getWidth();
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }

        if ( !(obj instanceof Tile) ) {
            return false;
        }

        Tile tile = (Tile)obj;

        return  this.x1 == tile.x1 &&
                this.x2 == tile.x2 &&
                this.y1 == tile.y1 &&
                this.y2 == tile.y2;
    }
}
