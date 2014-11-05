package nl.tudelft.in4303.grading;

import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.Parameter;


public class CLOptions {
	
	@Parameter(description="branch and student to grade")
	private List<String> parameters = new ArrayList<>();
	
	@Parameter(names={"-d", "-details", "-g", "-grade"}, description="generate detailed report")
	private boolean details = false;
	
	@Parameter(names={"-l", "-late"}, description="number of late days")
	private int late = 0;

	public String getBranch() {
		return parameters.get(0);
	}

	public String getStudent() {
		return parameters.get(1);
	}

	public boolean reportDetails() {
		return details;
	}

	public int getLate() {
		return late;
	}
}
