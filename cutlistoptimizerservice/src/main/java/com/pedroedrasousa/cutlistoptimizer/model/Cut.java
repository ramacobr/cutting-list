package com.pedroedrasousa.cutlistoptimizer.model;

public class Cut {

    private final int x1;

    private final int y1;

    private final int x2;

    private final int y2;

    private final int originalWidth;

    private final int originalHeight;

    private final boolean isHorizontal;

    private final int cutCoords;

    private final int originalTileId;

    private final int child1TileId;

    private final int child2TileId;

    public Cut(Cut cut) {
        this.x1 = cut.x1;
        this.y1 = cut.y1;
        this.x2 = cut.x2;
        this.y2 = cut.y2;

        this.originalWidth = cut.originalWidth;
        this.originalHeight = cut.originalHeight;
        this.isHorizontal = cut.isHorizontal;
        this.cutCoords = cut.cutCoords;

        this.originalTileId = cut.originalTileId;
        this.child1TileId = cut.child1TileId;
        this.child2TileId = cut.child2TileId;
    }

    public Cut(int x1, int y1, int x2, int y2, int originalWidth, int originalHeight, boolean isHorizontal, int cutCoords, int originalTileId, int child1TileId, int child2TileId) {
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;

        this.originalWidth = originalWidth;
        this.originalHeight = originalHeight;
        this.isHorizontal = isHorizontal;
        this.cutCoords = cutCoords;

        this.originalTileId = originalTileId;
        this.child1TileId = child1TileId;
        this.child2TileId = child2TileId;
    }

    public Cut(Builder builder) {
        this.x1 = builder.x1;
        this.x2 = builder.x2;
        this.y1 = builder.y1;
        this.y2 = builder.y2;

        this.originalWidth = builder.originalWidth;
        this.originalHeight = builder.originalHeight;
        this.isHorizontal = builder.isHorizontal;
        this.cutCoords = builder.cutCoords;

        this.originalTileId = builder.originalTileId;
        this.child1TileId = builder.child1TileId;
        this.child2TileId = builder.child2TileId;
    }

    public int getX1() {
        return x1;
    }

    public int getY1() {
        return y1;
    }

    public int getX2() {
        return x2;
    }

    public int getY2() {
        return y2;
    }

    public int getOriginalTileId() {
        return originalTileId;
    }

    public int getChild1TileId() {
        return child1TileId;
    }

    public int getChild2TileId() {
        return child2TileId;
    }

    public int getOriginalWidth() {
        return originalWidth;
    }

    public int getOriginalHeight() {
        return originalHeight;
    }

    public boolean getIsHorizontal() {
        return isHorizontal;
    }

    public int getCutCoords() {
        return cutCoords;
    }

    public static class Builder {
        private int x1;

        private int y1;

        private int x2;

        private int y2;

        private int originalWidth;

        private int originalHeight;

        private boolean isHorizontal;

        private int cutCoords;

        private int originalTileId;

        private int child1TileId;

        private int child2TileId;

        public int getX1() {
            return x1;
        }

        public Builder setX1(int x1) {
            this.x1 = x1;
            return this;
        }

        public int getY1() {
            return y1;
        }

        public Builder setY1(int y1) {
            this.y1 = y1;
            return this;
        }

        public int getX2() {
            return x2;
        }

        public Builder setX2(int x2) {
            this.x2 = x2;
            return this;
        }

        public int getY2() {
            return y2;
        }

        public Builder setY2(int y2) {
            this.y2 = y2;
            return this;
        }

        public int getOriginalWidth() {
            return originalWidth;
        }

        public Builder setOriginalWidth(int originalWidth) {
            this.originalWidth = originalWidth;
            return this;
        }

        public int getOriginalHeight() {
            return originalHeight;
        }

        public Builder setOriginalHeight(int originalHeight) {
            this.originalHeight = originalHeight;
            return this;
        }

        public boolean isHorizontal() {
            return isHorizontal;
        }

        public Builder setHorizontal(boolean horizontal) {
            isHorizontal = horizontal;
            return this;
        }

        public int getCutCoords() {
            return cutCoords;
        }

        public Builder setCutCoords(int cutCoords) {
            this.cutCoords = cutCoords;
            return this;
        }

        public int getOriginalTileId() {
            return originalTileId;
        }

        public Builder setOriginalTileId(int originalTileId) {
            this.originalTileId = originalTileId;
            return this;
        }

        public int getChild1TileId() {
            return child1TileId;
        }

        public Builder setChild1TileId(int child1TileId) {
            this.child1TileId = child1TileId;
            return this;
        }

        public int getChild2TileId() {
            return child2TileId;
        }

        public Builder setChild2TileId(int child2TileId) {
            this.child2TileId = child2TileId;
            return this;
        }

        public Cut build() {
            return new Cut(this);
        }
    }
}
