package embeddedbroker.broker;

import org.ourgrid.broker.BrokerConstants;
import org.ourgrid.common.interfaces.management.BrokerManager;

import br.edu.ufcg.lsd.commune.container.servicemanager.client.InitializationContext;

public final class BrokerAsyncInitializationContext implements
InitializationContext<BrokerManager, BrokerAsyncManagerClient> {

	public BrokerAsyncManagerClient createManagerClient() {
		return new BrokerAsyncManagerClient();
	}

	public Class<BrokerManager> getManagerObjectType() {
		return BrokerManager.class;
	}

	public String getServerContainerName() {
		return BrokerConstants.MODULE_NAME;
	}

}
