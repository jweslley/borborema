package embeddedbroker;

import java.io.Serializable;

public interface Task<Result extends Serializable> extends Serializable {

	Result execute();

}
