package nl.tudelft.in4303.grading.github;

import nl.tudelft.in4303.grading.IResult;

import org.eclipse.egit.github.core.CommitStatus;

public class ExtendedCommitStatus extends CommitStatus {
	/**
	 * 
	 */
	private static final long serialVersionUID = -581935326816069686L;
	
	private String context;
	
	public ExtendedCommitStatus() {
		super();
	}
	
	public ExtendedCommitStatus(String context, String state, String description) {
		super();
		setContext(context);
		setState(state);
		setDescription(description);
	}
	
	public ExtendedCommitStatus(IResult report) {
	
		super();
		switch (report.getStatus()) {
		case SUCCESS:
			setState("success");
			break;
		case FAILURE:
			setState("failure");
			break;
		case ERROR:
		default:
			setState("error");
			break;
		}
		setDescription(report.getStatusDescription());
	}
	
	public String getContext() {
		return context;
	}

	public CommitStatus setContext(final String context) {
		this.context = context;
		return this;
	}
}
