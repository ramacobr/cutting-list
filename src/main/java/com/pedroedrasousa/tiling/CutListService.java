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

    /**
     *
     * @param permutations
     * @return Number of removed permutations.
     */
    private int removeDuplicatedPermutations(List<List<TileDimensions>> permutations) {

        int count = 0;

        List<String> distinctPermutations = new ArrayList<>();
        for (Iterator<List<TileDimensions>> iterator = permutations.iterator(); iterator.hasNext(); ) {
            List<TileDimensions> permutation = iterator.next();

            // Build a string based on tile dimensions to represent the permutation
            StringBuilder sb = new StringBuilder();
            for (TileDimensions tileDimensions : permutation) {
                sb.append(tileDimensions.dimensionsToString());
            }

            // Remove this permutation if not distinct from previous ones,
            // else add it to the distinct list.
            if (distinctPermutations.contains(sb.toString())) {
                iterator.remove();
                count++;
            } else {
                distinctPermutations.add(sb.toString());
            }
        }

        return count;
    }

    private List<GroupedTileDimensions> generateGroups(List<TileDimensions> tilesToFit, Configuration cfg) {

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
        logger.info("Task[{}] TotalNbrTiles[{}] Tiles: {}", cfg.getTaskId(), tilesToFit.size(), sb);

        List<GroupedTileDimensions> groups = new ArrayList<>();
        HashMap<String, Integer> map = new HashMap<>();
        int groupNbr = 0;
        for (TileDimensions tileDimensions : tilesToFit) {

            String groupId = tileDimensions.toString() + groupNbr;

            map.put(groupId, map.get(groupId) != null ? map.get(groupId) + 1 : 1);
            GroupedTileDimensions groupedTileDimensions = new GroupedTileDimensions(tileDimensions, groupNbr);
            groups.add(groupedTileDimensions);
            if (groupNbr + distincTileDimensions.size() < 5 &&
                    distincTileDimensions.get(tileDimensions.toString()) > 5 && // Only split in groups if the quantity justifies it
                    map.get(groupId) > distincTileDimensions.get(tileDimensions.toString()) / 2) {
                groupNbr++;
            }
        }

        return groups;
    }

    private <T> HashMap<T, Integer> getDistinctGroupedTileDimensions(List<T> groups, Configuration cfg) {
        StringBuilder sb = new StringBuilder();

        // Create a list with all distinct groups
        HashMap<T, Integer> distincGroupTileDimensions = new HashMap<>();
        for (T tileDimensions : groups) {
            //String tileDimensionsStr = tileDimensions.toString();
            distincGroupTileDimensions.put(tileDimensions, distincGroupTileDimensions.get(tileDimensions) != null ? distincGroupTileDimensions.get(tileDimensions) + 1 : 1);
        }

        return distincGroupTileDimensions;
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

            // TODO: delete
//            logger.info("tile: " + tileDimensions);
//            logger.info("x= " + x);
//            logger.info("y= " + y);
//            logger.info("nbrTiles= " + nbrTiles);
//            logger.info("placeholder= " + placeholder);


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

        logger.info("STARTING... " + cfg.toString());

        long startTime = System.currentTimeMillis();

        // Create task
        runningTasks.getTasks().add(new RunningTasks.Task(cfg.getTaskId()));

        List<Solution> allSolutions = new ArrayList<>();


        StringBuilder sb = new StringBuilder();


        // Log stock tile dimensions
        HashMap<TileDimensions, Integer> distincStockTileDimensions = getDistinctGroupedTileDimensions(stockTiles, cfg);

        sb.setLength(0);
        for (TileDimensions tileDimensions : distincStockTileDimensions.keySet()) {
            sb.append(tileDimensions + "*" + distincStockTileDimensions.get(tileDimensions) + " ");
        }
        logger.info("Task[{}] TotalNbrStockTiles[{}] StockTiles: {}", cfg.getTaskId(), stockTiles.size(), sb);




        List<GroupedTileDimensions> groups = generateGroups(tilesToFit, cfg);

        HashMap<GroupedTileDimensions, Integer> distincGroupTileDimensions = getDistinctGroupedTileDimensions(groups, cfg);
                // Log groups
        int groupIdx = 0;
        sb.setLength(0);
        for (Map.Entry<GroupedTileDimensions, Integer> entry : distincGroupTileDimensions.entrySet()) {
            groupIdx++;
            sb.append(" group[" + groupIdx + "/" + distincGroupTileDimensions.size() + ":" + entry.getKey().toBaseTileDimensions() + "*" + entry.getValue() + "]");
        }
        logger.info("Task[" + cfg.getTaskId() + "]" + sb.toString());




        List<List<TileDimensions>> placeHolders = getPlaceHolders(groups);




        // Get all possible combinations by permuting the order in witch the tiles are fited
        List<List<GroupedTileDimensions>> permutations = Permutation.<GroupedTileDimensions>generatePermutations(new ArrayList<>(distincGroupTileDimensions.keySet()));





        // Create lists sorted according to the calculated permutations
        List<List<TileDimensions>> tilesPermutations = new ArrayList<>();
        for (List<GroupedTileDimensions> combination : permutations) {
            ArrayList<TileDimensions> solutionPermutation = new ArrayList<>(groups);
            tilesPermutations.add(solutionPermutation);
            Collections.sort(solutionPermutation, Comparator.comparingInt(o -> combination.indexOf(o)));

            for (List<TileDimensions> placeholders : placeHolders) {
//                // Build additional permutations foreach placeholder
//                for (TileDimensions tileDimensions : placeholders) {
//                    ArrayList<TileDimensions> solutionPermutationPlaceholders = new ArrayList<>(solutionPermutation);
//                    solutionPermutationPlaceholders.add(0, tileDimensions);
//                    tilesPermutations.add(solutionPermutationPlaceholders);
//                }
//
//                // Build an additional permutation containing all placeholders
//                if (placeholders.size() > 1) {
//                    ArrayList<TileDimensions> solutionPermutationPlaceholders = new ArrayList<>(solutionPermutation);
//                    solutionPermutationPlaceholders.addAll(0, placeholders);
//                    tilesPermutations.add(solutionPermutationPlaceholders);
//                }
            }
        }


        removeDuplicatedPermutations(tilesPermutations);



        // Log permutations
        int permutationIndex = 0;
        for (List<TileDimensions> permutation : tilesPermutations) {
            permutationIndex++;
            sb.setLength(0);
            sb.append("Task[" + cfg.getTaskId() + "] Permutation " + permutationIndex + "/" + tilesPermutations.size() + ":");
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
                tmpStockSolution = stockPanelPicker.getCandidateStockSolutions(tilesToFit, stockTiles, 0f, spare, stockSolutionsToExclude, startWith, cfg.getForceOneBaseTile() == true ? 1 : Integer.MAX_VALUE);
                if (tmpStockSolution == null) {
                    break;
                }
                stockSolution.add(tmpStockSolution);
                stockSolutionsToExclude.add(tmpStockSolution);
                usedArea2 = (float) requiredArea / (float) tmpStockSolution.getArea();
                logger.info("Task[{}] Candidate stock {} usedArea[{}]", cfg.getTaskId(), tmpStockSolution, usedArea2);
            }

            if (stockSolution.size() == 0) {
                logger.info("Task[{}] No more possible stock solutions", cfg.getTaskId());
                done = true;
            }

            // Iterate through all permutations
            int nbrTotalThreads = 0;
            permutationIndex = -1;
            for (List<TileDimensions> tilesPermutation : tilesPermutations) {
                permutationIndex++;

                for (StockSolution stockSolution1 : stockSolution) {

                    float usedArea = (float) requiredArea / (float) stockSolution1.getArea();
                    int discardAbove = (int) (500.0f * Math.pow(usedArea, 3.0f));
                    discardAbove = Math.max(discardAbove, 100);

                    // TODO: Only for debug purposes
                    if (cfg.getAccuracyFactor() > 0) {
                        discardAbove = cfg.getAccuracyFactor();
                    }

                    CutListThread cutListThread = (CutListThread) context.getBean("cutListThread");
                    cutListThread.setPermutationId((permutationIndex + 1) + "/" + tilesPermutations.size());
                    cutListThread.setAllSolutions(allSolutions);
                    cutListThread.setTiles(tilesPermutation);
                    cutListThread.setCfg(cfg);
                    cutListThread.setAccuracyFactor(discardAbove);
                    cutListThread.setStockSolution(stockSolution1);
                    taskExecutor.execute(cutListThread);
                    nbrTotalThreads++;
                    RunningTasks.Task task2 = runningTasks.getTask(cfg.getTaskId());
                    if (task2 != null) {
                        task2.incrementRunningThreads();
                        task2.incrementNbrTotalThreads();
                    }
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

                    // If a good solution was found, we're done.
                    if (allSolutions.get(0).getNoFitTiles().size() == 0) {
                        done = true;
                    }
                    break;
                }
            }

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
}
