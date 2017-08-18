package com.pedroedrasousa.cutlistoptimizer.model;

public class BaseTileDimensions {

    protected final int width;

    protected final int height;

    public BaseTileDimensions(BaseTileDimensions that) {
        this.width = that.width;
        this.height = that.height;
    }

    public BaseTileDimensions(int width, int height) {
        this.width = width;
        this.height = height;
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

    public long getArea() {
        return width * height;
    }

    public BaseTileDimensions rotate90() {
        return new BaseTileDimensions(this.height, this.width);
    }

    public boolean isSquare() {
        return this.width == this.height;
    }

    public boolean isHorizontal() {
        return this.width > this.height;
    }

    @Override
    public String toString() {
        return "[" + width + "x" + height + ']';
    }

    /**
     * Whether specified tile has the same dimensions.
     * Orientation doesn't matter.
     *
     * @param that
     * @return
     */
    public boolean hasSameDimensions(BaseTileDimensions that) {
        if ((this.width == that.width && this.height == that.height
                || this.width == that.height && this.height == that.width)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BaseTileDimensions that = (BaseTileDimensions) o;

        if (width != that.width) return false;
        return height == that.height;
    }

    @Override
    public int hashCode() {
        int result = width;
        result = 31 * result + height;
        return result;
    }
}
