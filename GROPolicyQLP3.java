package Game;

public class GROPolicyQLP3 implements GROPolicy {
	int max_diff = 29;
	@Override
	/**
	 * @param p - player (0/1)
	 * @param i - current player score
	 * @param j - current opponent score
	 * @param k - turn total
	 * @param o - ones rolled this turn
	 * @return whether or not player will roll in this state
	 */
	public boolean willRoll(int p, int i, int j, int k, int o) {
		int score_diff = i + k - j;
		
		if (o < 2) { //0, 1 ones rolled
			//when first player reach goal, continue to roll until 
			//score difference >= threshold of difference (max_diff)
			if (p == 0 && i + k >= 50) {
				return score_diff <= max_diff;
			}
			return true;
		}
		else {
			//player 2 model
			if (p == 1){ //o == 2
				if (j > i + k) { //first player is ahead
					if (j < 50) { //sec < first < 50
						if (j >= 38) { //TODO: adjust
							return true;
						}
						return score_diff <= 11;
					}
					else { 
						return true; //first player reach 50 => keep rolling
					}
				}
				else { //sec player (current) is ahead
					return score_diff <= 8;
				}
			}
			//player 1 model
			else {
				if (i + k >= 50) {
					return score_diff <= 11;
				}
				else {
					if (j > i + k) { //second player is ahead
						if (j < 50) { //sec < 50
							if (j >= 38) { //TODO: adjust
								return true;
							}
							return score_diff <= 11;
						}
					}
				}
			}
		}
		return false;
	}
	
	public static void main(String[] args) {
		GROPolicy optimal = new GROPolicyOptimal();
		GROPolicy approx = new GROPolicyRollWith4Or5();
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

/*
Policy 1 first player win rate: 0.4649960181683372
Policy 2 first player win rate: 0.40532090885404887
Policy 1 second player win rate: 0.5946790911459512
Policy 2 second player win rate: 0.5350039818316628
Policy 1 average win rate: 0.5298375546571442
Policy 2 average win rate: 0.4701624453428558
Average win rate difference: -0.05967510931428843
 */

