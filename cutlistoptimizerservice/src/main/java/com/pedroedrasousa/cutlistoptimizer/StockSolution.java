package com.pedroedrasousa.cutlistoptimizer;

import com.pedroedrasousa.cutlistoptimizer.model.TileDimensions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class StockSolution {

    private List<TileDimensions> stockTileDimensions = new ArrayList<>();

    public StockSolution(List<TileDimensions> tilesDimensions) {
        this.stockTileDimensions = tilesDimensions;
    }

    public StockSolution(TileDimensions... tilesDimensions) {
        for (TileDimensions tileDimensions : tilesDimensions) {
            stockTileDimensions.add(tileDimensions);
        }
    }

    public void addStockTile(TileDimensions tileDimensions) {
        stockTileDimensions.add(tileDimensions);
    }

    public List<TileDimensions> getStockTileDimensions() {
        return stockTileDimensions;
    }

    public void setStockTileDimensions(List<TileDimensions> stockTileDimensions) {
        this.stockTileDimensions = stockTileDimensions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StockSolution that = (StockSolution) o;

        // If number of stock tiles differs, solutions are not equal for sure.
        if (this.stockTileDimensions.size() != that.stockTileDimensions.size()) {
            return false;
        }

        List<TileDimensions> thatStockTileDimensionsCopy = new ArrayList<>(that.stockTileDimensions);

        for (Iterator<TileDimensions> stockIterator = stockTileDimensions.iterator(); stockIterator.hasNext(); ) {
            TileDimensions thisTileDimensions = stockIterator.next();
            boolean match = false;
            for (Iterator<TileDimensions> otherStockIterator = thatStockTileDimensionsCopy.iterator(); otherStockIterator.hasNext(); ) {
                TileDimensions thatTileTimensions = otherStockIterator.next();
                if (thisTileDimensions.hasSameDimensions(thatTileTimensions)) {
                    match = true;
                    otherStockIterator.remove();
                    break;
                }
            }

            if (!match) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        return stockTileDimensions != null ? stockTileDimensions.hashCode() : 0;
    }

    @Override
    public String toString() {
        String bases = new String();
        for (TileDimensions tileDimensions : stockTileDimensions) {
            bases += "[" + tileDimensions.getWidth() + "x" + tileDimensions.getHeight() + "]";
        }
        return bases;
    }

    public int getArea() {
        int area = 0;
        for (TileDimensions tileDimensions : stockTileDimensions) {
            area += tileDimensions.getArea();
        }
        return area;
    }
}
