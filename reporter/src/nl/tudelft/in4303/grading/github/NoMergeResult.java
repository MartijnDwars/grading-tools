package nl.tudelft.in4303.grading.github;

import nl.tudelft.in4303.grading.IResult;

final class NoMergeResult implements IResult {
	@Override
	public Status getStatus() {
		return Status.FAILURE;
	}
	
	@Override
	public String getStatusDescription() {
		return "No submission possible.";
	}

	@Override
	public String getGrade() {
		return "Make sure your branch can be merged into the branch of the assignment.";
	}
	
	@Override
	public String getFeedback() {
		return "Make sure your branch can be merged into the branch of the assignment.";
	}
}