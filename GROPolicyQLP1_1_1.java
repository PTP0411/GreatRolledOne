
public class GROPolicyQLP1_1_1 implements GROPolicy {

	@Override
	public boolean willRoll(int p, int i, int j, int k, int o) {
		if (p == 0) {
			// Player 1 model
			if (o == 0) {
				int holdAt = Math.max(50 - i, 29 - i + j);
				return k < holdAt;
			}
			else if (o == 1) {
				int holdAt = (j > i) ? 25 + 4 * j / 5 - i : 25 - i / 2 + j / 3;
				// Go-for-it approximated as i > 35 or j > 25
				if (i >= 35 || j >= 25)
					holdAt = Math.max(holdAt, 50 - i);
				return k < holdAt;

//				return true;
			}
			else if (j >= 40 && i < j) {
				return k < 17;
			}
			else {
				return k < 5;
			}
		}
		else {
			// Player 2 model
			if (j >= 50) {
				return i+k <= j;
			}
			if (o == 0) {
				if (j >= 26)
					return i + k < 50;
				else 
					return k < Math.min(50 - i, 36 + j / 3 - 2 * i / 3);
//				return true;
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
Policy 1 first player win rate: 0.45862259444872694
Policy 2 first player win rate: 0.438547430903675
Policy 1 second player win rate: 0.561452569096325
Policy 2 second player win rate: 0.541377405551273
Policy 1 average win rate: 0.510037581772526
Policy 2 average win rate: 0.489962418227474
Average win rate difference: -0.02007516354505201
	 */
}
