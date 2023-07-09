
/**
 * GROPolicy - a common interface for evaluating and experimenting with various
 * Great Rolled Ones play policies.
 * @author Todd W. Neller
 */
public interface GROPolicy {
	/**
	 * @param p - player (0/1)
	 * @param i - current player score
	 * @param j - current opponent score
	 * @param k - turn total
	 * @param o - ones rolled this turn
	 * @return whether or not player will roll in this state
	 */
	boolean willRoll(int p, int i, int j, int k, int o);
}
