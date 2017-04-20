package com.pedroedrasousa.tiling;

import com.pedroedrasousa.tiling.comparator.SolutionBiggestUnusedTileAreaComparator;
import com.pedroedrasousa.tiling.comparator.SolutionComparatorFactory;
import com.pedroedrasousa.tiling.comparator.SolutionMostNbrTilesComparator;
import com.pedroedrasousa.tiling.comparator.SolutionSmallestCenterOfMassDistToOriginComparator;
import com.pedroedrasousa.tiling.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.tiles3.TilesConfigurer;

import java.util.*;

@Service
public class CutListService {

    private final static Logger logger = LoggerFactory.getLogger(CutListService.class);

    @Autowired
    private ApplicationContext context;

    @Autowired
    private StockPanelPicker stockPanelPicker;

    @Autowired
    private RunningTasks runningTasks;

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    private void sort(List<Solution> solutions, Configuration cfg, boolean isFinalSort) {

        List<Comparator> solutionComparators = new ArrayList<>();

        List<String> criterias = new ArrayList<>(cfg.getPriorities());

        if (!isFinalSort) {
            criterias.remove("SMALLEST_CENTER_OF_MASS_DIST_TO_ORIGIN");
        }

        // Solutions without all fitted tiles will go last
        solutionComparators.add(new SolutionMostNbrTilesComparator());

        //solutionComparators.add(new SolutionBiggestUnusedTileAreaComparator());

        for (String priotity : criterias) {
            solutionComparators.add(SolutionComparatorFactory.getSolutionComparator(priotity));
        }

        Collections.sort(solutions, (o1, o2) -> {

            int diff = 0;

            for (Comparator<Solution> solutionComparator : solutionComparators) {
                diff = solutionComparator.compare(o1, o2);
                if (diff != 0) {
                    break;
                }
            }

            return diff;
        });
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
            String str = solution.getMosaics().get(0).getRootTileNode().toString();
            if (set.add(str) == false) {
                solutionsToRemove.add(solution);
                count++;
            }
        }

