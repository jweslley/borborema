package embeddedbroker.util;

public class Preconditions {

	public static void check(boolean value) {
		check("", value);
	}

	public static void check(String message, boolean value) {
		if (!value) {
			throw new IllegalArgumentException(message);
		}
	}

	public static void checkNotNull(Object object) {
		checkNotNull("", object);
	}

	public static void checkNotNull(String message, Object object) {
		if (object == null) {
			throw new IllegalArgumentException(message);
		}
	}

}
