package nl.tudelft.in4303.grading;

import java.io.PrintStream;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class GroupResult implements IResult {
	protected static final Logger logger = LoggerFactory.getLogger(GroupResult.class);
	protected final String name;
	protected Status status = null;
	protected double total = 0;
	protected double points = 0;
	protected int passed = 0;
	protected int missed = 0;
	protected final List<String> missedDescr = new ArrayList<>();
	protected final List<GroupResult> results = new ArrayList<>();
	protected final List<String> errors = new ArrayList<>();
	protected final TestsListener listener;

	protected final DecimalFormat df = new DecimalFormat("#0.0#");
	protected final DecimalFormat dfGrade = new DecimalFormat("0.00");
	
	public GroupResult(String name, TestsListener listener) {
		this.name = name;
		this.listener = listener;
		df.setRoundingMode(RoundingMode.HALF_UP);
	}
	
	@Override
	public Status getStatus() {
		return status;
	}

	public void succeed() {
		if (status == null) {
			status = Status.SUCCESS;
		}
	}

	public void error(String e) {
		status = Status.ERROR;
		errors.add(e);
	}

	public boolean hasErrors() {
		return status == Status.ERROR;
	}

	public void finishedGroup(GroupResult result) {
		results.add(result);
		errors.addAll(result.errors);
		
		this.total  += result.total;
		this.points += result.points;
		this.passed += result.passed;
		this.missed += result.missed;
	}

	@Override
	public String getGrade() {
		return getReport(true);
	}

	@Override
	public String getFeedback() {
		return getReport(false);
	}
	
	private String getReport(boolean details) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		PrintStream stream = new PrintStream(output);
		
		switch (status) {
			case SUCCESS:
				report("# ", stream, details);
				if (!details) {
					stream.println();
					stream.println("## Summary");
					stream.println();
					stream.println(getStatusDescription());
				}
				break;
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
		if (details) {
			stream.println(header + name + " (" + df.format(points) + " out of " + df.format(total) + " points)");
		} else {
			stream.println(header + name);
		}

		stream.println();
		
		report(stream, details);
		stream.println();
		
		if (passed == 0 || missed == 0) {
			return;
		}
		
		for (GroupResult group : results) {
			if (details) {
				group.report("#" + header, stream, details);
			} else {
				group.report("#" + header, stream, details);
			}
		}
	}

	abstract protected void report(PrintStream stream, boolean details);
}
