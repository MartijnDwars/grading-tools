package nl.tudelft.in4303.grading.tests;

import java.io.PrintStream;

import nl.tudelft.in4303.grading.GroupResult;
import nl.tudelft.in4303.grading.TestsListener;

import org.apache.commons.io.output.ByteArrayOutputStream;

public class TestsResult extends GroupResult {
	
	public TestsResult(String name, TestsListener listener) {
		super(name, listener);
	}
	
	public void finishedLanguage(boolean detected, String description, double points) {
		this.total += points;
		if (detected) {
			this.points += points;
			this.passed++;
			
			logger.debug("detected: {} ({} points)", description, points);			
		} else {
			missed++;
			missedDescr.add(description);
			
			logger.debug("missed: {} ({} points)", description, points);
		}
	}
	
	@Override
	public String getStatusDescription() {
		
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		PrintStream stream = new PrintStream(output);
		
		switch (status) {
		case SUCCESS:
			
			stream.print("You score " + points + " out of " + total + " points. ");
			stream.print("You have " + listener.getValid() + " valid tests. ");
			stream.print(listener.getEffective() + " of your valid tests detect " + passed + " erroneous language definitions.");
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

	public void report(PrintStream stream, boolean details) {

		if (passed == 0) {
			stream.println("You miss all erroneous language definitions.");
			return;
		}
		
		if (missed == 0) {
			stream.println("You detect all erroneous language definitions.");
			return;
		}
		
		if (details) {
			if (missedDescr.size() > 0) { 
				stream.println("You missed the following erroneous language definitions:");
				stream.println();
				
				for (String missed : missedDescr)
					stream.println("* "+missed);
				
				stream.println();
			}

			return;
		}
		
		if (passed == missed) {
			stream.println("You detect as many erroneous language definitions as you miss.");
			return;
		}
		
		double ratio = passed / missed;
		
		if (ratio >= 3.0) {
			stream.println("You detect many erroneous language definitions.");
			return;
		}
		
		if (ratio < 0.33) {
			stream.println("You miss many erroneous language definitions.");
			return;
		}
		
		if (passed > missed)
			stream.println("You detect more erroneous language definitions than you miss.");
		else
			stream.println("You detect less erroneous language definitions than you miss.");
	}
}
