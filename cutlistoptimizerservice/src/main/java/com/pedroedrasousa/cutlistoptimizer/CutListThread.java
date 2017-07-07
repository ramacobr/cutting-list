package com.pedroedrasousa.cutlistoptimizer;

import com.pedroedrasousa.cutlistoptimizer.model.*;
import com.pedroedrasousa.cutlistoptimizer.comparator.SolutionComparatorFactory;
import com.pedroedrasousa.cutlistoptimizer.comparator.SolutionMostNbrTilesComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class CutListThread implements Runnable {

    private final static Logger logger = LoggerFactory.getLogger(CutListThread.class);

    private RunningTasks runningTasks;

    public RunningTasks getRunningTasks() {
        return runningTasks;
    }

    public void setRunningTasks(RunningTasks runningTasks) {
        this.runningTasks = runningTasks;
    }

    private String permutationId;

    private List<TileDimensions> tiles;
    private List<Solution> solutions;
    private Configuration cfg;
    private int accuracyFactor;
    private List<Solution> allSolutions;

    private StockSolution stockSolution;

    public String getPermutationId() {
        return permutationId;
    }

    public void setPermutationId(String permutationId) {
        this.permutationId = permutationId;
    }

    public List<TileDimensions> getTiles() {
        return tiles;
    }

    public void setTiles(List<TileDimensions> tiles) {
        this.tiles = tiles;
    }

    public List<Solution> getSolutions() {
        return solutions;
    }

    public void setSolutions(List<Solution> solutions) {
        this.solutions = solutions;
    }

    public Configuration getCfg() {
        return cfg;
    }

    public void setCfg(Configuration cfg) {
        this.cfg = cfg;
    }

    public int getAccuracyFactor() {
        return accuracyFactor;
    }

    public void setAccuracyFactor(int accuracyFactor) {
        this.accuracyFactor = accuracyFactor;
    }

    public List<Solution> getAllSolutions() {
        return allSolutions;
    }

    public void setAllSolutions(List<Solution> allSolutions) {
        this.allSolutions = allSolutions;
    }

    public StockSolution getStockSolution() {
        return stockSolution;
    }

    public void setStockSolution(StockSolution stockSolution) {
        this.stockSolution = stockSolution;
    }

    @Override
    public void run() {
        computeSolutions();
    }

    /**
     *
     * @param solutions
     * @return Number of removed solutions.
     */
    public int removeDuplicated(List<Solution> solutions) {
        int count = 0;
        List<Solution> solutionsToRemove = new ArrayList<>();

        Set<String> set = new HashSet<>();
        for (Solution solution : solutions) {
            String str = solution.getMosaics().get(0).getRootTileNode().toStringIdentifier();
            if (set.add(str) == false) {
                solutionsToRemove.add(solution);
                count++;
            }
        }

        solutions.removeAll(solutionsToRemove);
        return count;
    }


    private void sort(List<Solution> solutions, Configuration cfg) {

        final List<Comparator> solutionComparators = new ArrayList<>();

        List<String> criterias = new ArrayList<>(cfg.getPriorities());

        // Solutions without all fitted tiles will go last
        solutionComparators.add(new SolutionMostNbrTilesComparator());

        for (String priotity : criterias) {
            Comparator comparator = SolutionComparatorFactory.getSolutionComparator(priotity);

            if (comparator != null) {
                solutionComparators.add(comparator);
            }
        }

//        Collections.sort(solutions, (o1, o2) -> {
//
//            int diff = 0;
//
//            for (Comparator<Solution> solutionComparator : solutionComparators) {
//                diff = solutionComparator.compare(o1, o2);
//                if (diff != 0) {
//                    break;
//                }
//            }
//
//            return diff;
//        });

        Collections.sort(solutions, new Comparator<Solution>() {
            @Override
            public int compare(Solution o1, Solution o2) {

                int diff = 0;

                for (Comparator<Solution> solutionComparator : solutionComparators) {
                    diff = solutionComparator.compare(o1, o2);
                    if (diff != 0) {
                        break;
                    }
                }

                return diff;
            }
        });
    }

    void computeSolutions() {

        // Calculate permutation priority based on the number of dimensions change while iterating the tile list
        int permutationPriority = Integer.MAX_VALUE;
        String lastTileDimensions = "";
        for (TileDimensions tile : tiles) {
            if (!tile.dimensionsToString().equals(lastTileDimensions)) {
                permutationPriority--;
                lastTileDimensions = tile.dimensionsToString();
            }
        }

        // Clone the candidate stock solutions
        List<Solution> solutions = new ArrayList<>();
        Solution stockSolutionClone = new Solution(stockSolution);
        stockSolutionClone.setPermutationPriority(permutationPriority);
        solutions.add(stockSolutionClone);

        // Loop through all the titles to be fitted
        for (TileDimensions tile : tiles) {

            List<Solution> newSolutions = new ArrayList<>();
            boolean fitted = false;

            HashMap<String, Integer> depths = new HashMap<>();

            // Loop through all solutions to fit the tiles
            for (Iterator<Solution> iterator = solutions.iterator(); iterator.hasNext(); ) {
                Solution solution = iterator.next();

                for (Mosaic mosaic : solution.getMosaics()) {

                    List<Mosaic> newMosaics = new ArrayList<>();
                    add(tile, mosaic, newMosaics, cfg.getCutThickness(), cfg.getAllowTileRotation());

                    for (Mosaic newMosaic : newMosaics) {
                        Solution newSolution = new Solution(solution, mosaic);  // Copy the solution but exclude the mosaic that will be replaced by the new possibility
                        newSolution.addMosaic(newMosaic);                       // Add the new possibility
                        newSolutions.add(newSolution);                          // Add this new solution to list
                    }

                    if (newMosaics.size() > 0) {
                        fitted = true;
                        break;
                    }
                }

                if (fitted == true) {
                    iterator.remove();
                } else {
                    solution.getNoFitTiles().add(tile);
                }
            }

            solutions.addAll(newSolutions);

            removeDuplicated(solutions);

            List<Solution> solutionsToRemove = new ArrayList<>();
            sort(solutions, cfg);
            solutionsToRemove.addAll(solutions.subList(Math.min(solutions.size() - 1, accuracyFactor/*(int) (accuracyFactor * 500.0f)*/), solutions.size() - 1));

            solutions.removeAll(solutionsToRemove);

            RunningTasks.Task task = runningTasks.getTask(cfg.getTaskId());
            if (task == null) {
                break;
            }
            //task.setSolution((new TilingResponseDTOBuilder()).setSolutions(solutions.get(0)).setInfo(null).build());
        }

        synchronized (allSolutions) {
            allSolutions.addAll(solutions);
            sort(allSolutions, cfg);
            ArrayList<Solution> solutionsToRemove = new ArrayList<>();
            solutionsToRemove.addAll(allSolutions.subList(Math.min(allSolutions.size() - 1, accuracyFactor/*(int) (accuracyFactor * 500.0f)*/), allSolutions.size() - 1));
            allSolutions.removeAll(solutionsToRemove);
        }

        if (solutions.get(0).getNoFitTiles().size() == 0) {
            // TODO: To break or not to break - if not try all permutations and then choose best
            //break;
        }

//        logger.info("Task[{}] Finished permutation[{}] stock[{}] discardAbove[{}] - usedAreaRatio[{}] nbrCuts[{}] maxDepth[{}] nbrNoFitTiles[{}]",
//                cfg.getTaskId(),
////                permutationIndex + 1,
////                tilesPermutations.size(),
//                permutationId,
//                stockSolution,
//                accuracyFactor,
//
//                Math.round(solutions.get(0).getUsedAreaRatio() * 100f) / 100f,
//                solutions.get(0).getNbrCuts(),
//                solutions.get(0).getMaxDepth(),
//                solutions.get(0).getNoFitTiles().size());

        RunningTasks.Task task = runningTasks.getTask(cfg.getTaskId());
        if (task != null) {

            // Remove unused panels from the final solution
            Iterator<Mosaic> iterator = allSolutions.get(0).getMosaics().iterator();
            while (iterator.hasNext()) {
                Mosaic mosaic = iterator.next();
                if (mosaic.getUsedArea() == 0) {
                    iterator.remove();
                }
            }

            task.setSolution((new TilingResponseDTOBuilder()).setSolutions(allSolutions.get(0)).setInfo(null).build());
            task.decrementRunningThreads();
            task.setStatusMessage("Searching for best solution...\nIteration " + permutationId + " on stock " + stockSolution);
        } else {
            //logger.info("Task[{}] permutation[{}] Task was deliberately stopped", cfg.getTaskId(), permutationId);
            return;
        }
    }

    /**
     * Adds the specified tile to the provided root node.
     *
     * @param tileDimensions Dimensions of the tile to be added.
     * @param mosaic The root node to add the tile to.
     * @return The root nodes of every possibility.
     */
    private void add(TileDimensions tileDimensions, Mosaic mosaic, List<Mosaic> possibilities, int cutThickness, boolean allowRotation) {

        // Calculate possibilities with tile as is
        fitTile(tileDimensions, mosaic, possibilities, cutThickness);

        // Consider possibilities after rotating the tile 90º
        if (allowRotation && !tileDimensions.isSquare()) {
            tileDimensions = tileDimensions.rotate90();
            fitTile(tileDimensions, mosaic, possibilities, cutThickness);
        }
    }


    /**
     * Calculate all the possibilities for fitting the specified tile into the root node.
     *
     * @param tileToAdd The dimensions of the tile to be fitted.
     * @param mosaic The root node from witch to work when considering the possibilities for fitting the tile.
     * @return A list of root nodes of all the possibilities for fitting the tile.
     */
    private void fitTile(TileDimensions tileToAdd, Mosaic mosaic, List<Mosaic> possibilities, int cutThickness) {

        List<Cut> newCuts;
        List<TileNode> candidates = new ArrayList<>();

        findCandidates(tileToAdd.getWidth(), tileToAdd.getHeight(), mosaic.getRootTileNode(), candidates);

        for (TileNode candidate : candidates) {

            // No need to split, tile has the exact required dimensions.
            if (candidate.getWidth() == tileToAdd.getWidth() && candidate.getHeight() == tileToAdd.getHeight()) {
                // TODO: Is the copy really needed?
                TileNode possibilitiy = copy(mosaic.getRootTileNode(), candidate);
                TileNode candidateCopy = possibilitiy.findTile(candidate);

                candidateCopy.setExternalId(tileToAdd.getId());
                candidateCopy.setFinal(!tileToAdd.isPlaceHolder());


                Mosaic newMosaic = new Mosaic(possibilitiy);
                newMosaic.getCuts().addAll(mosaic.getCuts());
                possibilities.add(newMosaic);
                continue;
            }

            // Consider possibilities by splitting first horizontally
            TileNode possibilitiy = copy(mosaic.getRootTileNode(), candidate);
            TileNode candidateCopy = possibilitiy.findTile(candidate);

            newCuts = splitHV(candidateCopy, tileToAdd, cutThickness);

            Mosaic newMosaic = new Mosaic(possibilitiy);
            newMosaic.getCuts().addAll(mosaic.getCuts());
            newMosaic.getCuts().addAll(newCuts);
            possibilities.add(newMosaic);

            // Second split result would be the same
            if (candidate.getWidth() == tileToAdd.getWidth() || candidate.getHeight() == tileToAdd.getHeight()) {
                continue;
            }

            // Consider now possibilities by splitting vertically
            TileNode possibilitiy2 = copy(mosaic.getRootTileNode(), candidate);
            TileNode candidateCopy2 = possibilitiy2.findTile(candidate);

            newCuts = splitVH(candidateCopy2, tileToAdd, cutThickness);

            Mosaic newMosaic2 = new Mosaic(possibilitiy2);
            newMosaic2.getCuts().addAll(mosaic.getCuts());
            newMosaic2.getCuts().addAll(newCuts);
            possibilities.add(newMosaic2);
        }
    }


    /**
     * Splits the specified node to fit a tile with specified dimensions.
     * Split horizontally first.
     *
     * @param tileNode The tile node to be splitted.
     */
    private static List<Cut> splitHV(TileNode tileNode, TileDimensions tileDimensions, int cutThickness) {

        List<Cut> cuts = new ArrayList<>();

        // Check if tile needs to be split horizontally.
        if (tileNode.getWidth() > tileDimensions.getWidth()) {
            cuts.add(splitHorizontally(tileNode, tileDimensions.getWidth(), cutThickness));

            // Check if tile needs to be split vertically.
            if (tileNode.getHeight() > tileDimensions.getHeight()) {
                // Vertically split the tile resulting from the horizontal split.
                cuts.add(splitVertically(tileNode.getChild1(), tileDimensions.getHeight(), cutThickness, tileDimensions.getId()));
                // 1st child from vertical split of the 1st child from the horizontal split is the final tile.
                tileNode.getChild1().getChild1().setFinal(!tileDimensions.isPlaceHolder());
            } else {
                // No need to split vertically, the 1st child from horizontal split will be the final tile.
                tileNode.getChild1().setFinal(!tileDimensions.isPlaceHolder());
                tileNode.getChild1().setExternalId(tileDimensions.getId());
            }

        } else {
            // No need to split horizontally, just split vertically and the 1st child will be the final tile.
            cuts.add(splitVertically(tileNode, tileDimensions.getHeight(), cutThickness, tileDimensions.getId()));
            tileNode.getChild1().setFinal(!tileDimensions.isPlaceHolder());
        }

        return cuts;
    }

    /**
     * Splits the specified node to fit a tile with specified dimensions.
     * Split vertically first.
     *
     * @param tileNode The tile node to be splitted.
     */
    private static List<Cut> splitVH(TileNode tileNode, TileDimensions tileDimensions, int cutThickness) {

        List<Cut> cuts = new ArrayList<>();

        // Check if tile needs to be split vertically.
        if (tileNode.getHeight() > tileDimensions.getHeight()) {
            cuts.add(splitVertically(tileNode, tileDimensions.getHeight(), cutThickness));

            // Check if tile needs to be split horizontally.
            if (tileNode.getWidth() > tileDimensions.getWidth()) {
                // Vertically split the two tiles resulting from the horizontal split.
                cuts.add(splitHorizontally(tileNode.getChild1(), tileDimensions.getWidth(), cutThickness, tileDimensions.getId()));
                // 1st child from vertical split of the 1st child from the horizontal split is the final tile.
                tileNode.getChild1().getChild1().setFinal(!tileDimensions.isPlaceHolder());
            } else {
                // No need to split vertically, the 1st child from horizontal split will be the final tile.
                tileNode.getChild1().setFinal(!tileDimensions.isPlaceHolder());
                tileNode.getChild1().setExternalId(tileDimensions.getId());
            }

        } else {
            // No need to split horizontally, just split vertically and the 1st child will be the final tile.
            cuts.add(splitHorizontally(tileNode, tileDimensions.getWidth(), cutThickness, tileDimensions.getId()));
            tileNode.getChild1().setFinal(!tileDimensions.isPlaceHolder());
        }

        return cuts;
    }


    /**
     * Horizontally split the specified tile node.
     * Resulting 1st child will have the specified width.
     *
     * @param tileNode Tile node to split.
     * @param w Desired width for the 1st child.
     */
    private static Cut splitHorizontally(TileNode tileNode, int w, int cutThickness) {
        return splitHorizontally(tileNode, w, cutThickness, 999);
    }

    /**
     * Horizontally split the specified tile node.
     * Resulting 1st child will have the specified width.
     *
     * @param tileNode Tile node to split.
     * @param w Desired width for the 1st child.
     */
    private static Cut splitHorizontally(TileNode tileNode, int w, int cutThickness, int id) {

        int originalWidth = tileNode.getWidth();
        int originalHeight = tileNode.getHeight();

        if (tileNode == null) {
            return null;
        }

        TileNode child1 = new TileNode(
                tileNode.getX1(),
                tileNode.getX1() + w,
                tileNode.getY1(),
                tileNode.getY2());
        child1.setExternalId(id);
        if (child1.getArea() > 0) {
            tileNode.setChild1(child1);
        }

        TileNode child2 = new TileNode(
                tileNode.getX1() + w + cutThickness,
                tileNode.getX2(),
                tileNode.getY1(),
                tileNode.getY2());
        if (child2.getArea() > 0) {
            tileNode.setChild2(child2);
        }

        Cut cut = new Cut.Builder()
                .setX1(tileNode.getX1() + w)
                .setY1(tileNode.getY1())
                .setX2(tileNode.getX1() + w)
                .setY2(tileNode.getY2())
                .setOriginalWidth(originalWidth)
                .setOriginalHeight(originalHeight)
                .setHorizontal(true)
                .setCutCoords(w)
                .setOriginalTileId(tileNode.getId())
                .setChild1TileId(child1.getId())
                .setChild2TileId(child2.getId())
                .build();

        return cut;
    }

    /**
     * Vertically split the specified tile node.
     * Resulting 1st child will have the specified height.
     *
     * @param tileNode Tile node to split.
     * @param h Desired height for the 1st child.
     */
    private static Cut splitVertically(TileNode tileNode, int h, int cutThickness) {
        return splitVertically(tileNode, h, cutThickness, 999);
    }

    /**
     * Vertically split the specified tile node.
     * Resulting 1st child will have the specified height.
     *
     * @param tileNode Tile node to split.
     * @param h Desired height for the 1st child.
     */
    private static Cut splitVertically(TileNode tileNode, int h, int cutThickness, int id) {

        int originalWidth = tileNode.getWidth();
        int originalHeight = tileNode.getHeight();

        if (tileNode == null) {
            return null;
        }

        TileNode child1 = new TileNode(
                tileNode.getX1(),
                tileNode.getX2(),
                tileNode.getY1(),
                tileNode.getY1() + h);
        child1.setExternalId(id);
        if (child1.getArea() > 0) {
            tileNode.setChild1(child1);
        }

        TileNode child2 = new TileNode(
                tileNode.getX1(),
                tileNode.getX2(),
                tileNode.getY1() + h + cutThickness,
                tileNode.getY2());
        if (child2.getArea() > 0) {
            tileNode.setChild2(child2);
        }

        Cut cut = new Cut.Builder()
                .setX1(tileNode.getX1())
                .setY1(tileNode.getY1() + h)
                .setX2(tileNode.getX2())
                .setY2(tileNode.getY1() + h)
                .setOriginalWidth(originalWidth)
                .setOriginalHeight(originalHeight)
                .setHorizontal(true)
                .setCutCoords(h)
                .setOriginalTileId(tileNode.getId())
                .setChild1TileId(child1.getId())
                .setChild2TileId(child2.getId())
                .build();

        return cut;
    }



    /**
     * Finds candidate tiles to fit the tile of the specified dimensions
     *
     * @param w Width of the tile to fit.
     * @param h Height of the tile to fit.
     * @param rootTileNode Root tile node to traverse for finding empty tile candidates.
     * @param candidates List to append the candidate tile nodes.
     */
    private static void findCandidates(int w, int h, TileNode rootTileNode, List<TileNode> candidates) {

        // If no tile or this tile is final, no candidate here.
        if (rootTileNode == null || rootTileNode.isFinal()) {
            return;
        }

        // If required dimensions are not fulfilled by this tile,
        // they will also not be fulfilled by its children, no candidate here.
        if(rootTileNode.getWidth() < w || rootTileNode.getHeight() < h) {
            return;
        }

        if (rootTileNode.getChild1() == null && rootTileNode.getChild2() == null) {
            // This tile has no children, see if it fits for candidate.
            if(rootTileNode.getWidth() >= w && rootTileNode.getHeight() >= h) {
                candidates.add(rootTileNode);
            }
        } else {
            // This tile has children, check recursively if any of them is a possible candidate.
            if (rootTileNode.getChild1() != null) {
                findCandidates(w, h, rootTileNode.getChild1(), candidates);
            }
            if (rootTileNode.getChild2() != null) {
                findCandidates(w, h, rootTileNode.getChild2(), candidates);
            }
        }
    }

    /**
     * Recursively copy specified note until maxLevel.
     * Tile node specified has maximum level will also be included in the copy.
     *
     * @param tileNode
     * @param maxLevel Copy until this node.
     * @return The copy root node.
     */
    private static TileNode copy(TileNode tileNode, TileNode maxLevel) {
        TileNode copy = new TileNode(tileNode);
        copyChildren(tileNode, copy, maxLevel);
        return copy;
    }

    /**
     * Recursively copy node children until maxLevel node, maxLevel included.
     * Root node is not copied.
     *
     * @param src Source tile node.
     * @param dst Destiny tile node.
     * @param maxLevel Copy until this node.
     */
    private static void copyChildren(TileNode src, TileNode dst, TileNode maxLevel) {

        if (src == maxLevel) {
            return;
        }

        if (src.getChild1() != null) {
            dst.setChild1(new TileNode(src.getChild1()));
            copyChildren(src.getChild1(), dst.getChild1(), maxLevel);
        }

        if (src.getChild2() != null) {
            dst.setChild2(new TileNode(src.getChild2()));
            copyChildren(src.getChild2(), dst.getChild2(), maxLevel);
        }
    }
}
