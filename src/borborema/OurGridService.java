package borborema;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

import org.apache.log4j.Logger;
import org.ourgrid.broker.BrokerComponentContextFactory;
import org.ourgrid.broker.BrokerConfiguration;
import org.ourgrid.broker.BrokerConstants;
import org.ourgrid.common.interfaces.to.GridProcessState;
import org.ourgrid.common.interfaces.to.JobEndedInterested;
import org.ourgrid.common.spec.exception.JobSpecificationException;
import org.ourgrid.common.spec.exception.TaskSpecificationException;
import org.ourgrid.common.spec.job.IOBlock;
import org.ourgrid.common.spec.job.IOEntry;
import org.ourgrid.common.spec.job.JobSpec;
import org.ourgrid.common.spec.job.TaskSpec;

import borborema.broker.BrokerAsyncApplicationClient;
import borborema.executor.TaskRunner;
import borborema.executor.codec.JavaSerializationCodec;
import borborema.executor.codec.SerializationCodec;
import borborema.util.Annotations;
import br.edu.ufcg.lsd.commune.context.ModuleContext;
import br.edu.ufcg.lsd.commune.context.PropertiesFileParser;
import br.edu.ufcg.lsd.commune.network.xmpp.CommuneNetworkException;
import br.edu.ufcg.lsd.commune.processor.ProcessorStartException;

public class OurGridService implements GridService {

	private static Logger logger = Logger.getLogger(OurGridService.class);

	private static final int WAIT_TIME_INTERVAL = 200;

	private final JobHandler jobHandler;
	private final SerializationCodec codec;
	private final BrokerAsyncApplicationClient client;

	public OurGridService() {
		this(BrokerConfiguration.PROPERTIES_FILENAME);
	}

	public OurGridService(String configurationFilename) {
		try {
			PropertiesFileParser properties = new PropertiesFileParser(configurationFilename);
			ModuleContext context = new BrokerComponentContextFactory(properties).createContext();
			client = new BrokerAsyncApplicationClient(context);
			client.waitUpTime(WAIT_TIME_INTERVAL);

			jobHandler = new JobHandler();
			client.getContainer().deploy(BrokerConstants.JOB_ENDED_INTERESTED, jobHandler);
			codec = new JavaSerializationCodec();  // TODO @Inject

		} catch (CommuneNetworkException e) {
			throw new GridServiceException("Cannot contact Broker", e);

		} catch (ProcessorStartException e) {
			throw new GridServiceException("Cannot contact Broker", e);
		}
	}

	public final void shutdown() {
		try {
			client.stop();

		} catch (CommuneNetworkException e) {
			throw new GridServiceException("Cannot contact Broker", e);
		}
	}

	public final <JobResult, TaskResult extends Serializable>
	Future<JobResult> submit(Job<TaskResult, JobResult> job) {
		return submit(null, job);
	}

	public final <JobResult, TaskResult extends Serializable>
	Future<JobResult> submit(String requirements, Job<TaskResult, JobResult> job) {

		Pair<JobSpec, List<File>> jobSpec = createJobSpec(job, requirements);
		int jobId = client.addJob(jobSpec._1);
		GridFuture<TaskResult, JobResult> future = new GridFuture<TaskResult, JobResult>(jobId);
		jobHandler.addJob(job, future, jobSpec._2);
		client.notifyWhenJobIsFinished(jobId);
		return future;
	}

	private <JobResult, TaskResult extends Serializable>
	Pair<JobSpec, List<File>> createJobSpec(Job<TaskResult, JobResult> job, String requirements) throws GridServiceException {

		List<TaskSpec> tasks = new ArrayList<TaskSpec>();
		List<File> outputs = new ArrayList<File>();

		Pair<TaskSpec, File> taskSpec;
		for (Task<TaskResult> task : job) {
			taskSpec = createTaskSpec(job, task);
			tasks.add(taskSpec._1);
			outputs.add(taskSpec._2);
		}

		if (tasks.size() == 0) {
			throw new GridServiceException("No tasks to process.");
		}

		if (requirements == null) {
			requirements = Annotations.getRequirements(job);
		}

		try {
			JobSpec jobSpec = new JobSpec(job.toString(), requirements, tasks);
			return new Pair<JobSpec, List<File>>(jobSpec, outputs);

		} catch (JobSpecificationException e) {
			throw new GridServiceException(e);
		}
	}

