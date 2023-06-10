
import java.util.Arrays;

public class GreatRolledOne {
	int goal;
	Double[][] pRollOutcome; // [num_dice_left][num_ones_rolled]
	double[][] pExceed; // [score_diff][num_ones_rolled]
	Boolean[][][] roll; //[curr_score][oppo_score][turn_total]
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
		roll = new Boolean[goal][goal][goal];
		pExceed = new double[maxScore + 1][3];
		pRollOutcome = new Double[NUM_DICE + 1][NUM_DICE + 1];
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
	 * Compute probabilities given diceLeft and numOnes. For reference, check Fig.1 in README
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
		
	}
	
	/**
	 * 
	 * @param i: player score
	 * @param j: opponent score
	 * @param k: current turn total
	 * @return whether roll or not
	 */
	public boolean shouldRoll(int i, int j, int k) {
    	return roll[i][j][k];
    }
	
	/**
	 * 
	 * @param i: player score
	 * @param j: opponent score
	 * @param k: current turn total
	 * @return probability of winning
	 */
	public double pWin(int i, int j, int k) {
		// Baseline, need modify to fit this problem
		if (i + k >= goal)
            return 1.0;
        else if (j >= goal)
            return 0.0;
        else return p[i][j][k];
    }
	
	public static void main(String[]args) {
		new GreatRolledOne(50, 1e-9);
	}
}
