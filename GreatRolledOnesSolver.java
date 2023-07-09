import java.awt.Color;
import java.util.Formatter;

/**
 * GreatRolledOnesSolver - computes optimal play for The Great Rolled Ones dice game from Random Fun Generator (2020).
 * The rules specify that all players get the same number of turns, and do _not_ specify what happens in the place of a tie.
 * We therefore presume that when a player reaches at least the goal score, an opponent is constrained to try to exceed
 * that score, i.e. no ties are permitted.  A roll at the beginning of the turn (turn total 0) is required.
 * 
 * First (flawed) version written and computed on April 20, 2023.
 * Correct version written and computed on May 18th, 2023, validated July 6th, 2023.
 * @author Todd W. Neller
 */
public class GreatRolledOnesSolver implements GROPolicy {

	final int ROLL = 1, HOLD = 0;
	public int numDice, goal, maxScore; // number of dice rolled at start of turn, goal score, and max score well beyond goal
	double epsilon; // value iteration convergence threshold
	double[][][][][] pWin; // indexed by player, score, opponent score, turn total, ones
	boolean[][][][][] roll;
	double[][] pRollOutcome; // indexed by dice rolled, dice rolled 1
	double[][] pExceed; // probability of exceeding opponent with given index lead on last turn, ones
	boolean[][][][][] isReachable;

	public GreatRolledOnesSolver() {
		this(5, 50, 1e-14);
	}

	public GreatRolledOnesSolver(int numDice, int goal, double epsilon) {
		this.numDice = numDice;
		this.goal = goal;
		maxScore = 2 * goal;
		this.epsilon = epsilon;
		//		if (!load()) { // total load/save is over twice as slow as recomputing
		System.out.printf("Solving numDice = %d, goal = %d, epsilon = %.1e ...\n", numDice, goal, epsilon);
		init();
		fastSolve();
		System.out.println("Solved.");
		//			save();
		//		}
	}

