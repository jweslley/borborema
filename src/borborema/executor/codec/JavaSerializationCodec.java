package borborema.executor.codec;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

public class JavaSerializationCodec implements SerializationCodec {

	public final Object readObject(InputStream is) throws IOException {
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(is);
			return ois.readObject();

		} catch (ClassNotFoundException e) {
			throw new IOException(e);

		} finally {
			if (ois != null) {
				ois.close();
			}
		}
	}

	public final void writeObject(Object source, OutputStream os) throws IOException {
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(os);
			oos.writeObject(source);

		} finally {
			if (oos != null) {
				oos.close();
			}
		}
	}

}
