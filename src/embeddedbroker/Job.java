package embeddedbroker;

import java.io.File;
import java.io.Serializable;

public interface Job<TaskResult extends Serializable, JobResult> extends Iterable<Task<TaskResult>> {

	Job<TaskResult, JobResult> addTask(Task<TaskResult> task);

	Job<TaskResult, JobResult> addJarFile(File file);

	Job<TaskResult, JobResult> addJarFile(Class<?> klass);

	//  Job<TaskResult, JobResult> addNativeLibrary(File file); // TODO

	//	Job<TaskResult, JobResult> addInput(File input);  // TODO

	//	Job<TaskResult, JobResult> addOutput(File output); // TODO

	Iterable<File> getJarFiles();

}
