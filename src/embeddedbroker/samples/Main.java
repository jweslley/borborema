package embeddedbroker.samples;

import java.util.List;
import java.util.concurrent.Future;

import embeddedbroker.BagOfTasks;
import embeddedbroker.GridService;
import embeddedbroker.OurGridService;

public class Main {

	public static void main(final String[] args) throws Exception {
		GridService service = new OurGridService();

		BagOfTasks<Integer> bot = new BagOfTasks<Integer>();
		bot.addTask(new SumTask(8, 10));

		Future<List<Integer>> botResult = service.submit(bot);

		Thread.sleep(20000);
		boolean cancel = botResult.cancel(true);
		System.out.println("cancel: " + cancel);
	}

}
