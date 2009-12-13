package borborema.examples;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import borborema.Input;
import borborema.Task;

public class Grep implements Task<String> {

	private static final long serialVersionUID = -8848459785125354894L;

	@Input(required=true)
	private final File input;

	private final String pattern;

	public Grep(File input, String pattern) {
		this.input = input;
		this.pattern = pattern;
	}

	@Override
	public String execute() {
		try {
			StringBuilder result = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(input)));
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				if (line.matches(pattern)) {
					result.append(line).append("\n");
				}
			}
			return result.toString();

		} catch (IOException e) {
			return "ERROR: " + e.getMessage();
		}
	}

}
