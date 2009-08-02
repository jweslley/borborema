package embeddedbroker;

import java.util.List;

public interface Job<Result> {

	void addTask(Task<Result> task);

	List<Task<Result>> getTasks();

}
