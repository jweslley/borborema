package embeddedbroker;

import java.util.EventListener;
import java.util.concurrent.Future;

public interface JobListener extends EventListener {

	void jobSubmitted(/* what params?*/);

	void jobFinished(Future<?> result);

}
