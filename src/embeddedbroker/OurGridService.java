package embeddedbroker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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

import br.edu.ufcg.lsd.commune.context.ModuleContext;
import br.edu.ufcg.lsd.commune.context.PropertiesFileParser;
import br.edu.ufcg.lsd.commune.network.xmpp.CommuneNetworkException;
import br.edu.ufcg.lsd.commune.processor.ProcessorStartException;
import embeddedbroker.broker.BrokerAsyncApplicationClient;
import embeddedbroker.executor.TaskRunner;
import embeddedbroker.executor.codec.JavaSerializationCodec;
import embeddedbroker.executor.codec.SerializationCodec;

public class OurGridService implements GridService, JobEndedInterested {

	private final SerializationCodec codec;
	private final BrokerAsyncApplicationClient client;
	private final List<File> libraries = Arrays.asList(new File("embedded-broker.jar"));

	public OurGridService() {
		this(BrokerConfiguration.PROPERTIES_FILENAME);
	}

	public OurGridService(String configurationFilename) {
		try {
			PropertiesFileParser properties = new PropertiesFileParser(BrokerConfiguration.PROPERTIES_FILENAME);
			ModuleContext context = new BrokerComponentContextFactory(properties).createContext();
			client = new BrokerAsyncApplicationClient(context);
			client.waitUpTime(200);
			client.getContainer().deploy(BrokerConstants.JOB_ENDED_INTERESTED, this);
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

		Pair<JobSpec, List<File>> jobSpec = createJobSpec(job);
		int jobID = client.addJob(jobSpec._1);
		GridFuture<TaskResult, JobResult> future = new GridFuture<TaskResult, JobResult>(jobID, job, jobSpec._2);
		jobs.add(future);
		client.notifyWhenJobIsFinished(jobID);
		return future;
	}

	private <JobResult, TaskResult extends Serializable>
	Pair<JobSpec, List<File>> createJobSpec(Job<TaskResult, JobResult> job) throws GridServiceException {

		List<TaskSpec> tasks = new ArrayList<TaskSpec>();
		List<File> outputs = new ArrayList<File>();

		Pair<TaskSpec, File> taskSpec;
		for (Task<TaskResult> task : job) {
			taskSpec = createTaskSpec(task);
			tasks.add(taskSpec._1);
			outputs.add(taskSpec._2);
		}

		if (tasks.size() == 0) {
			throw new GridServiceException("No tasks to process.");
		}

		JobSpec jobSpec = new JobSpec(job.toString());
		try {
			jobSpec.setTaskSpecs(tasks);

		} catch (JobSpecificationException e) {
			throw new GridServiceException(e);
		}

		return new Pair<JobSpec, List<File>>(jobSpec, outputs);
	}

	private <TaskResult extends Serializable>
	Pair<TaskSpec, File> createTaskSpec(Task<TaskResult> task) throws GridServiceException {

		File input = createTempFile();
		File output = createTempFile();

		try {
			codec.writeObject(task, new FileOutputStream(input));
		} catch (IOException e) {
			throw new GridServiceException("Could not write in temporary file.", e);
		}

		String fInput = "$JOB-$TASK.in";
		String fOutput = "$JOB-$TASK.out";

		IOBlock initBlock = new IOBlock();
		initBlock.putEntry(new IOEntry("put", input.getAbsolutePath(), fInput));

		for (File library : libraries) {
			initBlock.putEntry(new IOEntry("store", library.getAbsolutePath(), library.getName()));
		}

		/*for (File resource : resourceList) {
			initBlock.putEntry(new IOEntry("put", resource.getAbsolutePath(), resource.getName()));
		} createJarList()*/

		String remoteCommand = "java -cp .:$STORAGE/embedded-broker.jar " + TaskRunner.class.getName() + " " + fInput + " " + fOutput;

		IOBlock finalBlock = new IOBlock();
		finalBlock.putEntry(new IOEntry("get", fOutput, output.getAbsolutePath()));

		try {
			return new Pair<TaskSpec, File>(new TaskSpec(initBlock, remoteCommand, finalBlock, null), output);

		} catch (TaskSpecificationException e) {
			throw new GridServiceException("Could not create task specification.", e);
		}
	}

	private File createTempFile() throws GridServiceException {
		try {
			return File.createTempFile(UUID.randomUUID().toString(), ".task");

		} catch (IOException e) {
			throw new GridServiceException("Could not create temp file!", e);
		}
	}

	private static class Pair<A, B> {

		public A _1;
		public B _2;

		public Pair(A a, B b) {
			this._1 = a;
			this._2 = b;
		}

	}


	private class GridFuture<TaskResult extends Serializable, JobResult>
	implements Future<JobResult>, JobEndedInterested {

		private final int jobID;
		private final List<File> output;
		private final Job<TaskResult, JobResult> job;

		public GridFuture(int jobId, Job<TaskResult, JobResult> job, List<File> output) {
			this.job = job;
			this.jobID = jobId;
			this.output = output;
		}

		public boolean cancel(boolean mayInterruptIfRunning) {
			throw new UnsupportedOperationException();
		}

		public JobResult get() throws InterruptedException, ExecutionException {
			/*
			System.out.println("waiting for job " + jobID);
			client.waitForJob(jobID);
			System.out.println("job finished");
			return (JobResult) result();*/
			throw new UnsupportedOperationException();
		}

		public JobResult get(long timeout, TimeUnit unit) throws InterruptedException,
		ExecutionException, TimeoutException {
			// TODO Auto-generated method stub
			return null;
		}

		public boolean isCancelled() {
			// TODO Auto-generated method stub
			return false;
		}

		public boolean isDone() {
			// TODO Auto-generated method stub
			return false;
		}

		private List<Object> result() {

			System.out.println("getting results");
			List<Object> results = new ArrayList<Object>();
			try {
				for (File resultFile : output) {
					results.add(codec.readObject(new FileInputStream(resultFile)));
				}

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("results: " + results);
			return results;
		}

		@Override
		public void jobEnded(int jobID, GridProcessState state) {
			if (this.jobID != jobID) {
				return;
			}

			System.out.println("FUTURE: O job com id " + jobID + " terminou com o estado " + state.name());
			result();
		}

		@Override
		public void schedulerHasBeenShutdown() {
			// ignore
		}

	}

	private final List<GridFuture> jobs = new ArrayList<GridFuture>();

	@Override
	public void jobEnded(int jobID, GridProcessState state) {
		System.out.println("O job com id " + jobID + " terminou com o estado " + state.name());

		for (GridFuture future : jobs) {
			if (future.jobID == jobID) {
				future.result();
			}
		}
	}

	@Override
	public void schedulerHasBeenShutdown() {
		// TODO Auto-generated method stub

	}

}
