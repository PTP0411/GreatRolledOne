import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * GROPolicyCritic is a tool for identifying primary weaknesses in suboptimal GRO play.
 * Given an approximately optimal policy to play against the optimal policy,
 * a call to simulateGames(NUM_GAMES) will simulate NUM_GAMES games with each
 * policy alternating playing as first player.
 * Errors in play decisions are noted along with severity, grouping errors that
 * vary only in k (turn total) value.
 * A call to textProfile(PERCENTILE) will sort error statistics and then print tables that
 * are blank except for top PERCENTILE percentile situations where errors most greatly
 * impact the quality of play.
 * 
 * @author Todd W. Neller
 */
public class GROPolicyCritic {
	public static final int NUM_GAMES = 1000000, PERCENTILE = 95;

	GreatRolledOnesSolver optimal = new GreatRolledOnesSolver();
	GROPolicy policy;
	Map<String, GROError> errorMap = new HashMap<>();
	
	public GROPolicyCritic(GROPolicy policy) {
		this.policy = policy;
	}
	
	public int[] simulateGames(int numGames) {
		int[] numWins = new int[2]; // policy1, policy2 wins
		for (int i = 0; i < numGames; i++) {
			int policy2player = i % 2;
			numWins[policy2player == simulateGame(policy2player) ? 1 : 0]++;
		}
		return numWins;
	}
	
	public int simulateGame(int policy2Player) {
		int numDice = optimal.numDice;
		int goal = optimal.goal;
		int[] score = new int[2];
		int p = 0; // current player
		while (true) { // while game isn't over, take a turn
			int o = 0;
			int i = score[p];
			int j = score[1 - p];
			int k = 0;
			while (true) { // while turn isn't over, roll and make a decision
//				System.out.printf("p=%d i=%d j=%d k=%d o=%d\n", p, i, j, k, o);
				int newOnes = 0;
				int diceRolled = numDice - o;
				for (int d = 0; d < diceRolled; d++) {
					int roll = (int) (6 * Math.random()) + 1;
					if (roll == 1)
						newOnes++;
				}
				o += newOnes;
				if (o >= 3) {
					// end turn insane
					break;
				}
				k += diceRolled - newOnes;
				if (k >= optimal.maxScore) {
					k = optimal.maxScore - 1;
					break;
				}
				
				// Do not force the following rational decisions:
//				if (p == 1 && score[0] >= goal && i + k > score[0]) {
//					// player 2 wins exceeding player 1 -> hold
//					break;
//				}
//				if (p == 1 && score[0] < goal && i + k >= goal) {
//					// player 2 wins exceeding 50 first -> hold
//					break;
//				}
				
				// Otherwise, there is a decision to be made:
				boolean optimalWillRoll = optimal.willRoll(p, i, j, k, o);
				if (policy2Player == p) {
					// policy player
					boolean willRoll = policy.willRoll(p, i, j, k, o);
					if (willRoll != optimalWillRoll) {
						// record error
						String errorString = String.format("%d,%d,%d,%d", p, i, j, o);
						if (!errorMap.containsKey(errorString))
							errorMap.put(errorString, new GROError(p, i, j, o));
						errorMap.get(errorString).newError(k);
					}
					if (!willRoll)
						break;
				}
				else {
					// optimal player
					if (!optimalWillRoll)
						break;
				}
			}
			// Turn ended
			if (o < 3)
				score[p] += k;
			// Check if game is over
			if (score[0] >= goal && p == 1) // first player reaches goal and second player has had a chance to exceed
				break;
			if (score[1] >= goal) // second player reaches goal (game ends immediately)
				break;
			// Change player
			p = 1 - p;
		}
		return score[0] >= score[1] ? 0 : 1;
	}
	
	class GROError implements Comparable<GROError> {
		int p, i, j, kCorrect, o;
		int count;
		double lossSum;
		
		public GROError(int p, int i, int j, int o) {
			this.p = p;
			this.i = i;
			this.j = j;
			this.o = o;
			
			kCorrect = 0;
			while (kCorrect + 1 < optimal.maxScore && optimal.willRoll(p, i, j, kCorrect, o))
				kCorrect++;
		}
		
