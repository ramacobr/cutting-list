package com.pedroedrasousa.tiling;

import com.pedroedrasousa.tiling.model.TileDimensions;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class StockPanelPicker {

    private boolean isExcluded(StockSolution stockSolution, List<StockSolution> excludedStockSolutions) {
        // Loop through excluded stock solutions and compare them against the candidate one
        for (StockSolution excludedStockSolution : excludedStockSolutions) {
            if (excludedStockSolution.equals(stockSolution)) {
                return true;
            }
        }
        return false;
    }

    private boolean isExcluded(List<TileDimensions> stockTiles, List<StockSolution> excludedStockSolutions, List<Integer> stockTilesIndexes) {

        if (excludedStockSolutions == null || excludedStockSolutions.size() == 0) {
            return false;
        }

        // Build a stock solution object with the candidate stock tile indexes to compare against the excluded stock solutions
        List<TileDimensions> stockSolutions = new ArrayList<>();
        for (Integer i : stockTilesIndexes) {
            stockSolutions.add(stockTiles.get(i));
        }
        StockSolution candidateStockSolution = new StockSolution(stockSolutions);

        return isExcluded(candidateStockSolution, excludedStockSolutions);
    }


    private Integer getNextUnusedStockTile(int nbrStockTiles, List<Integer> stockTilesIndexes, int index) {
        for (int i = index + 1; i < nbrStockTiles; i++) {
            if (!stockTilesIndexes.contains(i)) {
                return i;
            }
        }
       return null;
    }

    /**
     * Computes the best stock solution based on the specified criteria.
     *
     * @param stockTiles Available stock tiles to be used
     * @param requiredArea Area required by the pretended tiles
     * @param nbrTiles Number of stock panels to be used
     * @param exclusions Stock solutions to exclude.
     * @return The computed stock solution, null if the specified criterias could not be fulfilled.
     */
    private StockSolution getCandidateStockSolution(List<TileDimensions> stockTiles, int requiredArea, int nbrTiles, List<StockSolution> exclusions) {
        List<Integer> stockTilesIndexes = new ArrayList<>();

        // Sort base tiles by area, least area first.
        stockTiles.sort(Comparator.comparingInt(TileDimensions::getArea));

        // Start with the stock panels with least area
        for (int i = 0; i < nbrTiles; i++) {
            stockTilesIndexes.add(i);
        }

        // Iterate through the stock tiles that will be used
        for (int i = 0; i < nbrTiles; i++) {

            Integer nextSpareTileIndex = null;

            // Keep incrementing the stock tile being iterated until required area is met or the biggest tile is reached.
            do {
                // Check if the current set of stock tiles meet the required area
                int remainingRequiredArea = requiredArea;
                for (Integer sol : stockTilesIndexes) {
                    remainingRequiredArea -= stockTiles.get(sol).getArea();
                }

                // If required area is met and solution is not excluded, build an array with the tile dimensions and return it.
                if (remainingRequiredArea <= 0 && !isExcluded(stockTiles, exclusions, stockTilesIndexes)) {
                    StockSolution stockSolution = new StockSolution();
                    for (Integer sol : stockTilesIndexes) {
                        stockSolution.addStockTile(stockTiles.get(sol));
                    }
                    return stockSolution;
                }

                // Use the next size for the stock tile number being iterated
                nextSpareTileIndex = getNextUnusedStockTile(stockTiles.size(), stockTilesIndexes, stockTilesIndexes.get(i));
                if (nextSpareTileIndex != null) {
                    stockTilesIndexes.set(i, nextSpareTileIndex);
                }

            } while (nextSpareTileIndex != null);
        }

        return null;
    }

    public StockSolution getCandidateStockSolutions(List<TileDimensions> tilesToFit, List<TileDimensions> stockTiles, float areaDelta, int nbrSpare, List<StockSolution> exclusions, int minNbrPanels) {

        StockSolution stockSolution;

        // Sort base tiles by area, least area first.
        stockTiles.sort(Comparator.comparingInt(TileDimensions::getArea));

        int requiredMaxDimension = 0;

        // Calculate the required area for fitting every tile.
        int requiredArea = 0;
        for (TileDimensions tile : tilesToFit) {
            requiredArea += tile.getArea();
            if (tile.getMaxDimension() > requiredMaxDimension) {
                requiredMaxDimension = tile.getMaxDimension();
            }
        }

        // Add required delta to area
        requiredArea = (int)(requiredArea * (1.0f + areaDelta));

        // Try to match required area with the least possible number of stock tiles.
        // Start with one stock tile and increment until required area is met.
        // Resulting solution will be returned if no spare is requested.
        int nbrTiles;
        for (nbrTiles = minNbrPanels; nbrTiles < stockTiles.size(); nbrTiles++) {
            stockSolution = getCandidateStockSolution(stockTiles, requiredArea, nbrTiles, exclusions);
            if (stockSolution != null) {
                if (nbrSpare == 0) {
                    return stockSolution;
                }
                break;
            }
        }

        // If at least one spare stock panel was requested and there are still stock panels remaining build a solution
        if (nbrSpare > 0 && nbrTiles < stockTiles.size()) {
            stockSolution = getCandidateStockSolution(stockTiles, requiredArea, nbrTiles + nbrSpare, exclusions);
            if (stockSolution != null) {
                return stockSolution;
            }
        }

        // Couldn't find stock tiles to fit the required area
        // Return biggest stock tiles as last resort if not in exclusions
        stockSolution = new StockSolution();
        for (int i = 0; i < stockTiles.size(); i++) {
            stockSolution.addStockTile(stockTiles.get(stockTiles.size() - 1));
        }
        if (!isExcluded(stockSolution, exclusions)) {
            return stockSolution;
        }

        return null;
    }
}
