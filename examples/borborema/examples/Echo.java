package borborema.examples;

import borborema.Task;

public class Echo implements Task<String> {

	private static final long serialVersionUID = -7369647372904461425L;

	private final String message;

	public Echo(String message) {
		this.message = message;
	}

	public String execute() {
		return message;
	}

}
