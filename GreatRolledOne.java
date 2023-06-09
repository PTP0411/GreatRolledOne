
import java.util.Arrays;

public class GreatRolledOne {
	int goal;
	Double[][] pRollOutcome;
	double[][] pExceed;
	Boolean[][][] roll; //roll or hold
	double epsilon;
	int maxScore; //score that player 1 will not exceed
	final int NUM_DICES = 5; 
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
		roll = new Boolean[goal][goal][goal];	// why 3 dims
		pExceed = new double[maxScore + 1][3];	// why 2 dims
		pRollOutcome = new Double[NUM_DICES + 1][NUM_DICES + 1];	// why 2 dims
		//compute winning probabilities
		pOneRolled();
		computepExceed();
	}

	/**
	 * Calculate prob that one is rolled???
	 */
	private void pOneRolled() {
		// initialize pRollOutcome with 1.0
		Arrays.fill(pRollOutcome[0], 1.0);
		for (int dice = 1; dice <= NUM_DICES; dice++) {
			for (int one = 0; one <= dice; one++) {
				rollDice(dice, one);
			}
		}
		for (Double[] outcome : pRollOutcome) {
			System.out.println(Arrays.toString(outcome));
		}
	}

	/**
	 * This func do sth
	 * @param diceLeft
	 * @param numOnes
	 * @return
	 */
	private double rollDice(int diceLeft, int numOnes) {
		// Case: 
		if (diceLeft == 0 && numOnes == 0) {
			pRollOutcome[diceLeft][numOnes] = 1.0;
		}
		// Case:
		if (pRollOutcome[diceLeft][numOnes] == null) {
			pRollOutcome[diceLeft][numOnes] = 0.0;
			// Case:
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
	 * computepExceed do sth
	 */
	private void computepExceed() {
		//base case is when the score difference is 0
		for (int ones = 2; ones >= 0; --ones) {
			int diceLeft = NUM_DICES - ones;
			// ??
			for (int scoreDiff = 0; scoreDiff <= maxScore; ++scoreDiff) {
				for (int newOnes = 0; newOnes < 3 - ones; ++newOnes) {
					int point = diceLeft - newOnes;
					// Case: in-bound
					if (point <= scoreDiff) {
						// get the probability of ones rolled given dice left
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

	public static void main(String[]args) {
		new GreatRolledOne(50, 1e-9);
	}
}
