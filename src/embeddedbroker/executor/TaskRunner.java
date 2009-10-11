package embeddedbroker.executor;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;

import embeddedbroker.Task;
import embeddedbroker.executor.codec.JavaSerializationCodec;
import embeddedbroker.executor.codec.SerializationCodec;

public class TaskRunner {

	/**
	 * @param args
	 * 			args[0] - source filename. Serialized object input.
	 * 			args[1] - target filename. Serialized object output.
	 * 
	 * TODO add another param args[2], which will be the codec class name.
	 * TODO returns either the task result or the thrown exception. (use xml?!)
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public static void main(String... args) throws IOException {
		SerializationCodec codec = new JavaSerializationCodec();
		Object rawTask = codec.readObject(new FileInputStream(args[0]));

		Serializable result = ((Task<Serializable>) rawTask).execute();

		codec.writeObject(result, new FileOutputStream(args[1]));
	}

}
