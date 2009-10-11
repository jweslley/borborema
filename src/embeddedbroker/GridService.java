package embeddedbroker;

import java.io.Serializable;
import java.util.concurrent.Future;

public interface GridService {

	<JobResult, TaskResult extends Serializable>
	Future<JobResult> submit(Job<TaskResult, JobResult> job);

	<JobResult, TaskResult extends Serializable>
	Future<JobResult> submit(String requirements, Job<TaskResult, JobResult> job);

	void shutdown();

}
