
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

	public GreatRolledOne(int goal, double epsilon) {
		this.goal = goal;
		this.maxScore = COEFFICENT * goal;
		this.epsilon = epsilon;
		roll = new Boolean[goal][goal][goal];
		pExceed = new double[maxScore + 1][3];
		pRollOutcome = new Double[NUM_DICES + 1][NUM_DICES + 1];
		//compute winning probabilies
		probOneRolled();
		computepExceed();
	}

	private void probOneRolled() {
		Arrays.fill(pRollOutcome[0], 1.0);
		for (int dices = 1; dices <= NUM_DICES; dices++) {
			for (int one = 0; one <= dices; one++) {
				rollDice(dices, one);
			}
		}
		for (Double[] outcome : pRollOutcome) {
			System.out.println(Arrays.toString(outcome));
		}
	}

	private double rollDice(int dicesLeft, int numOnes) {
		if (dicesLeft == 0 && numOnes == 0) {
			pRollOutcome[dicesLeft][numOnes] = 1.0;
		}
		if (pRollOutcome[dicesLeft][numOnes] == null) {
			pRollOutcome[dicesLeft][numOnes] = 0.0;
			if (numOnes > 0) {
				pRollOutcome[dicesLeft][numOnes] += (double)1 / 6 * rollDice(dicesLeft - 1, numOnes - 1);
			}
			if (numOnes < dicesLeft) {
				pRollOutcome[dicesLeft][numOnes] += (double)5 / 6 * rollDice(dicesLeft - 1, numOnes);
			}
		}
		return pRollOutcome[dicesLeft][numOnes];
	}

	private void computepExceed() {
		//base case is when the score difference is 0
		for (int o = 2; o >= 0; --o) {
			int diceLeft = NUM_DICES - o;
			for (int scoreDiff = 0; scoreDiff <= maxScore; ++scoreDiff) {
				for (int newO = 0; newO < 3 - o; ++newO) {
					int point = diceLeft - newO;

					if (point <= scoreDiff) { // in-bound case
						// get the probability of ones roll given dice left
						pExceed[scoreDiff][o] += pRollOutcome[diceLeft][newO] * 
								pExceed[scoreDiff - point][newO + o];
					}
					else {
						pExceed[scoreDiff][o] += pRollOutcome[diceLeft][newO];
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
