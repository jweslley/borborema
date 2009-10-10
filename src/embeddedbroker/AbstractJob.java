package embeddedbroker;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public abstract class AbstractJob<TaskResult extends Serializable, JobResult> implements Job<TaskResult, JobResult> {

	private final Set<Task<TaskResult>> tasks = new HashSet<Task<TaskResult>>();

	public final Job<TaskResult, JobResult> addTask(Task<TaskResult> task) {
		if (task == null) {
			throw new IllegalArgumentException("Task must not be null");
		}

		tasks.add(task);
		return this;
	}

	public final Iterator<Task<TaskResult>> iterator() {
		return tasks.iterator();
	}

}
