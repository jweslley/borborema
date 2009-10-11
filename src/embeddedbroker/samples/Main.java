package embeddedbroker.samples;

import java.io.FileInputStream;
import java.io.IOException;

import org.ourgrid.broker.BrokerComponentContextFactory;
import org.ourgrid.broker.BrokerConfiguration;
import org.ourgrid.broker.ui.sync.BrokerSyncApplicationClient;
import org.ourgrid.broker.ui.sync.BrokerUIMessages;
import org.ourgrid.common.spec.job.JobSpec;
import org.ourgrid.common.spec.main.DescriptionFileCompile;

import br.edu.ufcg.lsd.commune.context.ModuleContext;
import br.edu.ufcg.lsd.commune.context.PropertiesFileParser;
import embeddedbroker.executor.codec.JavaSerializationCodec;

public class Main {

	public static void smain(String[] args) throws Exception {
		PropertiesFileParser properties = new PropertiesFileParser(BrokerConfiguration.PROPERTIES_FILENAME);
		ModuleContext context = new BrokerComponentContextFactory(properties).createContext();
		BrokerSyncApplicationClient client = new BrokerSyncApplicationClient(context);

		JobSpec theJob = DescriptionFileCompile.compileJDF( "/usr/local/broker/examples/addJob/echo.jdf" );
		int id = Integer.parseInt(client.addJob( theJob ).getResult().toString());
		System.out.println( BrokerUIMessages.getJobAddedMessage( id ) );

		client.stop();
	}

	public static void main(final String[] args) throws IOException {
		System.out.println(new JavaSerializationCodec().readObject(new FileInputStream("/tmp/824c7ac7-dbc9-46f1-a692-a6ab8c900c0b8705849000276913611.task")));
	}

}
