package borborema.samples;

import java.math.BigInteger;

import borborema.JvmOptions;
import borborema.Task;


@JvmOptions("-server")
public class PrimeSearch implements Task<BigInteger> {

	private static final long serialVersionUID = -6102073508034805682L;

	private final BigInteger seed;

	public PrimeSearch(BigInteger seed) {
		this.seed = seed;
	}

	@Override
	public BigInteger execute() {
		return seed.nextProbablePrime();
	}

}
