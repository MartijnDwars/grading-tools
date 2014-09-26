package nl.tudelft.in4303.grading.tests;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import nl.tudelft.in4303.grading.IResult;

import org.apache.commons.io.output.ByteArrayOutputStream;

public class TestsResult implements IResult {
	
	private final String name;
	
	private Status status = null;
	private double total  = 0;
	private double points = 0;
	private int detected  = 0;
	private int missed    = 0;
	
	private final List<String> missedDescr = new ArrayList<>();
	
	private final List<TestsResult> results = new ArrayList<>();
	private final List<String> errors = new ArrayList<>();
	private final TestsListener listener;

	public TestsResult(String name, TestsListener listener) {
		this.name = name;
		this.listener = listener;
	}
	
	public void finishedLanguage(boolean detected, String description, double points) {
		
		this.total += points;
		if (detected) {
			this.points += points;
			this.detected++;
		} else {
			missed++;
			missedDescr.add(description);
		}
	}
	
	public void finishedGroup(TestsResult result) {

		results.add(result);
		errors.addAll(result.errors);
		
		this.total    += result.total;
		this.points   += result.points;
		this.detected += result.detected;
		this.missed   += result.missed;
	}
	
	@Override
	public String getStatusDescription() {
		
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		PrintStream stream = new PrintStream(output);
		
		switch (status) {
		case SUCCESS:
			
			stream.print("You score " + points + " points. ");
			stream.print("You have " + listener.getValid() + " valid tests. ");
			stream.print(listener.getEffective() + " of your valid tests detect " + detected + " erroneous language definitions.");
			break;

		case ERROR:
			
			stream.print("Your tests caused " + errors.size() + " errors.");
			break;
			
		case FAILURE:
			break;
		}
		
		stream.close();
		return output.toString();
	}
	
	@Override
	public String getGrade() {
		return getReport(true);
	}
	
	@Override
	public String getFeedback() {
		return getReport(false);
	}
		public String getReport(boolean details) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		PrintStream stream = new PrintStream(output);
		
		switch (status) {
		case SUCCESS:
			
			report("# ", stream, details);
			
		case ERROR:

			for (String error : errors) {
				stream.println("```");
				stream.println(error);
				stream.println("```");
				stream.println();
			}
			break;
			
		case FAILURE:
			break;
		}
		
		stream.close();
		return output.toString();
	}

	private void report(String header, PrintStream stream, boolean details) {

		stream.println(header + name);
		stream.println();
		
		if (detected == 0) {
			stream.println("You miss all erroneous language definitions.");
			return;
		}
		
		if (missed == 0) {
			stream.println("You detect all erroneous language definitions.");
			return;
		}
		
		groupSuccess(stream, details);
		stream.println();
		
		for (TestsResult group : results)
			if (details)
				group.report("#" + header + " (" + points + " out of " + total + " points)", stream, details);
			else
				group.report("# " + header, stream, details);
	}	
	
	private void groupSuccess(PrintStream stream, boolean details) {

		if (details) {
		
			stream.println("You missed the following erroneous language definitions:");
			stream.println();
			
			for (String missed : missedDescr)
				stream.println("* "+missed);
			
			stream.println();
			return;
		}
		
		if (detected == missed) {
			stream.println("You detect as many erroneous language definitions as you miss.");
			return;
		}
		
		double ratio = detected / missed;
		
		if (ratio >= 3.0) {
			stream.println("You detect many erroneous language definitions.");
			return;
		}
		
		if (ratio < 0.33) {
			stream.println("You miss many erroneous language definitions.");
			return;
		}
		
		if (detected > missed)
			stream.println("You detect more erroneous language definitions than you miss.");
		else
			stream.println("You detect less erroneous language definitions than you miss.");
	}

	@Override
	public Status getStatus() {
		return status;
	}

	public void succeed() {
		if (status == null)
			status = Status.SUCCESS;
	}
	
	public void error(String e) {
		status = Status.ERROR;
		errors.add(e);
	}

	public boolean hasErrors() {
		return status == Status.ERROR;
	}
}