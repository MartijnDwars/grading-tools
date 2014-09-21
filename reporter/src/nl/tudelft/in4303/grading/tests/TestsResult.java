package nl.tudelft.in4303.grading.tests;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import nl.tudelft.in4303.grading.IResult;

import org.apache.commons.io.output.ByteArrayOutputStream;

public class TestsResult implements IResult {
	
	private final String name;
	
	private Status status = Status.FAILURE;
	private double total  = 0;
	private double points = 0;
	private int detected  = 0;
	private int missed    = 0;
	
	private final List<TestsResult> results = new ArrayList<>();
	
	private final TestsListener listener;

	public TestsResult(String name, TestsListener listener) {
		this.name = name;
		this.listener = listener;
	}
	
	public void finishedLanguage(boolean detected, double points) {
		
		this.total += points;
		if (detected) {
			this.points += points;
			this.detected++;
		} else
			this.missed++;
	}
	
	public void finishedGroup(TestsResult result) {

		results.add(result);
		this.total    += result.total;
		this.points   += result.points;
		this.detected += result.detected;
		this.missed   += result.missed;
	}
	
	public String getReport() {
		
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		PrintStream stream = new PrintStream(output);
		report(" ", stream);
		
		stream.println("# Summary");
		stream.println();
		
		stream.println("You score currently " + points + " points.");
		stream.println();
		stream.println("You have " + listener.getValid() + " valid tests.");
//		stream.println("You have " + grader.getInvalid() + " invalid tests.");
		stream.println(listener.getEffective() + " of your valid tests detected " + detected + " erroneous language definitions.");
//		stream.println("You missed " + missed + " erroneous language definitions.");
	
		return output.toString();
	}

	private void report(String header, PrintStream stream) {

		stream.println(header + name);
		stream.println();
		
		if (detected == 0) {
			stream.println("You missed all erroneous language definitions.");
			return;
		}
		
		if (missed == 0) {
			stream.println("You detected all erroneous language definitions.");
			return;
		}
		
		stream.println(groupSuccess());
		stream.println();
		
		for (TestsResult group : results)
			group.report("#" + header, stream);
	}
	
	private String groupSuccess() {

		if (detected == missed)
			return "You detected as many erroneous language definitions as you missed.";

		double ratio = detected / missed;
		
		if (ratio >= 3.0) 
			return "You detected many erroneous language definitions.";

		if (ratio < 0.33) 
			return "You missed many erroneous language definitions.";

		if (detected > missed)
			return "You detected more erroneous language definitions than you missed.";
		else
			return "You detected less erroneous language definitions than you missed.";
	}

	@Override
	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}
}