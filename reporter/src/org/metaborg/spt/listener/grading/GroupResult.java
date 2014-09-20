package org.metaborg.spt.listener.grading;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

class GroupResult {
	
	private final String name;
	
	private double total  = 0;
	private double points = 0;
	private int detected  = 0;
	private int missed    = 0;
	
	private final List<GroupResult> results = new ArrayList<>();

	public GroupResult(String name) {
		this.name = name;
	}
	
	public void finishedLanguage(boolean detected, double points) {
		
		this.total += points;
		if (detected) {
			this.points += points;
			this.detected++;
		} else
			this.missed++;
	}
	
	public void finishedGroup(GroupResult result) {

		results.add(result);
		this.total    += result.total;
		this.points   += result.points;
		this.detected += result.detected;
		this.missed   += result.missed;
	}
	
	public void finishedGrading(PrintStream stream, TestGrader grader) {
		
		report(" ", stream);
		
		stream.println("# Summary");
		stream.println();
		
		stream.println("You score currently " + points + " points.");
		stream.println();
		stream.println("You have " + grader.getValid() + " valid tests.");
//		stream.println("You have " + grader.getInvalid() + " invalid tests.");
		stream.println(grader.getEffective() + " of your valid tests detected " + detected + " erroneous grammars.");
//		stream.println("You missed " + missed + " erroneous grammars.");
	}

	public void report(String header, PrintStream stream) {

		stream.println(header + name);
		stream.println();
		
		if (detected == 0) {
			stream.println("You missed all erroneous grammars.");
			return;
		}
		
		if (missed == 0) {
			stream.println("You detected all erroneous grammars.");
			return;
		}
		
		stream.println(groupSuccess());
		stream.println();
		
		for (GroupResult group : results)
			group.report("#" + header, stream);
	}
	
	private String groupSuccess() {

		if (detected == missed)
			return "You detected as many erroneous grammars as you missed.";

		double ratio = detected / missed;
		
		if (ratio >= 3.0) 
			return "You detected many erroneous grammars.";

		if (ratio < 0.33) 
			return "You missed many erroneous grammars.";

		if (detected > missed)
			return "You detected more erroneous grammars than you missed.";
		else
			return "You detected less erroneous grammars than you missed.";
	}
}