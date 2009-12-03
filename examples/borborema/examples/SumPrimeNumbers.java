package borborema.examples;

import java.io.File;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.Future;

import borborema.GridService;
import borborema.MapReduceJob;
import borborema.OurGridService;

public class SumPrimeNumbers extends MapReduceJob<BigInteger, BigInteger> {

	@Override
	public BigInteger reduce(List<BigInteger> intermediateResults) {
		BigInteger sum = new BigInteger("0");
		for (BigInteger value : intermediateResults) {
			sum = sum.add(value);
		}
		return sum;
	}

	public static void main(final String[] args) throws Exception {
		GridService service = new OurGridService();

		SumPrimeNumbers job = new SumPrimeNumbers();
		job
		.addJarFile(new File("borborema.jar"))
		.addTask(new PrimeSearch(new BigInteger("10000000")))
		.addTask(new PrimeSearch(new BigInteger("200000000000")))
		.addTask(new PrimeSearch(new BigInteger("300000000000000000")))
		.addTask(new PrimeSearch(new BigInteger("40000000000000000000000")));

		Future<BigInteger> result = service.submit(job);

		BigInteger sum = result.get();
		System.out.println("result: " + sum);
	}

}
