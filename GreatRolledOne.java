
import java.util.Arrays;

public class GreatRolledOne {
	int goal;
	Double[][] pRollOutcome; // [num_dice_rolled][num_ones_rolled]
	double[][] pExceed; // [score_diff][num_ones_rolled]
	Boolean[][][] isRoll; //[curr_score][oppo_score][turn_total]
	double[][][][] pWin;	// [curr_score][oppo_score][turn_total][num_ones_rolled]
	double epsilon;
	int maxScore; //score that player 1 will not exceed
	final int NUM_DICE = 5; 
	final int COEFFICENT = 2;	

	/**
	 * Constructor of GreatRolledOne class
	 * @param goal
	 * @param epsilon
	 */
	public GreatRolledOne(int goal, double epsilon) {
		this.goal = goal;
		this.maxScore = COEFFICENT * goal;
		this.epsilon = epsilon;
		isRoll = new Boolean[goal][goal][goal];
		pExceed = new double[maxScore + 1][3];
		pRollOutcome = new Double[NUM_DICE + 1][NUM_DICE + 1];
		pWin = new double[goal][goal][goal][3 + 1];
		//compute winning probabilities
		pOneRolled();
		computepExceed();
	}

	/**
	 * Calculate probability that one is rolled
	 */
	private void pOneRolled() {
		// initialize pRollOutcome with 1.0
		Arrays.fill(pRollOutcome[0], 1.0);
		for (int dice = 1; dice <= NUM_DICE; dice++) {
			for (int one = 0; one <= dice; one++) {
				rollDice(dice, one);
			}
		}
		for (Double[] outcome : pRollOutcome) {
			System.out.println(Arrays.toString(outcome));
		}
	}

	/**
	 * Compute probabilities of rolling ones given num_dice_roll. For reference, check Fig.1 in README
	 * @param diceLeft: number of dice left
	 * @param numOnes: number of ones rolled
	 * @return probability
	 */
	private double rollDice(int diceLeft, int numOnes) {
		// Base Case 
		if (diceLeft == 0 && numOnes == 0) {
			pRollOutcome[diceLeft][numOnes] = 1.0;
		}
		// Case:
		if (pRollOutcome[diceLeft][numOnes] == null) {
			pRollOutcome[diceLeft][numOnes] = 0.0;
			// Case: if one is rolled at least once
			if (numOnes > 0) {
				pRollOutcome[diceLeft][numOnes] += (double)1 / 6 * rollDice(diceLeft - 1, numOnes - 1);
			}
			// Case:
			if (numOnes < diceLeft) {
				pRollOutcome[diceLeft][numOnes] += (double)5 / 6 * rollDice(diceLeft - 1, numOnes);
			}
		}
		return pRollOutcome[diceLeft][numOnes];
	}
	
	/**
	 * Compute probabilities that player's score exceeds opponent's score
	 */
	private void computepExceed() {
		// Base case is when the score difference is 0
		for (int ones = 2; ones >= 0; --ones) {
			int diceLeft = NUM_DICE - ones;
			// For each case scoreDiff, consider each case rolls one
			for (int scoreDiff = 0; scoreDiff <= maxScore; ++scoreDiff) {
				for (int newOnes = 0; newOnes < 3 - ones; ++newOnes) {
					int point = diceLeft - newOnes;
					// Case: in-bound, when point is less than scoreDiff
					if (point <= scoreDiff) {
						// Get the probability of ones rolled given dice left
						pExceed[scoreDiff][ones] += pRollOutcome[diceLeft][newOnes] * 
								pExceed[scoreDiff - point][newOnes + ones];
					}
					else {
						pExceed[scoreDiff][ones] += pRollOutcome[diceLeft][newOnes];
					}
				}
			}
		}
		System.out.println(Arrays.deepToString(pExceed));
	}
	
	/**
	 * 
	 */
	public void valueIterate() {
		double maxChange;
		do {
			maxChange = 0.0;
			// for each case of player's score
			for (int i = 0; i < goal; i++) {
				// for each case of opponent's score
    			for (int j = 0; j < goal; j++) {
    				// for each case of turn total
    				for (int k = 0; k < goal - i; k++) {
    					// for each case of number of ones rolled
    					for (int onesRolled = 0; onesRolled < 3; onesRolled++) {
    						// save old probability
        					double oldProb = pWin[i][j][k][onesRolled];
        					// compute probability should hold (1 - probability opponent wins if player holds)
        					double pHold = 1.0 - computeProbWin(j, i + k, 0);
        					// compute probability should roll
        					double pRoll = 0.0;
        					/*
        					 * how to add onesRolled into this equation?????
        					 */
        					for (int ones = 0; ones <= NUM_DICE; ones++) {
        						if (ones >= 3) {
        							pRoll += 1.0 - computeProbWin(j, i, 0);
        						}
        						else {
        							pRoll += computeProbWin(i, j, k + (NUM_DICE - ones));
        						}
        					}
        					// update values
        					pWin[i][j][k][onesRolled] = Math.max(pRoll, pHold);
        					isRoll[i][j][k] = pRoll > pHold;
        					double currChange = Math.abs(pWin[i][j][k][onesRolled] - oldProb);
        					maxChange = Math.max(maxChange, currChange);
    					}
    					
    				}
    			}
    		}
		}
		while (maxChange > this.epsilon);
	}
	
	/**
	 * Compute probability of winning the game given i, j, k
	 * @param i: current player score
	 * @param j: opponent score
	 * @param k: current turn total
	 * @return probability of winning
	 */
	public double computeProbWin(int i, int j, int k) {
		// Baseline, need modify to fit this problem
		if (i + k >= goal)
	        return 1.0;
	    else if (j >= goal)
	        return 0.0;
	    else return pWin[i][j][k];
	}

	/**
	 * Determine whether player should roll or not
	 * @param i: player score
	 * @param j: opponent score
	 * @param k: current turn total
	 * @return whether player should roll or not
	 */
	public boolean shouldRoll(int i, int j, int k) {
    	return isRoll[i][j][k];
    }
	
	public static void main(String[]args) {
		new GreatRolledOne(50, 1e-9);
	}
}
