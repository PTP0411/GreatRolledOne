
import java.util.Arrays;

public class GreatRolledOne {
	int goal;
	Double[][] pRollOutcome; // [num_dice_left][num_ones_rolled]
	double[][] pExceed; // [score_diff][num_ones_rolled]
	Boolean[][][][][] isRoll; //[curr_score][oppo_score][num_ones_rolled][turn_total][isFirstPlayer]
	double[][][][][] pWin;	// [curr_score][oppo_score][num_ones_rolled][turn_total][isFirstPlayer]
	double epsilon;
	int maxScore; //score that player 1 will not exceed
	final int NUM_DICE = 5;
	final int COEFFICENT = 2;
	final int FIRST_PLAYER = 0, SEC_PLAYER = 1;

	/**
	 * Constructor of GreatRolledOne class
	 * @param goal
	 * @param epsilon
	 */
	public GreatRolledOne(int goal, double epsilon) {
		this.goal = goal;
		this.maxScore = COEFFICENT * goal;
		this.epsilon = epsilon;
		pExceed = new double[maxScore + 1][3];
		pRollOutcome = new Double[NUM_DICE + 1][NUM_DICE + 1];
		isRoll = new Boolean[maxScore][maxScore][3][maxScore][2];
		pWin = new double[maxScore][maxScore][3][maxScore][2];
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
    				// for each case of number of ones rolled
					for (int onesRolled = 0; onesRolled < 3; onesRolled++) {
						// for each case of turn total
						for (int k = 0; k < goal - i; k++) {
							// for each case is 1st player or 2nd player
							for (int p = 0; p <= 1; p++) {
								// save old probability
								double oldProb = pWin[i][j][onesRolled][k][p];
								// compute probability should hold
								double pHold = 1.0 - computeProbWin(j, i + k, 0, 0, (p+1)%2);
								if (k == 0 || (p == SEC_PLAYER && j >= goal && j >= i)) {
									pHold = 0.0;
								}
								// calculate number of dice left after rolling ones
								int diceLeft = NUM_DICE - onesRolled;
								// compute probability should roll
								double pRoll = 0.0;
								if (p == SEC_PLAYER) {
									pRoll = (j-i < 0) ? 1.0 : pExceed[j-i][onesRolled];	// this will causes error
								}
								else {
									for (int newOnes = 0; newOnes <= 2-onesRolled; newOnes++) {
										int turnScore = diceLeft - newOnes;
										pRoll += pRollOutcome[diceLeft][newOnes]
													* computeProbWin(i, j, onesRolled + newOnes, k + turnScore, p);
									}
									for (int newOnes = 3-onesRolled; newOnes <= diceLeft; newOnes++) {
										pRoll += pRollOutcome[diceLeft][newOnes] 
													* computeProbWin(j, i, 0, 0, (p+1)%2);
									}
								}
								// update values
								pWin[i][j][onesRolled][k][p] = Math.max(pRoll, pHold);
								isRoll[i][j][onesRolled][k][p] = pRoll > pHold;
								double currChange = Math.abs(pWin[i][j][onesRolled][k][p] - oldProb);
								maxChange = Math.max(maxChange, currChange);
							}
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
	 * @param onesRolled: number of ones rolled so far
	 * @param k: current turn total
	 * @param l: is current player the first player
	 * @return probability of winning
	 */
	public double computeProbWin(int i, int j, int onesRolled, int k, int p) {
		if (i+k >= maxScore) {
			return 1.0;
		}
		else if (j >= maxScore) {
			return 0.0;
		}
		else {
			return pWin[i][j][onesRolled][k][p];
		}
	}

	/**
	 * Determine whether player should roll or not
	 * @param i: player score
	 * @param j: opponent score
	 * @param onesRolled: number of ones rolled so far
	 * @param k: current turn total
	 * @param l: is first player's turn
	 * @return whether player should roll or not
	 */
	public boolean shouldRoll(int i, int j, int onesRolled, int k, int l) {
    	return isRoll[i][j][onesRolled][k][l];
    }
	
	public static void main(String[]args) {
		GreatRolledOne game = new GreatRolledOne(50, 1e-14);
		game.valueIterate();
		System.out.println(game.pWin[0][0][0][0][0]);
		
		// finding the state that game is fair
//		int goal = 50;
//		int limit = goal;
//		double dev = 0.01;
//		for (int i = 0; i < limit; i++) {
//			for (int j = 0; j < limit; j++) {
////				for (int onesRolled = 0; onesRolled < 3; onesRolled++) {
////					for (int k = 0; k < goal - i; k++) {
////						for (int l = 0; l <= 1; l++) {
//						int onesRolled=0;
//						int k=0;
//						int l=0;	// first player
//							if (0.5-dev < game.pWin[i][j][onesRolled][k][l] && game.pWin[i][j][onesRolled][k][l] < 0.5+dev) {
//								System.out.println(game.pWin[i][j][onesRolled][k][l] 
//										+ ": i=" + i 
//										+ " j=" + j 
//										+ " o=" + onesRolled 
//										+ " k=" + k 
//										+ " l=" + l);
//							}
////						}
////					}
////				}
//			}
//		}
	}
}
