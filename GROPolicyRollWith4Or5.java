
public class GROPolicyRollWith4Or5 implements GROPolicy {

	@Override
	public boolean willRoll(int p, int i, int j, int k, int o) {
		if (p == 0 && i + k >= 50)
			return false;
		if (p == 1 && ((j >= 50 && i + k > j) || (j < 50 && i + k >= 50)))
			return false;
		return o < 2;
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
