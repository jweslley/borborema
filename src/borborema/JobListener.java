package borborema;

import java.util.EventListener;
import java.util.concurrent.Future;

// TODO not implemented yet
public interface JobListener extends EventListener {

	void jobSubmitted(/* what params?*/);

	void jobFinished(Future<?> result);

}
