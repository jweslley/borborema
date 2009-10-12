package embeddedbroker.samples;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.Future;

import embeddedbroker.BagOfTasks;
import embeddedbroker.GridService;
import embeddedbroker.OurGridService;

public class Main {

	public static void main(final String[] args) throws Exception {
		GridService service = new OurGridService();

		BagOfTasks<BigInteger> bot = new BagOfTasks<BigInteger>();
		bot.addTask(new PrimeSearch(new BigInteger("25")));
		bot.addTask(new PrimeSearch(new BigInteger("100")));
		bot.addTask(new PrimeSearch(new BigInteger("1000")));
		bot.addTask(new PrimeSearch(new BigInteger("10000000000000000000000")));

		Future<List<BigInteger>> botResult = service.submit(bot);

		List<BigInteger> list = botResult.get();
		System.out.println("result: " + list);
	}

}
