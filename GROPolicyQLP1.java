
public class GROPolicyQLP1 implements GROPolicy {

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
			if (j >= 50) {
				return i + k <= j;
			}
			if (o == 0) {
				return true;
			}
			else if (o == 1) {
				return true;
			}
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
	 */
}
