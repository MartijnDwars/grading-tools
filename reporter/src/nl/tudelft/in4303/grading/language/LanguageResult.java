package nl.tudelft.in4303.grading.language;

import java.io.PrintStream;
import java.math.RoundingMode;
import java.text.DecimalFormat;

import org.apache.commons.io.output.ByteArrayOutputStream;

import nl.tudelft.in4303.grading.GroupResult;
import nl.tudelft.in4303.grading.TestsListener;

public class LanguageResult extends GroupResult {

	private final DecimalFormat df = new DecimalFormat("#0.##");

	public LanguageResult(String name, TestsListener listener) {
		super(name, listener);
		df.setRoundingMode(RoundingMode.HALF_UP);
	}
	
	@Override
	public String getStatusDescription() {
		
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		PrintStream stream = new PrintStream(output);
		
		switch (status) {
		case SUCCESS:
			
			stream.print("You score " + df.format(points) + " points out of " + total + " points. ");
			stream.print("You pass " + passed + " tests. ");
			break;
	
		case ERROR:
			
			stream.print("Your language definition caused " + errors.size() + " errors.");
			break;
			
		case FAILURE:
			break;
		}
		
		stream.close();
		return output.toString();
	}

	public void report(PrintStream stream, boolean details) {

		if (passed == 0) {
			stream.println("You fail all tests.");
			return;
		}
		
		if (missed == 0) {
			stream.println("You pass all tests.");
			return;
		}
		
		if (details) {
		
			stream.println("You failed the following tests:");
			stream.println();
			
			for (String missed : missedDescr)
				stream.println("* "+missed);
			
			stream.println();
			return;
		}
		
		if (passed == missed) {
			stream.println("You pass as many tests as you fail.");
			return;
		}
		
		double ratio = passed / missed;
		
		if (ratio >= 3.0) {
			stream.println("You pass many tests.");
			return;
		}
		
		if (ratio < 0.33) {
			stream.println("You fail many tests.");
			return;
		}
		
		if (passed > missed)
			stream.println("You pass more tests than you fail.");
		else
			stream.println("You pass less tests than you fail.");
	}

	public void finishedSuite(int passed, int missed, double points, String description) {
			
			this.total  += points;
			this.points += points * passed / (passed + missed);
			this.passed += passed;
			this.missed += missed;
	}
	
}