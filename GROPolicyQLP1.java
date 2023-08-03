public class GROPolicyQLP1 implements GROPolicy {
	final int GOAL = 50;
	@Override
	public boolean willRoll(int p, int i, int j, int k, int o) {
		if (p == 0) {
			// Player 1 model
			if (o == 0) {
				int holdAt = Math.max(50 - i, 29 - i + j);
				return k < holdAt;
			}
			if (p ==0 && i + k >= 50) {
				return false;
			}
			return o < 2;
		}
		else {
			// Player 2 model
			// p2 must exceed p1
			if (j >= GOAL) {
				return i+k <= j;
			}
			// p2 can hold and win
			if (i + k >= GOAL) {
				return false;
			}
			// must roll when we have <2 ones rolled
			if (o == 0) {
				return true;
			}
			else if (o == 1) {
				return true;
			}
			// roll 5 or more if we have 2 ones set aside
			else {
				return k < 5;
			}
		}
	}
	/*
Policy 1 first player win rate: 0.4586935651142298
Policy 2 first player win rate: 0.4184472245386512
Policy 1 second player win rate: 0.5815527754613488
Policy 2 second player win rate: 0.5413064348857701
Policy 1 average win rate: 0.5201231702877893
Policy 2 average win rate: 0.4798768297122107
Average win rate difference: -0.04024634057557863 (worse: ~ -4.02% gap)

NOTE: j - i can contribute in decision making, simple calculation
	 */
}
