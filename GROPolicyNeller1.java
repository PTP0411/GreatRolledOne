
public class GROPolicyNeller1 implements GROPolicy {
	
	@Override
	public boolean willRoll(int p, int i, int j, int k, int o) {
		double iFrac = i / 50.0;
		double jFrac = j / 50.0;
		if (p == 0) {
			// Player 1 model
			if (o == 0) {
				int holdAt = (i + 3 * j < 15) ? 44 : (int) Math.max(50,  j + 28 * (1 + (iFrac * (1 - jFrac))));
				return k < holdAt;
			}
			else if (o == 1) {
				int holdAt = (int) ((1 - jFrac) * 25 * (1 + iFrac) + jFrac * 66);
				return k < holdAt;
			}
			else { // o == 2
				return k < 5;
			}
		}
		else {
			// Player 2 model
			if (j >= 50)
				return k <= j;
			else if (o == 0) {
				int holdAt = (int) Math.min(50, 36 + .4 * i + .5 * j);
				return k < holdAt;
			}
			else if (o == 1) {
				int holdAt = (int) Math.min(50, 36 + .5 * i + .8 * j);
				return k < holdAt;
			}
			else { // o == 2
				return k < 5;
			}
		}

		// This policy is not suitable for mental math.
		// Starting as player 1, using player 1 model only, optimal wins ~51.77% of games.
		// Starting as player 1, using player 1 model only, approx wins ~48.23% of games. (~ -3.53% gap)
		// Adding player 2 policy as distinct:
		/*
Policy 1 first player win rate: 0.4582826689247181
Policy 2 first player win rate: 0.42144630453623805
Policy 1 second player win rate: 0.578553695463762
Policy 2 second player win rate: 0.5417173310752819
Policy 1 average win rate: 0.5184181821942401
Policy 2 average win rate: 0.4815818178057599
Average win rate difference: -0.03683636438848015 (worse: ~ -3.68% gap)
Greatest error in p1o1 sheet.
Should I create a critic tool that identifies/clusters greatest/most-frequent errors in policy and reports them?
Idea: Sample reachable, probably play through simulated play between the policies.  
For each suboptimal policy first/second player, and for each number of ones set aside, 
highlight greatest error in play (the state, the decision, and the error measure itself (optimal win rate minus chosen))
 
		 */
	}

}
