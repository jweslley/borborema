package embeddedbroker;

import java.util.concurrent.Future;

public interface GridService {

	<JobResult, TaskResult> Future<JobResult> submit(Job<TaskResult, JobResult> job);

	void shutdown();

}
