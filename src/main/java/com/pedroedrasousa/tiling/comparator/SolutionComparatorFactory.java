package com.pedroedrasousa.tiling.comparator;

import com.pedroedrasousa.tiling.model.Solution;

import java.util.Comparator;

public class SolutionComparatorFactory {

    public static Comparator<Solution> getSolutionComparator(String comparatorType){
        if(comparatorType == null){
            return null;
        }
        if(comparatorType.equalsIgnoreCase("LEAST_WASTED_AREA")) {
            return new SolutionLeastWastedAreaComparator();
        } else if(comparatorType.equalsIgnoreCase("MOST_NBR_MOSAICS")) {
            return new SolutionMostNbrMosaicsComparator();
        } else if(comparatorType.equalsIgnoreCase("MOST_HV_DISCREPANCY")) {
            return new SolutionMostHVDiscrepancyComparator();
        } else if(comparatorType.equalsIgnoreCase("LEAST_NBR_UNUSED_TILES")) {
            return new SolutionLeastNbrUnusedTilesComparator();
        } else if(comparatorType.equalsIgnoreCase("LEAST_NBR_CUTS")) {
            return new SolutionLeastNbrCutsComparator();
        } else if(comparatorType.equalsIgnoreCase("MOST_UNUSED_PANEL_AREA")) {
            return new SolutionMostUnusedPanelAreaComparator();
        } else if(comparatorType.equalsIgnoreCase("SMALLEST_CENTER_OF_MASS_DIST_TO_ORIGIN")) {
            return new SolutionSmallestCenterOfMassDistToOriginComparator();
        } else if(comparatorType.equalsIgnoreCase("BIGGEST_UNUSED_TILE_AREA")) {
            return new SolutionBiggestUnusedTileAreaComparator();
        }

        return null;
    }
}
