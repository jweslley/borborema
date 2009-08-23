package embeddedbroker;

import java.io.Serializable;

public interface Job<TaskResult extends Serializable, JobResult> extends Iterable<Task<TaskResult>> {

	Job<TaskResult, JobResult> addTask(Task<TaskResult> task);

}
