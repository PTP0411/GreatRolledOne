
public class GROPolicyQLP2 implements GROPolicy {

	@Override
	public boolean willRoll(int p, int i, int j, int k, int o) {
		if (p == 0) {
			// Player 1 model
			if (k <= 5) {
				return true;
			}
			else if (k <= 15) {
				if (o == 0)
					return (i < 40 || j > 10) ? true : false;
				if (o == 1)
					return (i < 40 && j > 35) ? true : false;
				return (35<=i && i<=40 && 45<=j && j<=50) ? true : false;
			}
			else if (k <= 25) {
				if (o == 0)
					return (i >= 30 && j <= 40) ? false : true;
				if (o == 1)
					return (j-i>=30 || j-i>=5) ? true : false;
				return (i<=35 && j>=45) ? true : false;
			}
			else if (k <= 35) {
				if (o == 0)
					return (i<=30 || j-i>=0) ? true : false;
				if (o == 1)
					return (j<=30 && j-i>=10) || (j>30 && j-i>=15) ? true : false;
				return (i<=20 && j>=45) ? true : false;
			}
			else if (k <= 45) {
				if (o == 0)
					return (i<=10 || j-i>=10) ? true : false;
				if (o == 1)
					return (j-i>=25) ? true : false;
				return (i<=15 && j>=45) ? true : false;
			}
			else {
				return (o < 2) ? true : false;
			}
		}
		else {
			// Player 2 model
			if (k <= 5) {
				return true;
			}
			else if (k <= 15) {
				if (o == 0)
					return true;
				if (o == 1)
					return (i<=30 || j>=20) ? true : false;
				return (j>=50) ? true : false;
			}
			else if (k <= 25) {
				if (o == 0)
					return (i<=30) ? true : false;
				if (o == 1)
					return (j<=40 && j-i>=10) || (j>40 && j-i>=20) ? true : false;
				return (j>=50) ? true : false;
			}
			else if (k <= 35) {
				if (o == 0)
					return (i<=10 || j>=25) ? true : false;
				if (o == 1)
					return (j-i>=30) ? true : false;
				return (j>=50) ? true : false;
			}
			else if (k <= 45) {
				if (o == 0)
					return (i<=10 && j>=25) ? true : false;
				if (o == 1)
					return (j-i>=40) ? true : false;
				return (j>=50) ? true : false;
			}
			else {
				return (o < 2) ? true : false;
			}
		}
	}
	/* Based on plots under ./shouldRoll-optimal/ dir
Policy 1 first player win rate: 0.4540895845323894
Policy 2 first player win rate: 0.3255195670383934
Policy 1 second player win rate: 0.6744804329616065
Policy 2 second player win rate: 0.5459104154676107
Policy 1 average win rate: 0.5642850087469979
Policy 2 average win rate: 0.43571499125300206
Average win rate difference: -0.12857001749399588 (worse: ~ -12.86% gap)
	 */
}
