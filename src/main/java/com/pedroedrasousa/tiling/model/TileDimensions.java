package com.pedroedrasousa.tiling.model;

import com.fasterxml.jackson.databind.ser.Serializers;

public class TileDimensions {

    protected final int id;

    protected final int width;

    protected final int height;

    protected final boolean isPlaceHolder;

    public TileDimensions(TileDimensions that) {
        this.id = that.id;
        this.width = that.width;
        this.height = that.height;
        this.isPlaceHolder = that.isPlaceHolder;
    }

    public TileDimensions(int id, int width, int height, boolean isPlaceHolder) {
        this.id = id;
        this.width = width;
        this.height = height;
        this.isPlaceHolder = isPlaceHolder;
    }

    public TileDimensions(int width, int height, boolean isPlaceHolder) {
        this.id = -1;
        this.width = width;
        this.height = height;
        this.isPlaceHolder = isPlaceHolder;
    }

    public TileDimensions(int id, int width, int height) {
        this.id = id;
        this.width = width;
        this.height = height;
        this.isPlaceHolder = false;
    }

    public TileDimensions(int width, int height) {
        this.id = -1;
        this.width = width;
        this.height = height;
        this.isPlaceHolder = false;
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

    public boolean isPlaceHolder() {
        return isPlaceHolder;
    }

    public int getMaxDimension() {
        return Math.max(width, height);
    }

    public int getArea() {
        return width * height;
    }

    public TileDimensions rotate90() {
        return new TileDimensions(this.id, this.height, this.width, this.isPlaceHolder);
    }

    public boolean isSquare() {
        return this.width == this.height;
    }

    public boolean isHorizontal() {
        return this.width > this.height;
    }

    @Override
    public String toString() {
        return "id=" + id + "[" + width + "x" + height + ']';
    }

    public String dimensionsToString() {
        return "[" + width + "x" + height + ']';
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

    public BaseTileDimensions toBaseTileDimensions() {
        return new BaseTileDimensions(width, height);
    }
}
