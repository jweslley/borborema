package embeddedbroker.util;

import embeddedbroker.JvmOptions;
import embeddedbroker.Requirements;

public class Annotations {

	public static String getRequirements(Object source) {
		Requirements r = source.getClass().getAnnotation(Requirements.class);
		return (r != null) ? r.value() : "";
	}

	public static String getJvmOptions(Object source, String defaultValue) {
		JvmOptions opts = source.getClass().getAnnotation(JvmOptions.class);
		return (opts != null) ? opts.value() : defaultValue;
	}

}
