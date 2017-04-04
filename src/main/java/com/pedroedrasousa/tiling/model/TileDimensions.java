package com.pedroedrasousa.tiling.model;

public class TileDimensions {

    private final int id;

    private final int width;

    private final int height;

    public TileDimensions(TileDimensions tileDimensions) {
        this.id = tileDimensions.id;
        this.width = tileDimensions.width;
        this.height = tileDimensions.height;
    }

    public TileDimensions(int id, int width, int height) {
        this.id = id;
        this.width = width;
        this.height = height;
    }

    public TileDimensions(int width, int height) {
        this.id = -1;
        this.width = width;
        this.height = height;
    }

    public int getId() {
        return id;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getMaxDimension() {
        return Math.max(width, height);
    }

    public int getArea() {
        return width * height;
    }

    public TileDimensions rotate90() {
        return new TileDimensions(this.id, this.height, this.width);
    }

    public boolean isSquare() {
        return this.width == this.height;
    }

    @Override
    public String toString() {
        return "" + id + "[" + width + "x" + height + ']';
    }

    /**
     * Whether specified tile has the same dimensions.
     * Orientation doesn't matter.
     *
     * @param other
     * @return
     */
    public boolean hasSameDimensions(TileDimensions other) {
        if ((this.width == other.width && this.height == other.height
                || this.width == other.height && this.height == other.width)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TileDimensions that = (TileDimensions) o;

        if (id != that.id) return false;
        if (width != that.width) return false;
        return height == that.height;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + width;
        result = 31 * result + height;
        return result;
    }
}
