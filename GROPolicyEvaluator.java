import java.util.Arrays;

/**
 * GreatRolledOnesPolicyEvaluator - Given two policy and an assumption that policies will (1) hold to win, and 
 * (2) roll to exceed as second player, performs a value iteration on the fixed policies to compute
 * the probabilities of each policy winning against the other in each possible non-terminal state.
 * As output, the code shows computed win rates of each policy as first and second player, as well as
 * the average win rate (i.e. the expected win rate if a policy plays as first or second player each half of the time.

 * @author Todd W. Neller
 */
public class GROPolicyEvaluator {

	final int ROLL = 1, HOLD = 0;
	int numDice, goal, maxScore; // number of dice rolled at start of turn, goal score, and max score well beyond goal
	double epsilon; // value iteration convergence threshold
	double[][][][][] pWin1, pWin2; // indexed by player, score, opponent score, turn total, ones
	boolean[][][][][] roll1, roll2;
	double[][] pRollOutcome; // indexed by dice rolled, dice rolled 1
	double[][] pExceed; // probability of exceeding opponent with given index lead on last turn, ones

	public GROPolicyEvaluator(GROPolicy policy1, GROPolicy policy2) {
		this(policy1, policy2, 5, 50, 1e-14);
	}

	public GROPolicyEvaluator(GROPolicy policy1, GROPolicy policy2, int numDice, int goal, double epsilon) {
		this.numDice = numDice;
		this.goal = goal;
		maxScore = 2 * goal;
		this.epsilon = epsilon;
		init();
		// load policies to local arrays
		for (int p = 0; p < pWin1.length; p++)
			for (int i = 0; i < pWin1[p].length; i++)
				for (int j = 0; j < pWin1[p][i].length; j++)
					for (int k = 0; k < pWin1[p][i][j].length; k++)
						for (int o = 0; o < pWin1[p][i][j][k].length; o++) {
							roll1[p][i][j][k][o] = policy1.willRoll(p, i, j, k, o);
							try {
								roll2[p][i][j][k][o] = policy2.willRoll(p, i, j, k, o);
							}
							catch (Exception e) {
								System.out.printf("%d %d %d %d %d\n", p, i, j, k, o);
								System.exit(1);
							}
						}
		// evaluate performance of policies
		evaluate();
	}

