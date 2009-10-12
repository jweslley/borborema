package embeddedbroker.broker;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;
import org.ourgrid.common.interfaces.control.BrokerControlClient;
import org.ourgrid.common.interfaces.management.BrokerManager;

import br.edu.ufcg.lsd.commune.api.FailureNotification;
import br.edu.ufcg.lsd.commune.api.RecoveryNotification;
import br.edu.ufcg.lsd.commune.container.control.ControlOperationResult;
import br.edu.ufcg.lsd.commune.container.servicemanager.client.async.AsyncManagerClient;
import br.edu.ufcg.lsd.commune.container.servicemanager.client.sync.SyncContainerUtil;

public final class BrokerAsyncManagerClient extends AsyncManagerClient<BrokerManager> implements BrokerControlClient {

	private static Logger logger = Logger.getLogger(BrokerAsyncManagerClient.class);

	private final BlockingQueue<Object> blockingQueue;

	public BrokerAsyncManagerClient() {
		this(new ArrayBlockingQueue<Object>(1));
	}

	public BrokerAsyncManagerClient(BlockingQueue<Object> blockingQueue) {
		this.blockingQueue = blockingQueue;
	}

	public BlockingQueue<Object> getBlockingQueue() {
		return blockingQueue;
	}

	private BrokerAsyncApplicationClient getBrokerAsyncApplicationClient() {
		return (BrokerAsyncApplicationClient) getServiceManager().getApplication();
	}

	@Override
	public synchronized void operationSucceed(ControlOperationResult controlResult) {
		if (controlResult.getResult() instanceof Integer) {
			Integer jobId = (Integer) controlResult.getResult();
			getBrokerAsyncApplicationClient().notifyWhenJobIsFinished(jobId);
			SyncContainerUtil.putResponseObject(blockingQueue, jobId);

		} else {
			logger.error("Not an integer: " + controlResult.getResult().getClass());
		}
	}

	@Override
	@RecoveryNotification
	public void controlIsUp(BrokerManager control) {
		super.controlIsUp(control);
		getBrokerAsyncApplicationClient().setBrokerUp(true);
	}

	@Override
	@FailureNotification
	public void controlIsDown(BrokerManager control) {
		super.controlIsDown(control);
		getBrokerAsyncApplicationClient().setBrokerUp(false);
	}

	@Override
	public void hereIsConfiguration(Map<String, String> conf) {
		// ignore
	}

	@Override
	public void hereIsUpTime(long upTime) {
		// ignore
	}

}
