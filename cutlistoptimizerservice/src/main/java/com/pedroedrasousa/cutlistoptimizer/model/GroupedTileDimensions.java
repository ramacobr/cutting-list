package com.pedroedrasousa.cutlistoptimizer.model;

/**
 * Created by pedro on 4/17/17.
 */
public class GroupedTileDimensions extends TileDimensions {

    private final int group;

    public GroupedTileDimensions(GroupedTileDimensions that) {
        super(that.width, that.height);
        this.group = that.group;
    }

    public GroupedTileDimensions(TileDimensions that, int group) {
        super(that);
        this.group = group;
    }

    public GroupedTileDimensions(int width, int height, int group) {
        super(width, height);
        this.group = group;
    }

    public int getGroup() {
        return group;
    }

    public boolean equalsBaseTileDimensions(BaseTileDimensions baseTileDimensions) {
        return this.width == baseTileDimensions.width && this.height == baseTileDimensions.height;
    }

    @Override
    public String toString() {
        return "id=" + id + ", gropup=" + group + "[" + width + "x" + height + ']';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        GroupedTileDimensions that = (GroupedTileDimensions) o;

        return group == that.group;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + group;
        return result;
    }
}
