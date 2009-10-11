package embeddedbroker.broker;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.ourgrid.broker.BrokerComponentContextFactory;
import org.ourgrid.broker.BrokerConfiguration;
import org.ourgrid.broker.BrokerConstants;
import org.ourgrid.common.interfaces.to.GridProcessState;
import org.ourgrid.common.interfaces.to.JobEndedInterested;
import org.ourgrid.common.spec.job.JobSpec;
import org.ourgrid.common.spec.main.CompilerException;
import org.ourgrid.common.spec.main.DescriptionFileCompile;

import br.edu.ufcg.lsd.commune.context.ModuleContext;
import br.edu.ufcg.lsd.commune.context.PropertiesFileParser;
import br.edu.ufcg.lsd.commune.network.xmpp.CommuneNetworkException;
import br.edu.ufcg.lsd.commune.processor.ProcessorStartException;

public class Main implements JobEndedInterested {

	public static void main(String[] args) throws CommuneNetworkException, ProcessorStartException, CompilerException,
	InterruptedException {

		BrokerAsyncApplicationClient brokerASyncClient = new BrokerAsyncApplicationClient(getContext());

		while (!brokerASyncClient.isBrokerUp()) {
			Thread.sleep(200);
		}

		brokerASyncClient.getContainer().deploy(BrokerConstants.JOB_ENDED_INTERESTED, new Main());

		String jdfPath = "/usr/local/broker/examples/addJob/simplejob-put.jdf";

		JobSpec theJob = DescriptionFileCompile.compileJDF(jdfPath);

		int nextJobID = brokerASyncClient.addJob(theJob);

		brokerASyncClient.notifyWhenJobIsFinished(nextJobID);

	}

	private static ModuleContext getContext() {
		PropertiesFileParser propertiesFileParser = new PropertiesFileParser(BrokerConfiguration.PROPERTIES_FILENAME);
		return new BrokerComponentContextFactory(propertiesFileParser).createContext();
	}


	public void jobEnded(int jobid, GridProcessState state) {
		System.out.println("O job com id " + jobid + " terminou com o estado " + state.name());
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("/tmp/async.out")));
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				System.out.println(line);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public void schedulerHasBeenShutdown() {
		System.out.println("Main.schedulerHasBeenShutdown()");
	}

}
