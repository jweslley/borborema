package embeddedbroker.samples;

import java.io.File;

import embeddedbroker.Input;
import embeddedbroker.Task;

public class Grep implements Task<String> {

	private static final long serialVersionUID = -8848459785125354894L;

	@Input
	private final File input;

	public Grep(File input) {
		this.input = input;
	}

	@Override
	public String execute() {

		// TODO Auto-generated method stub
		return null;
	}

}
