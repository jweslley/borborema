package borborema.examples;

import java.io.File;
import java.util.List;
import java.util.concurrent.Future;

import borborema.BagOfTasks;
import borborema.GridService;
import borborema.OurGridService;

public class FileProcessing {

	public static void main(final String[] args) throws Exception {
		GridService service = new OurGridService();

		BagOfTasks<String> bot = new BagOfTasks<String>();
		bot
		.addJarFile(new File("borborema.jar"))
		.addTask(new Grep(new File("examples/borborema/examples/Grep.java"), ".*private.*"));

		Future<List<String>> botResult = service.submit(bot);

		List<String> list = botResult.get();
		System.out.println("result: " + list);

		service.shutdown();
	}

}
