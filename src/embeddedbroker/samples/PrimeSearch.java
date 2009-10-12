package embeddedbroker.samples;

import java.math.BigInteger;

import embeddedbroker.JvmOptions;
import embeddedbroker.Task;

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
