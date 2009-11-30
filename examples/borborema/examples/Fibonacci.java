package borborema.examples;

import java.math.BigInteger;

import borborema.Task;

public class Fibonacci implements Task<BigInteger> {

	private static final long serialVersionUID = -6474866412273520236L;

	private final int n;

	public Fibonacci(int n) {
		this.n = n;
	}

	@Override
	public BigInteger execute() {
		if (n <= 2) {
			return new BigInteger("1");
		}

		BigInteger f = new BigInteger("0");
		BigInteger g = new BigInteger("1");

		for (int i = 1; i <= n; i++) {
			f = f.add(g);
			g = f.subtract(g);
		}
		return f;
	}

}
