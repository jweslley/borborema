package embeddedbroker.samples;

import java.util.List;
import java.util.concurrent.Future;

import embeddedbroker.OurGridService;
import embeddedbroker.BagOfTasks;
import embeddedbroker.GridService;

public class Snippet {

	public static void main(final String[] args) {
		GridService service = new OurGridService();

		BagOfTasks<Integer> bot = new BagOfTasks<Integer>();
		bot.addTask(new SumTask(8, 5));

		Future<List<Integer>> botResult = service.submit(bot);

	}

}
