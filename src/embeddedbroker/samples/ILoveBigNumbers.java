package embeddedbroker.samples;

import java.math.BigInteger;

import embeddedbroker.Task;

/**
 * Inspired by: http://icpcres.ecs.baylor.edu/onlinejudge/external/102/10220.html
 */
public class ILoveBigNumbers implements Task<Long> {

	private static final long serialVersionUID = 6157597386179109048L;

	private final int n;

	public ILoveBigNumbers(int n) {
		this.n = n;
	}

	@Override
	public Long execute() {
		int aux = n;
		BigInteger factorial = new BigInteger("1");
		while (aux-- > 1) {
			factorial = factorial.multiply(new BigInteger(Integer.toString(aux+1)));
		}

		String factorialAsStr = factorial.toString();
		long sum = 0;
		for (int j = 0; j < factorialAsStr.length(); j++) {
			sum += factorialAsStr.charAt(j) - '0';
		}
		return sum;
	}

}