	private void init() {
		pWin1 = new double[2][maxScore][maxScore][maxScore][3]; // indexed by player, score, opponent score, turn total, ones
		pWin2 = new double[2][maxScore][maxScore][maxScore][3]; 
		roll1 = new boolean[2][maxScore][maxScore][maxScore][3];
		roll2 = new boolean[2][maxScore][maxScore][maxScore][3];
		
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
		System.out.println("Probability of new 1s (column) by number of dice rolled (row), 0-based:");
		for (double[] outcome : pRollOutcome)
			System.out.println(Arrays.toString(outcome));

		System.out.println("Probability of exceeding score difference:");
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
			System.out.println(ones + " ones: " + Arrays.toString(pExceed[ones]));
		}
	}


	private void evaluate() {
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
								// Compute for policy1
								double oldPWin1 = pWin1[p][i][j][k][o];
								
								if (j >= goal && i <= j) { // special case: opponent held having achieved the goal, must still exceed
									pWin1[p][i][j][k][o] = p == 0 ? 0 : pExceed[o][j - i]; // player 1 could not roll in this case; this check not needed?
								}
								else if (roll1[p][i][j][k][o]) { // roll
									int diceLeft = numDice - o;
									pWin1[p][i][j][k][o] = 0;
									for (int newOnes = 0; newOnes <= diceLeft; newOnes++) {
										if (o + newOnes >= 3) // insanity (like pig roll)
											pWin1[p][i][j][k][o] += pRollOutcome[diceLeft][newOnes] * (1 - pWin(2, 1 - p, j, i, 0, 0));
										else
											pWin1[p][i][j][k][o] += pRollOutcome[diceLeft][newOnes] * pWin(1, p, i, j, k + (diceLeft - newOnes), o + newOnes);
									}
								}
								else { // hold
									pWin1[p][i][j][k][o] = 0;
									// special case: One can only hold if the turn total is nonzero and
									//   (the other player hasn't met the goal score or you can hold and exceed it)
									if (k > 0 && (j < goal || i + k > j)) {
										pWin1[p][i][j][k][o] = 1 - pWin(2, 1 - p, j, i + k, 0, 0);
									}
								}

								double change = Math.abs(oldPWin1 - pWin1[p][i][j][k][o]);
								maxChange = maxChange > change ? maxChange : change;

								// Compute for policy2
								
								double oldPWin2 = pWin2[p][i][j][k][o];
								
								if (j >= goal && i <= j) { // special case: opponent held having achieved the goal, must still exceed
									pWin2[p][i][j][k][o] = p == 0 ? 0 : pExceed[o][j - i]; // player 1 could not roll in this case; this check not needed?
								}
								else if (roll2[p][i][j][k][o]) { // roll
									int diceLeft = numDice - o;
									pWin2[p][i][j][k][o] = 0;
									for (int newOnes = 0; newOnes <= diceLeft; newOnes++) {
										if (o + newOnes >= 3) // insanity (like pig roll)
											pWin2[p][i][j][k][o] += pRollOutcome[diceLeft][newOnes] * (1 - pWin(1, 1 - p, j, i, 0, 0));
										else
											pWin2[p][i][j][k][o] += pRollOutcome[diceLeft][newOnes] * pWin(2, p, i, j, k + (diceLeft - newOnes), o + newOnes);
									}
								}
								else { // hold
									pWin2[p][i][j][k][o] = 0;
									// special case: One can only hold if the turn total is nonzero and
									//   (the other player hasn't met the goal score or you can hold and exceed it)
									if (k > 0 && (j < goal || i + k > j)) {
										pWin2[p][i][j][k][o] = 1 - pWin(1, 1 - p, j, i + k, 0, 0);
									}
								}

								change = Math.abs(oldPWin2 - pWin2[p][i][j][k][o]);
								maxChange = maxChange > change ? maxChange : change;
							}
						}
						System.out.println(maxChange);
		} while (maxChange > epsilon);
	}

	public double pWin(int policy, int p, int i, int j, int k, int o) {
		if (p == 0 && i >= goal) // game end; note not i + k > goal; must have already held
			return i > j ? 1.0 : 0.0;
			if (p == 1 && i + k >= goal && i + k > j)
				return 1;
			// truncate scores/totals
			if (i >= maxScore) i = maxScore - 1;
			if (j >= maxScore) j = maxScore - 1;
			if (i + k >= maxScore) k = maxScore - 1 - i;
			return policy == 1 ? pWin1[p][i][j][k][o] : pWin2[p][i][j][k][o];
	}


	public static void main(String[] args) {
		GROPolicy optimal = new GROPolicyOptimal();
		
		// TODO - Experiment with creating new GROPolicy classes to discover:
		// (1) The best-performing policy that can be computed with mental math, and
		// (2) The best-performing policy that takes the least code/memory, i.e. the simplest, best-performing
		//     approximation of the optimal GROPolicy
//		GROPolicy approx = new GROPolicyNeller1();
//		GROPolicy approx = new GROPolicyQLP1();
//		GROPolicy approx = new GROPolicyQLP2();
//		GROPolicy approx = new GROPolicyQLP3();
		GROPolicy approx = new GROPolicyQLP4();
//		GROPolicy approx = new GreatRolledOne(50, 1e-14);
		if (approx instanceof GreatRolledOne) {
			((GreatRolledOne) approx).valueIterate(); // We could avoid this step if you call valueIterate in the constructor.
		}
		
		GROPolicyEvaluator evaluator = new GROPolicyEvaluator(optimal, approx);
		double pol1p1 = evaluator.pWin1[0][0][0][0][0];
		double pol2p1 = evaluator.pWin2[0][0][0][0][0];
		double pol1p2 = 1 - pol2p1;
		double pol2p2 = 1 - pol1p1;
		double avg1 = (pol1p1 + pol1p2) / 2.0;
		double avg2 = (pol2p1 + pol2p2) / 2.0;
		double diff = avg2 - avg1;
		System.out.println("Policy 1 first player win rate: " + pol1p1);
		System.out.println("Policy 2 first player win rate: " + pol2p1);
		System.out.println("Policy 1 second player win rate: " + pol1p2);
		System.out.println("Policy 2 second player win rate: " + pol2p2);
		System.out.println("Policy 1 average win rate: " + avg1);
		System.out.println("Policy 2 average win rate: " + avg2);
		System.out.println("Average win rate difference: " + diff);
	}
	

}
