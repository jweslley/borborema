package embeddedbroker;

import java.util.List;

public interface Job<TaskResult, JobResult> {

	Job<TaskResult, JobResult> addTask(Task<TaskResult> task);

	List<Task<TaskResult>> getTasks();

}
