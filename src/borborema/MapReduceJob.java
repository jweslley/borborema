package borborema;

import java.io.Serializable;
import java.util.List;

public abstract class MapReduceJob<MapResult extends Serializable, ReduceResult>
extends AbstractJob<MapResult, ReduceResult> {

	public abstract ReduceResult reduce(List<MapResult> intermediateResults);

}
