import java.util.Arrays;

/**
 * GROPolicyComparison is a tool for highlighting differences in minimum hold values
 * between an optimal GROPolicy and a given approximately optimal GROPolicy.  You can 
 * see where your approximate policy most greatly deviates from optimal minimum hold values.
 * 
 * @author Todd W. Neller
 */
public class GROPolicyComparison {

	GROPolicy policy1, policy2;
	final int ROLL = 1, HOLD = 0;
	int numDice, goal, maxScore; // number of dice rolled at start of turn, goal score, and max score well beyond goal
	double epsilon; // value iteration convergence threshold
	double[][][][][] pWin1, pWin2; // indexed by player, score, opponent score, turn total, ones
	boolean[][][][][] roll1, roll2;
	double[][] pRollOutcome; // indexed by dice rolled, dice rolled 1
	double[][] pExceed; // probability of exceeding opponent with given index lead on last turn, ones

	public GROPolicyComparison(GROPolicy policy1, GROPolicy policy2) {
		this(policy1, policy2, 5, 50, 1e-14);
	}

	public GROPolicyComparison(GROPolicy policy1, GROPolicy policy2, int numDice, int goal, double epsilon) {
		this.policy1 = policy1;
		this.policy2 = policy2;
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
		// compare policies
		compare();
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

	public void compare() {
		// Print out CSV tables highlighting any differences in minimum hold value
		int maxScore = ((GreatRolledOnesSolver) policy1).maxScore;
		for (int p = 0; p < 2; p++) 
			for (int o = 0; o < 3; o++) {
				System.out.printf("\nPlayer %d, %d Ones:\n\"i\\j\",", p, o);
				for (int j = 0; j < maxScore; j++) 
					System.out.printf("%5d, ", j);
				System.out.println();
				for (int i = 0; i < maxScore; i++) {
					System.out.printf("%5d, ", i);
					for (int j = 0; j < maxScore; j++) {
						int minHold1 = 0;
						while (minHold1 < maxScore && policy1.willRoll(p, i, j, minHold1, o))
							minHold1++;
						int minHold2 = 0;
						while (minHold2 < maxScore && policy2.willRoll(p, i, j, minHold2, o))
							minHold2++;
						System.out.print(minHold1 == minHold2 ? "     , " 
								: String.format("%2d/%2d, ", minHold1, minHold2));
					}
					System.out.println();
				}
				System.out.println();
			}
	}

	public static void main(String[] args) {
		GROPolicy optimal = new GreatRolledOnesSolver();
		
		// TODO - Experiment with creating new GROPolicy classes to discover:
		// (1) The best-performing policy that can be computed with mental math, and
		// (2) The best-performing policy that takes the least code/memory, i.e. the simplest, best-performing
		//     approximation of the optimal GROPolicy
//		GROPolicy approx = new GROPolicyNeller1();
//		GROPolicy approx = new GreatRolledOne(50, 1e-14);
//		((GreatRolledOne) approx).valueIterate();
		GROPolicy approx = new GROPolicyRollWith4Or5();
		
		// Highlight where the minimum hold values of the policies are different,
		// displaying <optimal minimum hold value>/<approx minimum hold value>.
		new GROPolicyComparison(optimal, approx);
	}
	

}
