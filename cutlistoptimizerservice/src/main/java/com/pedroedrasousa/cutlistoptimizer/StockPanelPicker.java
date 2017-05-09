package com.pedroedrasousa.cutlistoptimizer;

import com.pedroedrasousa.cutlistoptimizer.model.TileDimensions;

import java.util.List;

public interface StockPanelPicker {
    StockSolution getCandidateStockSolutions(List<TileDimensions> tilesToFit, List<TileDimensions> stockTiles, float areaDelta, int nbrSpare, List<StockSolution> exclusions, int minNbrPanels, int maxNbrTiles);
}