        solutions.removeAll(solutionsToRemove);
        return count;
    }

    void computeSolutions(List<TileDimensions> tiles, List<Solution> solutions, Configuration cfg, int accuracyFactor) {

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

            List<Solution> solutionsToRemove = new ArrayList<>();
            sort(solutions, cfg, false);
            solutionsToRemove.addAll(solutions.subList(Math.min(solutions.size() - 1, accuracyFactor/*(int) (accuracyFactor * 500.0f)*/), solutions.size() - 1));
            solutions.removeAll(solutionsToRemove);

            removeDuplicated(solutions);
        }
    }

    private List<List<TileDimensions>> getPlaceHolders(List<GroupedTileDimensions> tilesToFit) {
        List<TileDimensions> placeholders = new ArrayList<>();
        List<TileDimensions> placeholders2 = new ArrayList<>();

        // Create a list with all distinct tile dimensions
        HashMap<TileDimensions, Integer> distinctTileDimensions = new HashMap<>();
        for (GroupedTileDimensions tileDimensions : tilesToFit) {
            distinctTileDimensions.put(tileDimensions, distinctTileDimensions.get(tileDimensions) != null ? distinctTileDimensions.get(tileDimensions) + 1 : 1);
        }

        // Loop through all distinct tile dimensions and build a placeholder
        for (Map.Entry<TileDimensions, Integer> tileDimensions : distinctTileDimensions.entrySet()) {

            // The number of tiles having the dimensions being considered in current iteration
            int nbrTiles = tileDimensions.getValue();

            // Get the number of horizontal tiles to fit in the placeholder
            int x = (int)Math.sqrt(nbrTiles);

            // Decrement the number of tiles to be fitted in the placeholder until all can be correctly placed
            while ((double)nbrTiles / (double)x % 1.0 != 0.0) {
                nbrTiles--;
            }

            // Get the number of vertical tiles to fit in the placeholder
            int y = nbrTiles / x;

            // Build the placeholder TileDimensions object
            TileDimensions placeholder = new TileDimensions(tileDimensions.getKey().getWidth() * x, tileDimensions.getKey().getHeight() * y, true);
            placeholders.add(placeholder);
            placeholders2.add(new TileDimensions(tileDimensions.getKey().getHeight() * x, tileDimensions.getKey().getWidth() * y, true));

            logger.info("tile: " + tileDimensions);
            logger.info("x= " + x);
            logger.info("y= " + y);
            logger.info("nbrTiles= " + nbrTiles);
            logger.info("placeholder= " + placeholder);


        }

        List<List<TileDimensions>> placeholdersList = new ArrayList<>();
        placeholdersList.add(placeholders);
        placeholdersList.add(placeholders2);

        return placeholdersList;
    }

    /**
     * Recalculates possibilities list.
     * Possibilities list will contain the root nodes of every tree on witch tiles will fit.
     */
    // TODO: should return solution not dto?
    public TillingResponseDTO compute(List<TileDimensions> tilesToFit, List<TileDimensions> stockTiles, Configuration cfg) {

        // Validate if tiles were provided
        if (tilesToFit == null || tilesToFit.size() == 0) {
            TillingResponseDTO tillingResponseDTO = new TillingResponseDTO();
            tillingResponseDTO.setReturnCode("1");
            return tillingResponseDTO;
        }

        // Validate if stock tiles were provided
        if (stockTiles == null || stockTiles.size() == 0) {
            TillingResponseDTO tillingResponseDTO = new TillingResponseDTO();
            tillingResponseDTO.setReturnCode("2");
            return tillingResponseDTO;
        }

        logger.info("Task[" + cfg.getTaskId() + "] STARTING... nbrTilesToFit[" + tilesToFit.size() + "] nbrBaseTiles[" + stockTiles.size() + "]");


        long startTime = System.currentTimeMillis();

        // Create task
        runningTasks.getTasks().add(new RunningTasks.Task(cfg.getTaskId(), "Initializing..."));


        List<Solution> allSolutions = new ArrayList<>();

        // Create a list with all distinct tile dimensions
        HashMap<String, Integer> distincTileDimensions = new HashMap<>();
        for (TileDimensions tileDimensions : tilesToFit) {
            String tileDimensionsStr = tileDimensions.toString();
            distincTileDimensions.put(tileDimensionsStr, distincTileDimensions.get(tileDimensionsStr) != null ? distincTileDimensions.get(tileDimensionsStr) + 1 : 1);
        }

        StringBuilder sb = new StringBuilder();
        for (String tileDimensions : distincTileDimensions.keySet()) {
            sb.append(tileDimensions + "*" + distincTileDimensions.get(tileDimensions) + " ");
        }
        logger.info("Task[{}] TilesToFit: {}", cfg.getTaskId(), sb);






        List<GroupedTileDimensions> g = new ArrayList<>();
        HashMap<String, Integer> map = new HashMap<>();
        int groupNbr = 0;
        for (TileDimensions tileDimensions : tilesToFit) {
            map.put(tileDimensions.toString() + groupNbr, map.get(tileDimensions.toString() + groupNbr) != null ? map.get(tileDimensions.toString() + groupNbr) + 1 : 1);
            GroupedTileDimensions groupedTileDimensions = new GroupedTileDimensions(tileDimensions, groupNbr);
            g.add(groupedTileDimensions);
            if (map.get(tileDimensions.toString() + groupNbr) > distincTileDimensions.get(tileDimensions.toString()) / 2) {
                groupNbr++;
            }
        }

        // Create a list with all distinct tile dimensions
        HashMap<GroupedTileDimensions, Integer> distincGroupTileDimensions = new HashMap<>();
        for (GroupedTileDimensions tileDimensions : g) {
            //String tileDimensionsStr = tileDimensions.toString();
            distincGroupTileDimensions.put(tileDimensions, distincGroupTileDimensions.get(tileDimensions) != null ? distincGroupTileDimensions.get(tileDimensions) + 1 : 1);
        }

















        // Create a list with all distinct stock tile dimensions
        HashMap<String, Integer> distincStockTileDimensions = new HashMap<>();
        for (TileDimensions tileDimensions : stockTiles) {
            String tileDimensionsStr = tileDimensions.toString();
            distincStockTileDimensions.put(tileDimensionsStr, distincStockTileDimensions.get(tileDimensionsStr) != null ? distincStockTileDimensions.get(tileDimensionsStr) + 1 : 1);
        }

        sb.setLength(0);
        for (String tileDimensions : distincStockTileDimensions.keySet()) {
            sb.append(tileDimensions + "*" + distincStockTileDimensions.get(tileDimensions) + " ");
        }
        logger.info("Task[{}] StockTiles: {}", cfg.getTaskId(), sb);




        List<List<TileDimensions>> placeHolders = getPlaceHolders(g);




        // Get all possible combinations by permuting the order in witch the tiles are fited
        List<List<GroupedTileDimensions>> combinations = Permutation.<GroupedTileDimensions>generatePermutations(new ArrayList<>(distincGroupTileDimensions.keySet()));





        // Create lists sorted according to the calculated permutations
        List<List<TileDimensions>> tilesPermutations = new ArrayList<>();
        for (List<GroupedTileDimensions> combination : combinations) {
            ArrayList<TileDimensions> solutionPermutation = new ArrayList<>(g);
            tilesPermutations.add(solutionPermutation);
            Collections.sort(solutionPermutation, Comparator.comparingInt(o -> combination.indexOf(o)));

            for (List<TileDimensions> placeholders : placeHolders) {
                // Build additional permutations foreach placeholder
                for (TileDimensions tileDimensions : placeholders) {
                    ArrayList<TileDimensions> solutionPermutationPlaceholders = new ArrayList<>(solutionPermutation);
                    solutionPermutationPlaceholders.add(0, tileDimensions);
                    tilesPermutations.add(solutionPermutationPlaceholders);
                }
//
//                // Build an additional permutation containing all placeholders
//                if (placeholders.size() > 1) {
//                    ArrayList<TileDimensions> solutionPermutationPlaceholders = new ArrayList<>(solutionPermutation);
//                    solutionPermutationPlaceholders.addAll(0, placeholders);
//                    tilesPermutations.add(solutionPermutationPlaceholders);
//                }
            }}




        List<String> everyPermutation = new ArrayList<>();
        for (Iterator<List<TileDimensions>> iterator = tilesPermutations.iterator(); iterator.hasNext(); ) {
            List<TileDimensions> permutation = iterator.next();
            StringBuilder sb1 = new StringBuilder();
            for (TileDimensions tileDimensions : permutation) {
                sb1.append(tileDimensions.dimensionsToString());
            }

            if (everyPermutation.contains(sb1.toString())) {
                iterator.remove();
            } else {
                everyPermutation.add(sb1.toString());
            }
        }


        // Log permutations
        int permutationIndex = 0;
        for (List<TileDimensions> permutation : tilesPermutations) {
            permutationIndex++;
            sb.setLength(0);
            sb.append("Permutation " + permutationIndex + "/" + tilesPermutations.size() + ":");
            for (TileDimensions tileDimensions : permutation) {
                sb.append(" " + tileDimensions);
            }
            logger.info(sb.toString());
        }

















        List<StockSolution> stockSolutionsToExclude = new ArrayList<>();

        int spare = 0;
        int startWith = 1;

        // Calculate the required area for fitting every tile.
        int requiredArea = 0;
        for (TileDimensions tile : tilesToFit) {
            requiredArea += tile.getArea();
        }

        boolean done = false;
        while (!done) {

            List<StockSolution> stockSolution = new ArrayList<>();
            StockSolution tmpStockSolution;

            float usedArea2 = 1f;

            while (usedArea2 > 0.8) {
                tmpStockSolution = stockPanelPicker.getCandidateStockSolutions(tilesToFit, stockTiles, 0f, spare, stockSolutionsToExclude, startWith);
                stockSolution.add(tmpStockSolution);
                stockSolutionsToExclude.add(tmpStockSolution);
                usedArea2 = (float) requiredArea / (float) tmpStockSolution.getArea();
                logger.info("Task[{}] Candidate stock {} usedArea[{}]", cfg.getTaskId(), tmpStockSolution, usedArea2);
            }

//            tmpStockSolution = stockPanelPicker.getCandidateStockSolutions(tilesToFit, stockTiles, 0f, spare, stockSolutionsToExclude, startWith);
//            stockSolution.add(tmpStockSolution);
//            stockSolutionsToExclude.add(tmpStockSolution);
//            float usedArea2 = (float) requiredArea / (float) tmpStockSolution.getArea();
//            logger.info("Task[{}] Trying stock {} usedArea[{}]", cfg.getTaskId(), stockSolution, usedArea2);
//            if ((float) requiredArea / (float) tmpStockSolution.getArea() > 0.8) {
//                tmpStockSolution = stockPanelPicker.getCandidateStockSolutions(tilesToFit, stockTiles, 0f, spare, stockSolutionsToExclude, startWith);
//                stockSolution.add(tmpStockSolution);
//                stockSolutionsToExclude.add(tmpStockSolution);
//                usedArea2 = (float) requiredArea / (float) tmpStockSolution.getArea();
//                logger.info("Task[{}] Trying stock {} usedArea[{}]", cfg.getTaskId(), stockSolution, usedArea2);
//            }


//            tmpStockSolution = stockPanelPicker.getCandidateStockSolutions(tilesToFit, stockTiles, 0f, spare + 1, stockSolutionsToExclude, startWith);
//            stockSolution.add(tmpStockSolution);
//            stockSolutionsToExclude.add(tmpStockSolution);
//            if ((float) requiredArea / (float) tmpStockSolution.getArea() > 0.8) {
//                tmpStockSolution = stockPanelPicker.getCandidateStockSolutions(tilesToFit, stockTiles, 0f, spare, stockSolutionsToExclude, startWith);
//                stockSolution.add(tmpStockSolution);
//                stockSolutionsToExclude.add(tmpStockSolution);
//            }

//            if (stockSolution == null) {
//                spare++;
//                stockSolution = stockPanelPicker.getCandidateStockSolutions(tilesToFit, stockTiles, 0f, spare, stockSolutionsToExclude, startWith);
//
//                if (stockSolution == null) {
//                    // No more possible stock solutions
//                    logger.info("Couldn't find a suitable solution", stockSolution);
//                    break;
//                }
//            }
            //

            //stockSolutionsToExclude.add(stockSolution2);
            // Iterate through all permutations
            permutationIndex = -1;
            for (List<TileDimensions> tilesPermutation : tilesPermutations) {
                permutationIndex++;

                RunningTasks.Task task2 = runningTasks.getTask(cfg.getTaskId());
                if (task2 != null) {
                    task2.setStatusMessage("Trying permutation " + (permutationIndex + 1) + "/" + tilesPermutations.size() + " on stock " + stockSolution);
                }
                for (StockSolution stockSolution1 : stockSolution) {

                    float usedArea = (float) requiredArea / (float) stockSolution1.getArea();
                    int discardAbove = (int) (500.0f * Math.pow(usedArea, 3.0f));
                    discardAbove = Math.max(discardAbove, 100);

                    // TODO: Only for debug purposes
                    if (cfg.getAccuracyFactor() > 0) {
                        discardAbove = cfg.getAccuracyFactor();
                    }
                    logger.info("Task[{}] Trying stock {} usedArea[{}] discardAbove[{}]", cfg.getTaskId(), stockSolution1, usedArea, discardAbove);




                    //computeSolutions(tilesPermutation, solutions, cfg, discardAbove);


                    CutListThread cutListThread = (CutListThread) context.getBean("cutListThread");
                    cutListThread.setName("aaa");
                    cutListThread.setAllSolutions(allSolutions);
                    cutListThread.setTiles(tilesPermutation);
//                cutListThread.setSolutions(solutions);
                    cutListThread.setCfg(cfg);
                    cutListThread.setAccuracyFactor(discardAbove);
                    cutListThread.setStockSolution(stockSolution1);
                    taskExecutor.execute(cutListThread);


                }
            }



            for (;;) {
//                    System.out.println("Active Threads : " + taskExecutor.getActiveCount());
//                    System.out.println("Pool size: " + taskExecutor.getPoolSize());
//                    System.out.println("getTaskCount: " + taskExecutor.getThreadPoolExecutor().getTaskCount());
//                    System.out.println("remainingCapacity: " + taskExecutor.getThreadPoolExecutor().getQueue().remainingCapacity());

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (taskExecutor.getActiveCount() == 0) {
                    //taskExecutor.shutdown();
                    sort(allSolutions, cfg, true);
                    break;
                }
            }



            //sort(allSolutions, cfg, true);

//            if (runningTasks.getTask(cfg.getTaskId()) == null || (allSolutions.get(0).getNoFitTiles().size() == 0 && startWith > 3)) {
//                break;
//            }
//            else if(allSolutions.get(0).getNoFitTiles().size() == 0 && !cfg.getForceOneBaseTile()) {
//                startWith++;
//            }

//            if (runningTasks.getTask(cfg.getTaskId()) == null || startWith > 3) {
//                break;
//            }
//            startWith++;

            if (allSolutions.get(0).getNoFitTiles().size() == 0) {
                break;
            }
        }




        long elapsedTime = System.currentTimeMillis() - startTime;


        logger.info("Task[{}] Elapsed time: {} ms", cfg.getTaskId(), elapsedTime);
        allSolutions.get(0).setElapsedTime(elapsedTime);

        runningTasks.removeTask(cfg.getTaskId());

        return (new TilingResponseDTOBuilder()).setSolutions(allSolutions.get(0)).setInfo(null).build();
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
            // TODO:
            //if (src.getChild1() instanceof ImmutableTileNode) {
            //    dst.setChild1(src.getChild1());
            //}else {
                dst.setChild1(new TileNode(src.getChild1()));
            //}
            copyChildren(src.getChild1(), dst.getChild1(), maxLevel);
        }

        if (src.getChild2() != null) {
            dst.setChild2(new TileNode(src.getChild2()));
            copyChildren(src.getChild2(), dst.getChild2(), maxLevel);
        }
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
}