	private void init() {
		pWin = new double[2][maxScore][maxScore][maxScore][3]; // indexed by player, score, opponent score, turn total, ones
		roll = new boolean[2][maxScore][maxScore][maxScore][3];
		
		pRollOutcome = new double[numDice + 1][];
		pRollOutcome[0] = new double[] {1.0};
		for (int dice = 1; dice < pRollOutcome.length; dice++) {
			pRollOutcome[dice] = new double[dice + 1];
			for (int ones = 0; ones <= dice; ones++ ) {
				if (ones < dice)
					pRollOutcome[dice][ones] += pRollOutcome[dice - 1][ones] * 5 / 6; // next die rolled was non-one
				if (ones > 0)
					pRollOutcome[dice][ones] += pRollOutcome[dice - 1][ones - 1] / 6; // next die rolled was one
			}
		}
//		System.out.println("Probability of new 1s (column) by number of dice rolled (row), 0-based:");
//		for (double[] outcome : pRollOutcome)
//			System.out.println(Arrays.toString(outcome));

//		System.out.println("Probability of exceeding score difference:");
		pExceed = new double[3][maxScore];
		for (int ones = 2; ones >= 0; ones--) {
			for (int scoreDiff = 0; scoreDiff < maxScore; scoreDiff++) {
				double pWinRoll = 0;
				int diceLeft = numDice - ones;
				for (int newOnes = 0; newOnes < 3 - ones; newOnes++) {
					int diffReduced = diceLeft - newOnes;
					pWinRoll += pRollOutcome[diceLeft][newOnes] * (diffReduced > scoreDiff ? 1.0 : pExceed[ones + newOnes][scoreDiff - diffReduced]);
				}
				pExceed[ones][scoreDiff] = pWinRoll;
			}
//			System.out.println(ones + " ones: " + Arrays.toString(pExceed[ones]));
		}
	}

//	private boolean load() {
//		String filename = String.format("gro-%d-%d-%e.dat", numDice, goal, epsilon);
//		File file = new File(filename);
//		try (
//				DataInputStream input = new DataInputStream(new FileInputStream(file));
//				) {
//			init();
//			for (int p = 0; p < pWin.length; p++)
//				for (int i = 0; i < pWin[p].length; i++)
//					for (int j = 0; j < pWin[p][i].length; j++)
//						for (int k = 0; k < pWin[p][i][j].length; k++)
//							for (int o = 0; o < pWin[p][i][j][k].length; o++)
//								pWin[p][i][j][k][o] = input.readDouble(); // indexed by player, score, opponent score, turn total, ones
//			for (int p = 0; p < roll.length; p++)
//				for (int i = 0; i < roll[p].length; i++)
//					for (int j = 0; j < roll[p][i].length; j++)
//						for (int k = 0; k < roll[p][i][j].length; k++)
//							for (int o = 0; o < roll[p][i][j][k].length; o++)
//								roll[p][i][j][k][o] = input.readBoolean();
//			return true;
//		} catch (Exception e) {
//			System.out.println(filename + " cannot load -> generating...");
//		}
//		return false;		
//	}
//
//	private void save() {
//		String filename = String.format("gro-%d-%d-%e.dat", numDice, goal, epsilon);
//		File file = new File(filename);
//		try (
//				DataOutputStream output = new DataOutputStream(new FileOutputStream(file));
//				) {
//			for (int p = 0; p < pWin.length; p++)
//				for (int i = 0; i < pWin[p].length; i++)
//					for (int j = 0; j < pWin[p][i].length; j++)
//						for (int k = 0; k < pWin[p][i][j].length; k++)
//							for (int o = 0; o < pWin[p][i][j][k].length; o++)
//								output.writeDouble(pWin[p][i][j][k][o]); // indexed by player, score, opponent score, turn total, ones
//			for (int p = 0; p < roll.length; p++)
//				for (int i = 0; i < roll[p].length; i++)
//					for (int j = 0; j < roll[p][i].length; j++)
//						for (int k = 0; k < roll[p][i][j].length; k++)
//							for (int o = 0; o < roll[p][i][j][k].length; o++)
//								output.writeBoolean(roll[p][i][j][k][o]);
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}	
//	}
//
//	private void solve() {
//		double maxChange;
//		do {
//			maxChange = 0.0;
//			for (int p = 0; p < 2; p++)
//				for (int i = 0; i < goal; i++)
//					for (int j = 0; j < maxScore; j++)
//						for (int k = 0; k < maxScore - i; k++)
//							for (int o = 0; o < 3; o++) {
//								double oldPWin = pWin[p][i][j][k][o];
//
//								double pWinRoll = 0;
//								if (j >= goal && i <= j) { // special case: opponent held having achieved the goal, must still exceed
//									pWinRoll = p == 0 ? 0 : pExceed[o][j - i]; // player 1 could not roll in this case; this check not needed?
//								}
//								else {
//									int diceLeft = numDice - o;
//									for (int newOnes = 0; newOnes <= diceLeft; newOnes++) {
//										if (o + newOnes >= 3) // insanity (like pig roll)
//											pWinRoll += pRollOutcome[diceLeft][newOnes] * (1 - pWin(1 - p, j, i, 0, 0));
//										else
//											pWinRoll += pRollOutcome[diceLeft][newOnes] * pWin(p, i, j, k + (diceLeft - newOnes), o + newOnes);
//									}
//								}
//
//								double pWinHold = 0;
//								// special case: One can only hold if the turn total is nonzero and
//								//   (the other player hasn't met the goal score or you can hold and exceed it)
//								if (k > 0 && (j < goal || i + k > j)) {
//									pWinHold = 1 - pWin(1 - p, j, i + k, 0, 0);
//								}
//
//								roll[p][i][j][k][o] = pWinRoll > pWinHold;
//								pWin[p][i][j][k][o] = pWinRoll > pWinHold ? pWinRoll : pWinHold;
//
//								//								if (p == 0 && i == 0 && j == 50 && k == 0 && o == 0) {
//								//									System.out.printf("%f %f %f\n", pWin[p][i][j][k][o], pWinRoll, pWinHold);
//								//								}
//
//								double change = Math.abs(oldPWin - pWin[p][i][j][k][o]);
//								maxChange = maxChange > change ? maxChange : change;
//							}
//			//			System.out.println(maxChange);
//		} while (maxChange > epsilon);
//	}

	private void fastSolve() {
		double maxChange;
		do {
			maxChange = 0.0;
			for (int p = 0; p < 2; p++)
				for (int o = 2; o >= 0; o--)
					for (int totalScore = goal + maxScore - 2; totalScore >= 0; totalScore--)
						for (int i = goal - 1; i >= 0; i--) {
							int j = totalScore - i;
							if (j < 0 || j >= maxScore) continue;
							for (int k = maxScore - i - 1; k >= 0; k--) {
//								System.out.printf("%d %d %d\n", i, j, k);
								double oldPWin = pWin[p][i][j][k][o];

								double pWinRoll = 0;
								if (j >= goal && i <= j) { // special case: opponent held having achieved the goal, must still exceed
									pWinRoll = p == 0 ? 0 : pExceed[o][j - i]; // player 1 could not roll in this case; this check not needed?
								}
								else {
									int diceLeft = numDice - o;
									for (int newOnes = 0; newOnes <= diceLeft; newOnes++) {
										if (o + newOnes >= 3) // insanity (like pig roll)
											pWinRoll += pRollOutcome[diceLeft][newOnes] * (1 - pWin(1 - p, j, i, 0, 0));
										else
											pWinRoll += pRollOutcome[diceLeft][newOnes] * pWin(p, i, j, k + (diceLeft - newOnes), o + newOnes);
									}
								}

								double pWinHold = 0;
								// special case: One can only hold if the turn total is nonzero and
								//   (the other player hasn't met the goal score or you can hold and exceed it)
								if (k > 0 && (j < goal || i + k > j)) {
									pWinHold = 1 - pWin(1 - p, j, i + k, 0, 0);
								}

								roll[p][i][j][k][o] = pWinRoll > pWinHold;
								pWin[p][i][j][k][o] = pWinRoll > pWinHold ? pWinRoll : pWinHold;

								double change = Math.abs(oldPWin - pWin[p][i][j][k][o]);
								maxChange = maxChange > change ? maxChange : change;
							}
						}
			//			System.out.println(maxChange);
		} while (maxChange > epsilon);
	}

	public double pWin(int p, int i, int j, int k, int o) {
		if (p == 0 && i >= goal) // game end; note not i + k > goal; must have already held
			return i >= j ? 1.0 : 0.0;
		if (p == 1 && i + k >= goal && i + k > j)
			return 1.0;
		// truncate scores/totals
		if (i >= maxScore) i = maxScore - 1;
		if (j >= maxScore) j = maxScore - 1;
		if (i + k >= maxScore) k = maxScore - 1 - i;
		return pWin[p][i][j][k][o];
	}

	@Override
	public boolean willRoll(int p, int i, int j, int k, int o) {
		if (i >= maxScore) i = maxScore - 1;
		if (j >= maxScore) j = maxScore - 1;
		if (i + k >= maxScore) k = maxScore - 1 - i;
		return roll[p][i][j][k][o];
	}

	public void printMinHoldValues() {
		System.out.println("\nMinimum hold values:");
		for (int p = 0; p < 2; p++) 
			for (int o = 0; o < 3; o++) {
				System.out.printf("\nPlayer %d, %d Ones:\n i\\j", p, o);
				for (int j = 0; j < maxScore; j++) 
					System.out.printf("%4d", j);
				System.out.println();
				for (int i = 0; i < maxScore; i++) {
					System.out.printf("%4d", i);
					for (int j = 0; j < maxScore; j++) {
						int k = 0;
						while (k + 1 < maxScore && roll[p][i][j][k][o])
							k++;
						System.out.printf("%4d", k);
					}
					System.out.println();
				}
				System.out.println();
			}
	}

	public void printTurnStartWinProbs() {
		for (int p = 0; p < 2; p++)  {
			System.out.printf("\nPlayer %d\n i\\j", p);
			for (int j = 0; j < maxScore; j++) 
				System.out.printf("%5d", j);
			System.out.println();
			for (int i = 0; i < maxScore; i++) {
				System.out.printf("%4d", i);
				for (int j = 0; j < maxScore; j++) {
					System.out.printf(" %4.2f", pWin[p][i][j][0][0]);
				}
				System.out.println();
			}
		}
	}

	public boolean checkForInclusion() {
		for (int p = 0; p < 2; p++) 
			for (int i = 0; i < maxScore; i++)
				for (int j = 0; j < maxScore; j++)
					for (int k = 0; k < maxScore - i; k++) 
						if ((roll[p][i][j][k][2] && !roll[p][i][j][k][1]) || (roll[p][i][j][k][1] && !roll[p][i][j][k][0])) {
							System.out.printf("%d %d %d %d 0 %b 1 %b 2 %b\n", p, i, j, k, roll[p][i][j][k][0], roll[p][i][j][k][1], roll[p][i][j][k][2]);
							return false;
						}
		return true;
	}

	public void generateVRML() {
		Color[] colors = {Color.GRAY, Color.YELLOW, Color.RED};
		float[][] rgbColors = new float[colors.length][];
		for (int i = 0; i < colors.length; i++)
			rgbColors[i] = colors[i].getRGBColorComponents(null);
		double[] transparencies = {.9, .9, 0};
		for (int p = 0; p < 2; p++) {
			try {
				int count = 0;
				Formatter out = new Formatter(String.format("roll_p%d.wrl", p + 1));
				out.format("#VRML V2.0 utf8\n");
				out.format("Group { children [\n");
				for (int i = 0; i < maxScore; i++)
					for (int j = 0; j < maxScore; j++)
						for (int k = 0; k < maxScore - i; k++) {
							int o = -1;
							while (o < 2 && roll[p][i][j][k][o + 1])
								o++;
							if (o >= 0) {
								out.format("DEF box%d Transform {\n", count++);
								out.format("  translation %d %d %d\n", i, j, k);
								out.format("  children [ Shape { appearance Appearance { material Material { diffuseColor %f %f %f transparency %f } } geometry Box { size 1 1 1 } } ] }\n", 
										rgbColors[o][0], rgbColors[o][1], rgbColors[o][2], transparencies[o]);
							}
						}
				out.format("] }\n");
				out.close();
			}
			catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
	
	public void computeReachability(int p, int i, int j, int k, int o) {
		isReachable = new boolean[2][maxScore][maxScore][maxScore][3];
		reach(p, i, j, k, o);
	}
	
	private void reach(int p, int i, int j, int k, int o) { 
//		try { ## commented try-catch lines here are for tracing how a state may be reached
		if (isReachable[p][i][j][k][o])
			return;
//		if (p == 0 && i == 6 && j == 0 && k == 0 && o == 0)  # for try-catch testing of state sequences
//			throw new RuntimeException("Reach query");
		isReachable[p][i][j][k][o] = true;
		int diceLeft = numDice - o;
		if (p == 1 && j >= goal && i <= j) { // special case: opponent held having achieved the goal, must still exceed
			for (int d = 3; d <= diceLeft; d++)
				reach(p, i, j, Math.min(maxScore - 1, k + d), numDice - d);
		}
		else {
			if (roll[p][i][j][k][o]) { // roll
				reach(1 - p, j, i, 0, 0);
				for (int d = 3; d <= diceLeft; d++)
					reach(p, i, j, Math.min(maxScore - 1, k + d), numDice - d);
			}
			else { // hold
				reach(1 - p, j, Math.min(maxScore - 1, i + k), 0, 0);
			}
		}
//		}
//		catch (RuntimeException e) {
//			System.out.printf("%d %d %d %d %d\n", p, i, j, k, o);
//			throw e;
//		}
	}
	

	public static void main(String[] args) {
		long startMS = System.currentTimeMillis();
		GreatRolledOnesSolver solver = new GreatRolledOnesSolver(); // = new GreatRolledOnesSolver(5, 50, 1e-9); // = new GreatRolledOnesSolver();
		System.out.println("Solved in " + (System.currentTimeMillis() - startMS) + " ms.");
		System.out.println("pWin(0, 0, 0, 0, 0) = " + solver.pWin(0, 0, 0, 0, 0)); // 0.44955341311566405

		// Optimal komi: i=3: pWin=0.495505 (pWin - .5 = -0.004495)
		int fairestI = 0;
		double fairestPWin = 0.0;
		double minDistFromFair = Double.MAX_VALUE;
		for (int i = 0; i < 10; i++) {
			double pWin = solver.pWin(0, i, 0, 0, 0);
			double distFromFair = Math.abs(pWin - 0.5);
			if (distFromFair < minDistFromFair) {
				minDistFromFair = distFromFair;
				fairestI = i;
				fairestPWin = pWin;
			}
			System.out.printf("i=%d: pWin=%f (pWin - .5 = %f)\n", i, pWin, pWin - .5);
		}
		System.out.printf("Fairest komi i=%d: pWin=%f (pWin - .5 = %f)\n", fairestI, fairestPWin, fairestPWin - .5);
		
		/*
i=0: pWin=0.449741 (pWin - .5 = -0.050259)
i=1: pWin=0.464806 (pWin - .5 = -0.035194)
i=2: pWin=0.480106 (pWin - .5 = -0.019894)
i=3: pWin=0.495619 (pWin - .5 = -0.004381)
i=4: pWin=0.511319 (pWin - .5 = 0.011319)
i=5: pWin=0.527100 (pWin - .5 = 0.027100)
i=6: pWin=0.542980 (pWin - .5 = 0.042980)
i=7: pWin=0.559047 (pWin - .5 = 0.059047)
i=8: pWin=0.575270 (pWin - .5 = 0.075270)
i=9: pWin=0.591508 (pWin - .5 = 0.091508)
		 */

		//		System.out.println(solver.checkForInclusion()); // true

		solver.printMinHoldValues();
//		solver.printTurnStartWinProbs();
		//		solver.generateVRML();
		
		// Uncomment for reachability computataion (isReachable[][][][][])
		// ... with default initial state:
//		solver.computeReachability(0, 0, 0, 0, 0);
		// ... with first player komi i = 3:
//		solver.computeReachability(3, 0, 0, 0, 0);
	}
	

}