	private <JobResult, TaskResult extends Serializable>
	Pair<TaskSpec, File> createTaskSpec(Job<TaskResult, JobResult> job, Task<TaskResult> task) throws GridServiceException {

		File taskFile = createTempFile(".task");
		File taskResult = createTempFile(".task");

		List<Pair<File, File>> inputFiles = serializeTask(task, taskFile);

		IOBlock initBlock = new IOBlock();
		initBlock.putEntry(new IOEntry("put", taskFile.getAbsolutePath(), taskFile.getName()));

		StringBuilder classpath = new StringBuilder(".");
		for (File jarFile : job.getJarFiles()) {
			initBlock.putEntry(new IOEntry("store", jarFile.getAbsolutePath(), jarFile.getName()));
			classpath.append(":$STORAGE/").append(jarFile.getName());
		}

		for (Pair<File, File> input : inputFiles) {
			initBlock.putEntry(new IOEntry("put", input._1.getAbsolutePath(), input._2.getName()));
		}

		String defaultOptions = Annotations.getJvmOptions(job, "");
		String jvmOptions = Annotations.getJvmOptions(task, defaultOptions);
		String remoteCommand = "java " + jvmOptions + " -cp " + classpath.toString()
		+ " " + TaskRunner.class.getName() + " " + taskFile.getName() + " " + taskResult.getName();

		IOBlock finalBlock = new IOBlock();
		finalBlock.putEntry(new IOEntry("get", taskResult.getName(), taskResult.getAbsolutePath()));

		try {
			return new Pair<TaskSpec, File>(new TaskSpec(initBlock, remoteCommand, finalBlock, null), taskResult);

		} catch (TaskSpecificationException e) {
			throw new GridServiceException("Could not create task specification.", e);
		}
	}

	private List<Pair<File, File>> serializeTask(Task<?> task, File taskFile) {

		try {
			List<Pair<File, File>> result = new ArrayList<Pair<File, File>>();
			Map<Field, File> localFiles = Annotations.getFilesFrom(task);
			Map<Field, File> remoteFiles = createRemoteFiles(localFiles, result);

			setFiles(task, remoteFiles);
			codec.writeObject(task, new FileOutputStream(taskFile));
			setFiles(task, localFiles);
			return result;

		} catch (IllegalAccessException e) {
			throw new GridServiceException("Reflection error. Check the SecurityManager.", e);

		} catch (FileNotFoundException e) {
			throw new GridServiceException("Input file is required.", e);

		} catch (IOException e) {
			throw new GridServiceException("Could not write in temporary file.", e);
		}
	}

	private Map<Field, File> createRemoteFiles(Map<Field, File> localFiles, List<Pair<File, File>> callback) {
		Set<Entry<Field, File>> fileSet = localFiles.entrySet();
		Map<Field, File> remoteFiles = new HashMap<Field, File>();
		for (Entry<Field, File> entry : fileSet) {
			File remoteFile = new File(UUID.randomUUID().toString() + ".input");
			remoteFiles.put(entry.getKey(), remoteFile);
			callback.add(new Pair<File, File>(entry.getValue(), remoteFile));
		}
		return remoteFiles;
	}

	private void setFiles(Task<?> task, Map<Field, File> files) throws IllegalAccessException {
		Set<Entry<Field, File>> entrySet = files.entrySet();
		for (Entry<Field, File> entry : entrySet) {
			entry.getKey().set(task, entry.getValue());
		}
	}

	private File createTempFile(String suffix) throws GridServiceException {
		try {
			return File.createTempFile(UUID.randomUUID().toString(), suffix);

		} catch (IOException e) {
			throw new GridServiceException("Could not create temp file!", e);
		}
	}

	private static class Pair<A, B> {

		public final A _1;
		public final B _2;

		public Pair(A a, B b) {
			this._1 = a;
			this._2 = b;
		}
	}

	private static class Triple<A, B, C> extends Pair<A, B> {

		public final C _3;

		public Triple(A a, B b, C c) {
			super(a, b);
			this._3 = c;
		}
	}

