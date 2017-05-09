package com.pedroedrasousa.cutlistoptimizer.comparator;

import com.pedroedrasousa.cutlistoptimizer.model.Solution;

import java.util.Comparator;

public class SolutionMostNbrMosaicsComparator implements Comparator<Solution> {
    @Override
    public int compare(Solution o1, Solution o2) {
        return o2.getMosaics().size() - o1.getMosaics().size();
    }
}
