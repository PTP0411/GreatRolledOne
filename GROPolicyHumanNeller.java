package Game;

//started in 7/13/23 Zoom meeting
public class GROPolicyHumanNeller implements GROPolicy {

	@Override
	public boolean willRoll(int p, int i, int j, int k, int o) {
		if (p == 0) {
			if (o == 0) {
				return k < Math.max(50 - i, 29 - i + j);
			}
			else if (o == 1) {
				// maximum of a surface and a go-for-it region
				// surface: (i=0,j=0)->25 (0,49)->66 (49,0)->1 (49,49)->17
				// for j=0: ~ 25 - i/2
				// for j=49: ~ 25 + 4j/5 - i
				// Perhaps best to approximate with max of two planes (one for different signs of j - i)
				// j > i: 25 + 4 * j / 5 - i
				// j < i: 25 - i / 2 + j / 3
				int holdAt = (j > i) ? 25 + 4 * j / 5 - i : 25 - i / 2 + j / 3;
				// Go-for-it approximated as i > 35 or j > 25
				if (i >= 35 || j >= 25)
					holdAt = Math.max(holdAt, 50 - i);
				return k < holdAt;
			}
			else if (o == 2)
				// TODO - Improvement needed in endgame (before i=43, j=46) here.
				if (i + j >= 71)
					return k < 50 - i;
				else
					return k < Math.min(5, 50 - i);
			else
				return false;
		}
		else { // p == 1
			if (j >= 50) // must exceed
				return i + k <= j;
			if (o == 0) {
				if (j >= 26)
					return i + k < 50;
				else 
					return k < Math.min(50 - i, 36 + j / 3 - 2 * i / 3);
			}
			else if (o == 1) {
				// End game:
				if (i >= 40 || j >= 36)
					return k < Math.max(50, j + 1) - i;
				else
					// Again, roughly approximate behind/ahead surfaces
//					return k < 19 + ((j > i) ? (3 * j / 2 - i) : (j / 4 - i / 3)); // -0.008714340246090302
//					return k < 19 + ((j > i) ? (j / 2 - i) : (j / 4 - i / 3)); //-0.010177040066061627
					return k < 19 + ((j > i) ? (j - i) : (j / 4 - i / 3)); // -0.007122094100461995
			}
			else if (o == 2) {
				// TODO - There's room for endgame improvement here.
				if (i + j >= 85)
					return k < 50 - i;
				return k < Math.min(5, 50 - i);
			}
			else
				return false;
		}
	}

	public static void main(String[] args) {
		GROPolicy optimal = new GROPolicyOptimal();
		GROPolicy approx = new GROPolicyHumanNeller();
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
