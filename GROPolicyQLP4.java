
public class GROPolicyQLP4 implements GROPolicy {

	@Override
	public boolean willRoll(int p, int i, int j, int k, int o) {
		if (p == 0) {
			// Player 1 model
			if (o == 0) {
				int holdAt = Math.max(50 - i, 29 - i - j);
				return k < holdAt;
			}
			if (p == 0 && i + k >= 50)
				return false;
			if (o == 1)
				return true;
			return k < 5;
		}
		else {
			// Player 2 model
			if (j >= 50) {
				return i+k <= j;
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
Policy 2 first player win rate: 0.41308883979770833
Policy 1 second player win rate: 0.5869111602022916
Policy 2 second player win rate: 0.5413064348857701
Policy 1 average win rate: 0.5228023626582607
Policy 2 average win rate: 0.47719763734173926
Average win rate difference: -0.04560472531652149 (worse: ~ -4.56% gap)
	 */
}
