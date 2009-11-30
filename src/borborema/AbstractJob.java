package borborema;

import static borborema.util.Preconditions.check;
import static borborema.util.Preconditions.checkNotNull;

import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public abstract class AbstractJob<TaskResult extends Serializable, JobResult> implements Job<TaskResult, JobResult> {

	private final Set<File> jarFiles = new HashSet<File>();
	private final Set<Task<TaskResult>> tasks = new HashSet<Task<TaskResult>>();

	public final Job<TaskResult, JobResult> addTask(Task<TaskResult> task) {
		checkNotNull("Task must not be null", task);

		tasks.add(task);
		// TODO uncomment next line in production mode
		// addLibrary(task.getClass());
		return this;
	}

	public final Iterator<Task<TaskResult>> iterator() {
		return tasks.iterator();
	}

	@Override
	public final Iterable<File> getJarFiles() {
		return jarFiles;
	}

	@Override
	public final Job<TaskResult, JobResult> addJarFile(File file) {
		checkNotNull("Library must not be null", file);
		check("File not found " + file, file.exists());

		jarFiles.add(file);
		return this;
	}

	@Override
	public final Job<TaskResult, JobResult> addJarFile(Class<?> klass) {
		checkNotNull("Class must not be null", klass);

		URL resource = getClass().getResource('/' + klass.getName().replace('.', '/') + ".class");

		checkNotNull("Class not found in classpath: " + klass, resource);
		check("Class is not placed inside a jar: " + klass, resource.getProtocol().equals("jar"));

		String file = resource.getFile().substring("file:".length());
		String jarFilename = file.substring(0, file.indexOf('!'));
		return addJarFile(new File(jarFilename));
	}

}
