package embeddedbroker.samples;

import embeddedbroker.Task;

public class EchoTask implements Task<String> {

	private static final long serialVersionUID = -7369647372904461425L;

	private final String message;

	public EchoTask(String message) {
		this.message = message;
	}

	public final String execute() {
		return message;
	}

}
