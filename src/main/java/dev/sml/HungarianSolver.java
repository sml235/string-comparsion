package dev.sml;

import java.util.ArrayList;

public class HungarianSolver {
    private int numRows;
    private int numCols;

    private boolean[][] primes;
    private boolean[][] stars;
    private boolean[] rowsCovered;
    private boolean[] colsCovered;
    private double[][] costs;

    public HungarianSolver(double theCosts[][]) {
        costs = theCosts;
        numRows = costs.length;
        numCols = costs[0].length;
        primes = new boolean[numRows][numCols];
        stars = new boolean[numRows][numCols];
        rowsCovered = new boolean[numRows];
        colsCovered = new boolean[numCols];

        for (int i = 0; i < numRows; i++) {
            rowsCovered[i] = false;
        }
        for (int j = 0; j < numCols; j++) {
            colsCovered[j] = false;
        }
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                primes[i][j] = false;
                stars[i][j] = false;
            }
        }
    }

    public int[][] execute() {
        subtractRowColMins();
        this.findStars();
        this.resetCovered();
        this.coverStarredZeroCols();
        while (!allColsCovered()) {
            int[] primedLocation = this.primeUncoveredZero();
            if (primedLocation[0] == -1) {
                this.minUncoveredRowsCols();
                primedLocation = this.primeUncoveredZero();
            }
            int primedRow = primedLocation[0];
            int starCol = this.findStarColInRow(primedRow);
            if (starCol != -1) {
                rowsCovered[primedRow] = true;
                colsCovered[starCol] = false;
            } else {
                this.augmentPathStartingAtPrime(primedLocation);
                this.resetCovered();
                this.resetPrimes();
                this.coverStarredZeroCols();
            }
        }
        return this.starsToAssignments();
    }

    public int[][] starsToAssignments() {
        int[][] toRet = new int[numCols][];
        for (int j = 0; j < numCols; j++) {
            toRet[j] = new int[]{
                    this.findStarRowInCol(j), j
            };
        }
        return toRet;
    }

    public void resetPrimes() {
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                primes[i][j] = false;
            }
        }
    }

    public void resetCovered() {
        for (int i = 0; i < numRows; i++) {
            rowsCovered[i] = false;
        }
        for (int j = 0; j < numCols; j++) {
            colsCovered[j] = false;
        }
    }

    public void findStars() {
        boolean[] rowStars = new boolean[numRows];
        boolean[] colStars = new boolean[numCols];

        for (int i = 0; i < numRows; i++) {
            rowStars[i] = false;
        }
        for (int j = 0; j < numCols; j++) {
            colStars[j] = false;
        }
        for (int j = 0; j < numCols; j++) {
            for (int i = 0; i < numRows; i++) {
                if (costs[i][j] == 0 && !rowStars[i] && !colStars[j]) {
                    stars[i][j] = true;
                    rowStars[i] = true;
                    colStars[j] = true;
                    break;
                }
            }
        }
    }

    private void minUncoveredRowsCols() {
        double minUncovered = Double.MAX_VALUE;
        for (int i = 0; i < numRows; i++) {
            if (!rowsCovered[i]) {
                for (int j = 0; j < numCols; j++) {
                    if (!colsCovered[j]) {
                        if (costs[i][j] < minUncovered) {
                            minUncovered = costs[i][j];
                        }
                    }
                }
            }
        }
        for (int i = 0; i < numRows; i++) {
            if (rowsCovered[i]) {
                for (int j = 0; j < numCols; j++) {
                    costs[i][j] = costs[i][j] + minUncovered;

                }
            }
        }
        for (int j = 0; j < numCols; j++) {
            if (!colsCovered[j]) {
                for (int i = 0; i < numRows; i++) {
                    costs[i][j] = costs[i][j] - minUncovered;
                }
            }
        }
    }

    private int[] primeUncoveredZero() {
        int[] location = new int[2];

        for (int i = 0; i < numRows; i++) {
            if (!rowsCovered[i]) {
                for (int j = 0; j < numCols; j++) {
                    if (!colsCovered[j]) {
                        if (costs[i][j] == 0) {
                            primes[i][j] = true;
                            location[0] = i;
                            location[1] = j;
                            return location;
                        }
                    }
                }
            }
        }

        location[0] = -1;
        location[1] = -1;
        return location;
    }

    private void augmentPathStartingAtPrime(int[] location) {
        ArrayList<int[]> primeLocations = new ArrayList<int[]>(numRows + numCols);
        ArrayList<int[]> starLocations = new ArrayList<int[]>(numRows + numCols);
        primeLocations.add(location);

        int currentRow = location[0];
        int currentCol = location[1];
        while (true) {
            int starRow = findStarRowInCol(currentCol);
            if (starRow == -1) {
                break;
            }
            int[] starLocation = new int[]{
                    starRow, currentCol
            };
            starLocations.add(starLocation);
            currentRow = starRow;

            int primeCol = findPrimeColInRow(currentRow);
            int[] primeLocation = new int[]{
                    currentRow, primeCol
            };
            primeLocations.add(primeLocation);
            currentCol = primeCol;
        }

        unStarLocations(starLocations);
        starLocations(primeLocations);
    }


    private void starLocations(ArrayList<int[]> locations) {
        for (int k = 0; k < locations.size(); k++) {
            int[] location = locations.get(k);
            int row = location[0];
            int col = location[1];
            stars[row][col] = true;
        }
    }


    private void unStarLocations(ArrayList<int[]> starLocations) {
        for (int k = 0; k < starLocations.size(); k++) {
            int[] starLocation = starLocations.get(k);
            int row = starLocation[0];
            int col = starLocation[1];
            stars[row][col] = false;
        }
    }


    private int findPrimeColInRow(int theRow) {
        for (int j = 0; j < numCols; j++) {
            if (primes[theRow][j]) {
                return j;
            }
        }
        return -1;
    }


    public int findStarRowInCol(int theCol) {
        for (int i = 0; i < numRows; i++) {
            if (stars[i][theCol]) {
                return i;
            }
        }
        return -1;
    }


    public int findStarColInRow(int theRow) {
        for (int j = 0; j < numCols; j++) {
            if (stars[theRow][j]) {
                return j;
            }
        }
        return -1;
    }

    private boolean allColsCovered() {
        for (int j = 0; j < numCols; j++) {
            if (!colsCovered[j]) {
                return false;
            }
        }
        return true;
    }

    private void coverStarredZeroCols() {
        for (int j = 0; j < numCols; j++) {
            colsCovered[j] = false;
            for (int i = 0; i < numRows; i++) {
                if (stars[i][j]) {
                    colsCovered[j] = true;
                    break;
                }
            }
        }
    }

    private void subtractRowColMins() {
        for (int i = 0; i < numRows; i++) {
            double rowMin = Double.MAX_VALUE;
            for (int j = 0; j < numCols; j++) {
                if (costs[i][j] < rowMin) {
                    rowMin = costs[i][j];
                }
            }
            for (int j = 0; j < numCols; j++) {
                costs[i][j] = costs[i][j] - rowMin;
            }
        }

        for (int j = 0; j < numCols; j++) {
            double colMin = Double.MAX_VALUE;
            for (int i = 0; i < numRows; i++) {
                if (costs[i][j] < colMin) {
                    colMin = costs[i][j];
                }
            }
            for (int i = 0; i < numRows; i++) {
                costs[i][j] = costs[i][j] - colMin;
            }
        }
    }

}
