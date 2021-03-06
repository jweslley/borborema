package borborema.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import borborema.Input;
import borborema.JvmOptions;
import borborema.Requirements;
import borborema.Task;

public class Annotations {

	public static String getRequirements(Object source) {
		Requirements r = source.getClass().getAnnotation(Requirements.class);
		return (r != null) ? r.value() : "";
	}

	public static String getJvmOptions(Object source, String defaultValue) {
		JvmOptions opts = source.getClass().getAnnotation(JvmOptions.class);
		return (opts != null) ? opts.value() : defaultValue;
	}

	public static Map<Field, File> getFilesFrom(Task<?> task)
		throws IllegalAccessException, FileNotFoundException {

		Map<Field, File> files = new HashMap<Field, File>();

		Field[] fields = task.getClass().getDeclaredFields();
		for (Field field : fields) {
			Input input = field.getAnnotation(Input.class);
			if ((input == null) || !field.getType().equals(File.class)) {
				continue;
			}

			field.setAccessible(true);
			File file = (File) field.get(task);
			if ((file != null) && file.exists()) {
				files.put(field, file);

			} else if (input.required()) {
				throw new FileNotFoundException();
			}
		}

		return files;
	}

}
