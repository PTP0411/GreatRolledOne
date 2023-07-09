
public class GROPolicyOptimal implements GROPolicy {

	public static GreatRolledOnesSolver solver = new GreatRolledOnesSolver();
	
	@Override
	public boolean willRoll(int p, int i, int j, int k, int o) {
		return solver.willRoll(p, i, j, k, o);
	}
}
