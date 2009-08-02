package embeddedbroker;

import java.util.concurrent.Future;

public interface GridService {

	<Result> Future<Result> submit(Job<Result> job);

	void shutdown();

}
