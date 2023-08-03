
public class GROPolicyQLP1_1 implements GROPolicy {

	@Override
	public boolean willRoll(int p, int i, int j, int k, int o) {
		if (p == 0) {
			if (o == 0) {
				int holdAt = Math.max(50 - i, 29 - i + j);
				return k < holdAt;
			}
			else if (o == 1) {
				return true;
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
Policy 1 first player win rate: 0.45869356511422976
Policy 2 first player win rate: 0.4206026814851582
Policy 1 second player win rate: 0.5793973185148418
Policy 2 second player win rate: 0.5413064348857702
Policy 1 average win rate: 0.5190454418145358
Policy 2 average win rate: 0.48095455818546423
Average win rate difference: -0.03809088362907159
	 */
}
