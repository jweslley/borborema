package borborema.examples;

import borborema.Task;

public class SumTask implements Task<Integer> {

	private static final long serialVersionUID = 4935621637714013048L;

	private final int a;
	private final int b;

	public SumTask(int a, int b) {
		this.a = a;
		this.b = b;
	}

	public Integer execute() {
		return a + b;
	}

}
