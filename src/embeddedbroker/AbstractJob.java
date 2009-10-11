package embeddedbroker;

import static embeddedbroker.util.Preconditions.check;
import static embeddedbroker.util.Preconditions.checkNotNull;

import java.io.File;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public abstract class AbstractJob<TaskResult extends Serializable, JobResult> implements Job<TaskResult, JobResult> {

	private final Set<File> libraries = new HashSet<File>();
	private final Set<Task<TaskResult>> tasks = new HashSet<Task<TaskResult>>();

	public final Job<TaskResult, JobResult> addTask(Task<TaskResult> task) {
		checkNotNull("Task must not be null", task);

		tasks.add(task);
		return this;
	}

	public final Iterator<Task<TaskResult>> iterator() {
		return tasks.iterator();
	}

	@Override
	public final Iterable<File> getLibraries() {
		return libraries;
	}

	@Override
	public final Job<TaskResult, JobResult> addLibrary(File file) {
		checkNotNull("Library must not be null", file);
		check("File not found " + file, file.exists());

		libraries.add(file);
		return this;
	}

	@Override
	public final Job<TaskResult, JobResult> addLibrary(Class<?> klass) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

}
