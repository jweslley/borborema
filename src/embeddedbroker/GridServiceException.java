package embeddedbroker;

public class GridServiceException extends RuntimeException {

	private static final long serialVersionUID = 510833722067541020L;

	public GridServiceException(String message) {
		super(message);
	}

	public GridServiceException(Throwable cause) {
		super(cause);
	}

	public GridServiceException(String message, Throwable cause) {
		super(message, cause);
	}

}
