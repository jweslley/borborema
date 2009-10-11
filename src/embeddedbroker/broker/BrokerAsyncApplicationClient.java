package embeddedbroker.broker;

import org.ourgrid.broker.BrokerConstants;
import org.ourgrid.common.interfaces.management.BrokerManager;
import org.ourgrid.common.interfaces.to.JobEndedInterested;
import org.ourgrid.common.spec.job.JobSpec;

import br.edu.ufcg.lsd.commune.container.ObjectDeployment;
import br.edu.ufcg.lsd.commune.container.servicemanager.client.InitializationContext;
import br.edu.ufcg.lsd.commune.container.servicemanager.client.async.AsyncApplicationClient;
import br.edu.ufcg.lsd.commune.container.servicemanager.client.sync.SyncContainerUtil;
import br.edu.ufcg.lsd.commune.context.ModuleContext;
import br.edu.ufcg.lsd.commune.network.xmpp.CommuneNetworkException;
import br.edu.ufcg.lsd.commune.processor.ProcessorStartException;

public final class BrokerAsyncApplicationClient extends AsyncApplicationClient<BrokerManager, BrokerAsyncManagerClient> {

	private boolean isBrokerUp;

	public BrokerAsyncApplicationClient(ModuleContext context) throws CommuneNetworkException, ProcessorStartException {
		super("BROKER_CLIENT", context);
	}

	@Override
	protected InitializationContext<BrokerManager, BrokerAsyncManagerClient> createInitializationContext() {
		return new BrokerAsyncInitializationContext();
	}

	/**
	 * Adds job described in the job specification
	 */
	public synchronized int addJob(JobSpec theJob) {
		BrokerAsyncManagerClient managerClient = getManagerClient();
		getManager().addJob(managerClient, theJob);
		return SyncContainerUtil.busyWaitForResponseObject(managerClient.getBlockingQueue(), Integer.class);
	}

	public void notifyWhenJobIsFinished(int jobID) {
		ObjectDeployment objectDeployment = getContainer().getObjectRepository().get(BrokerConstants.JOB_ENDED_INTERESTED);
		JobEndedInterested jobEndedInterested = (JobEndedInterested) objectDeployment.getObject();
		getManager().notifyWhenJobIsFinished(getManagerClient(), jobEndedInterested, jobID);
	}

	public boolean isBrokerUp() {
		return isBrokerUp;
	}

	public void setBrokerUp(boolean isBrokerUp) {
		this.isBrokerUp = isBrokerUp;
	}

	public void waitUpTime(long interval) { // TODO uptime
		try {
			while (!isBrokerUp) {
				Thread.sleep(interval);
			}

		} catch (InterruptedException e) {
			throw new IllegalStateException(e);
		}
	}

}