		public void newError(int k) {
			count++;
			
			// Compute pWinRoll for k
			double pWinRoll = 0;
			if (j >= optimal.goal && i <= j) { // special case: opponent held having achieved the goal, must still exceed
				pWinRoll = p == 0 ? 0 : optimal.pExceed[o][j - i]; // player 1 could not roll in this case; this check not needed?
			}
			else {
				int diceLeft = optimal.numDice - o;
				for (int newOnes = 0; newOnes <= diceLeft; newOnes++) {
					if (o + newOnes >= 3) // insanity (like pig roll)
						pWinRoll += optimal.pRollOutcome[diceLeft][newOnes] * (1 - optimal.pWin(1 - p, j, i, 0, 0));
					else
						pWinRoll += optimal.pRollOutcome[diceLeft][newOnes] * optimal.pWin(p, i, j, k + (diceLeft - newOnes), o + newOnes);
				}
			}


			// Compute pWinHold for k
			double pWinHold = 0;
			// special case: One can only hold if the turn total is nonzero and
			//   (the other player hasn't met the goal score or you can hold and exceed it)
			if (k > 0 && (j < optimal.goal || i + k > j)) {
				pWinHold = 1 - optimal.pWin(1 - p, j, i + k, 0, 0);
			}
			
			// Add absolute difference to lossSum
			lossSum += Math.abs(pWinRoll - pWinHold);
		}

		@Override
		public int compareTo(GROPolicyCritic.GROError other) {
			return (int) Math.signum(lossSum - other.lossSum);
		}
		
	}
	

	public void textProfile(int percentile) {
		if (errorMap.size() == 0) { // No errors experienced
			System.out.println("(No errors experienced in simulated play.)");
			return;
		}
		
		// Find error percentily threshold
		GROError[] errors = errorMap.values().toArray(new GROError[errorMap.size()]);
		Arrays.sort(errors);
		int thresholdIndex = percentile * errors.length / 100;
		double threshold = errors[thresholdIndex].lossSum;
		
		// Print out CSV tables highlighting greatest errors
		for (int p = 0; p < 2; p++) 
			for (int o = 0; o < 3; o++) {
				System.out.printf("\nPlayer %d, %d Ones:\n\"i\\j\",", p, o);
				for (int j = 0; j < optimal.maxScore; j++) 
					System.out.printf("%3d,", j);
				System.out.println();
				for (int i = 0; i < optimal.maxScore; i++) {
					System.out.printf("%5d,", i);
					for (int j = 0; j < optimal.maxScore; j++) {
						GROError error = errorMap.get(String.format("%d,%d,%d,%d", p, i, j, o));
						System.out.print((error == null || error.lossSum < threshold) 
								? "   ," : String.format("%3d,", error.kCorrect));
					}
					System.out.println();
				}
				System.out.println();
			}
	}

	public static void main(String[] args) {
		
		// TODO - Experiment with creating new GROPolicy classes to discover:
		// (1) The best-performing policy that can be computed with mental math, and
		// (2) The best-performing policy that takes the least code/memory, i.e. the simplest, best-performing
		//     approximation of the optimal GROPolicy
//		GROPolicy approx = new GreatRolledOne(50, 1e-14);
//		((GreatRolledOne) approx).valueIterate();
		GROPolicy approx = new GROPolicyRollWith4Or5();
//		GROPolicy approx = new GROPolicyNeller1();
		
		GROPolicyCritic critic = new GROPolicyCritic(approx);
		
		// Simulate games to look at win rate and record observed errors.
		int[] numWins = critic.simulateGames(NUM_GAMES);
//		System.out.println(Arrays.toString(numWins));
		System.out.printf("Optimal policy wins: %8d (%5.2f%%)\n", numWins[0], 100.0 * numWins[0] / NUM_GAMES);
		System.out.printf("Approx. policy wins: %8d (%5.2f%%)\n", numWins[1], 100.0 * numWins[1] / NUM_GAMES);
		
		// Highlight nonterminal states where experienced error is in above the threshold percentile.
		critic.textProfile(PERCENTILE); 
	}
}
