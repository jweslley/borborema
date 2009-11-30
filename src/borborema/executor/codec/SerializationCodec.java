package borborema.executor.codec;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface SerializationCodec {

	Object readObject(InputStream is) throws IOException;

	void writeObject(Object source, OutputStream os) throws IOException;

}
