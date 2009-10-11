package embeddedbroker;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.Future;

public class MapReduceJob<MapResult extends Serializable, ReduceResult> extends AbstractJob<MapResult, ReduceResult> implements JobListener {

	public void jobSubmitted() {
		// TODO Auto-generated method stub

	}

	public void jobFinished(Future<?> result) {
		// TODO Auto-generated method stub

	}

	public ReduceResult reduce(List<MapResult> parcialResults) {
		return null;
	}

}
