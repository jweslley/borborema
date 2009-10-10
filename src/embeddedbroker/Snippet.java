package embeddedbroker;

import java.util.List;
import java.util.concurrent.Future;

public class Snippet {

	public static void main(final String[] args) {
		GridService service = new OurGridService();

		Future<List<String>> botResult = service.submit(new BagOfTasks<String>());

	}

}
