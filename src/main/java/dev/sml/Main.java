package org.example;

import org.apache.commons.text.similarity.JaroWinklerSimilarity;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;

public class Main {
    private static final ResourceBundle CONFIG = ResourceBundle.getBundle("config");
    public static final String INPUT =  "input.txt";
    public static final String OUTPUT = "output.txt";
    public static final Path CATALOG_IN = Path.of(CONFIG.getString("catalog.in"),INPUT);
    public static final Path CATALOG_OUT = Path.of(CONFIG.getString("catalog.out"),OUTPUT);


    public static void main(String[] args) {

        try {
            List<String> lines = Files.readAllLines(CATALOG_IN, StandardCharsets.UTF_8);
            int n = Integer.parseInt(lines.get(0));
            List<String> firstGroup = lines.subList(1, 1 + n);
            int m = Integer.parseInt(lines.get(n + 1));
            List<String> secondGroup = lines.subList(n + 2, n + m + 2);
            JaroWinklerSimilarity jaroWinklerSimilarity = new JaroWinklerSimilarity();
            int maxSize = Integer.max(n, m);
            double[][] costs = new double[maxSize][maxSize];
            for (int i = 0; i < maxSize; i++) {
                for (int j = 0; j < maxSize; j++) {
                    String firstWord = null;
                    if (i < firstGroup.size()) {
                        firstWord = firstGroup.get(i);
                    }
                    String secondWord = null;
                    if (j < secondGroup.size()) {
                        secondWord = secondGroup.get(j);
                    }
                    double similarityJW;
                    if (firstWord == null || secondWord == null) {
                        similarityJW = 0.0;
                    } else {
                        similarityJW = jaroWinklerSimilarity.apply(firstWord, secondWord);
                    }
                    costs[i][j] = similarityJW;
                }
            }

            for (int i = 0; i < costs.length; i++) {
                double currentRowMax = 0;
                for (int j = 0; j < costs[i].length; j++) {
                    if (costs[i][j] > currentRowMax) {
                        currentRowMax = costs[i][j];
                    }
                }
                for (int k = 0; k < costs[i].length; k++) {
                    costs[i][k] -= currentRowMax;
                    costs[i][k] *= -1;
                }
            }

            HungarianSolver hungarianSolver = new HungarianSolver(costs);
            int[][] ints = hungarianSolver.execute();
            for (int i = 0; i < ints.length; i++) {
                String first = "?";
                String sec = "?";
                if (ints[i][0] < firstGroup.size()) {
                    first = firstGroup.get(ints[i][0]);
                }
                if (ints[i][1] < secondGroup.size()) {
                    sec = secondGroup.get(ints[i][1]);
                }
                System.out.println(first + " : " + sec);
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }


    }


}