	private class GridFuture<TaskResult extends Serializable, JobResult>
	extends AbstractQueuedSynchronizer implements Future<JobResult> {

		private static final long serialVersionUID = 8348057611360087600L;

		/** State value representing that task is running */
		private static final int RUNNING   = 1;
		/** State value representing that task was finished */
		private static final int FINISHED  = 2;
		/** State value representing that task was cancelled */
		private static final int CANCELLED = 4;

		/** The job id for GridFuture */
		public final int jobId;
		/** The result to return from get() */
		private JobResult result;
		/** The exception to throw from get() */
		private Throwable exception;

		public GridFuture(int jobId) {
			this.jobId = jobId;
			compareAndSetState(0, RUNNING);
		}

		public JobResult get() throws InterruptedException, ExecutionException {
			acquireSharedInterruptibly(0);
			if (getState() == CANCELLED) {
				throw new CancellationException();
			}
			if (exception != null) {
				throw new ExecutionException(exception);
			}

			return result;
		}

		public JobResult get(long timeout, TimeUnit unit) throws InterruptedException,
		ExecutionException, TimeoutException {
			if (!tryAcquireSharedNanos(0, unit.toNanos(timeout))) {
				throw new TimeoutException();
			}
			if (getState() == CANCELLED) {
				throw new CancellationException();
			}
			if (exception != null) {
				throw new ExecutionException(exception);
			}

			return result;
		}

		public boolean cancel(boolean mayInterruptIfRunning) {

			for (;;) {
				int s = getState();
				if (finishedOrCancelled(s)) {
					return false;
				}
				if (compareAndSetState(s, CANCELLED)) {
					break;
				}
			}

			if (mayInterruptIfRunning) {
				client.cancelJob(jobId);
			}

			releaseShared(0);
			return true;
		}

		private boolean finishedOrCancelled(int state) {
			return (state & (FINISHED | CANCELLED)) != 0;
		}

		public boolean isCancelled() {
			return (getState() == CANCELLED);
		}

		public boolean isDone() {
			return finishedOrCancelled(getState()) && !jobHandler.contains(jobId);
		}

		/**
		 * Implements AQS base release to always signal after setting
		 * final done status by removing job.
		 */
		@Override
		protected boolean tryReleaseShared(int ignore) {
			jobHandler.removeJob(jobId);
			return true;
		}

		/**
		 * Implements AQS base acquire to succeed if is done
		 */
		@Override
		protected int tryAcquireShared(int ignore) {
			return isDone() ? 1 : -1;
		}

		protected void setResult(JobResult jobResult) {

			if (getState() != RUNNING) {
				releaseShared(0); // cancel
			}

			for (;;) {
				int s = getState();
				if (s == FINISHED) {
					return;
				}

				if (s == CANCELLED) {
					releaseShared(0);
					return;
				}

				if (compareAndSetState(s, FINISHED)) {
					result = jobResult;
					releaseShared(0);
					return;
				}
			}
		}

		protected void setException(Throwable t) {

			if (getState() != RUNNING) {
				releaseShared(0); // cancel
			}

			for (;;) {
				int s = getState();
				if (s == FINISHED) {
					return;
				}

				if (s == CANCELLED) {
					releaseShared(0);
					return;
				}

				if (compareAndSetState(s, FINISHED)) {
					exception = t;
					result = null;
					releaseShared(0);
					return;
				}
			}
		}

	}

	@SuppressWarnings("unchecked")
	public final class JobHandler implements JobEndedInterested {

		private final Map<Integer, Triple<Job, GridFuture, List<File>>> jobs =
			new HashMap<Integer, Triple<Job, GridFuture, List<File>>>();

		public void addJob(Job job, GridFuture future, List<File> output) {
			jobs.put(future.jobId, new Triple<Job, GridFuture, List<File>>(job, future, output));
		}

		public void removeJob(int jobId) {
			jobs.remove(jobId);
		}

		public boolean contains(int jobId) {
			return jobs.containsKey(jobId);
		}

		public Triple<Job, GridFuture, List<File>> getJob(int jobId) {
			Triple<Job, GridFuture, List<File>> job = jobs.get(jobId);
			if (job == null) {
				throw new GridServiceException("Job not found: " + jobId);
			}
			return job;
		}

		@Override
		public void jobEnded(int jobId, GridProcessState state) {
			logger.info("Job [" + jobId + "] was " + state.name());

			Triple<Job, GridFuture, List<File>> job = getJob(jobId);
			if (state == GridProcessState.FINISHED) {
				readAndSetResults(job);

			} else if ((state == GridProcessState.FAILED)
					|| (state == GridProcessState.SABOTAGED)) {
				job._2.setException(new GridServiceException(state.name()));
			}
		}

		private void readAndSetResults(Triple<Job, GridFuture, List<File>> job) {

			try {
				List<Object> results = new ArrayList<Object>();
				for (File result : job._3) {
					results.add(codec.readObject(new FileInputStream(result)));
				}

				job._2.setResult(reduceOrGet(job._1, results));

			} catch (RuntimeException e) {
				job._2.setException(e);

			} catch (FileNotFoundException e) {
				job._2.setException(e);

			} catch (IOException e) {
				job._2.setException(e);
			}
		}

		private Object reduceOrGet(Job job, List<Object> intermediateResults) {

			if (job instanceof MapReduceJob) {
				try {
					// TODO execute in main thread?!
					return ((MapReduceJob) job).reduce(intermediateResults);

				} catch (Exception e) {
					throw new RuntimeException("An error occurred during reduce phase", e);
				}
			}

			return intermediateResults;
		}

		@Override
		public void schedulerHasBeenShutdown() {
			logger.debug("OurGridService.JobHandler.schedulerHasBeenShutdown()");
		}

	}

}
