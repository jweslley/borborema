package embeddedbroker;

import java.io.Serializable;
import java.util.concurrent.Future;

public class OurGridService implements GridService {

	public void shutdown() {
		// TODO Auto-generated method stub
	}

	public final <JobResult, TaskResult extends Serializable> Future<JobResult> submit(
			Job<TaskResult, JobResult> job) {
		return submit(null, job);
	}

	public final <JobResult, TaskResult extends Serializable> Future<JobResult> submit(
			String requirements, Job<TaskResult, JobResult> job) {
		return null;
	}

}
