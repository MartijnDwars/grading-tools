package nl.tudelft.in4303.githubfetcher;

import org.eclipse.egit.github.core.PullRequest;

public class GradeReport {
	public static enum Status {
		SUCCESS,
		FAILURE,
		ERROR
	};
	
	private PullRequest pullRequest;
	private Status status;
	private String report;
	
	public GradeReport(PullRequest pullRequest, Status status, String report) {
		this.pullRequest = pullRequest;
		this.status = status;
		this.report = report;
	}

	public PullRequest getPullRequest() {
		return pullRequest;
	}

	public Status getStatus() {
		return status;
	}

	public String getReport() {
		return this.report;
	}
}
