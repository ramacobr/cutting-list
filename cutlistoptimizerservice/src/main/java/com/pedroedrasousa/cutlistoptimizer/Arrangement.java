package com.pedroedrasousa.cutlistoptimizer;

import java.util.ArrayList;
import java.util.List;

public class Arrangement {

    public static <T> List<List<T>> generatePermutations(List<T> original) {
        if (original.size() == 0) {
            List<List<T>> result = new ArrayList<>();
            result.add(new ArrayList<T>());
            return result;
        }

        T firstElement = original.remove(0);
        List<List<T>> returnValue = new ArrayList<>();
        List<List<T>> permutations = generatePermutations(original);
        for (List<T> smallerPermutated : permutations) {
            for (int i = 0; i <= smallerPermutated.size(); i++) {
                List<T> temp = new ArrayList<T>(smallerPermutated);
                temp.add(i, firstElement);
                returnValue.add(temp);
            }
        }

        return returnValue;
    }
}
