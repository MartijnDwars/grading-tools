package nl.tudelft.in4303.grading.github;

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
	
	public String getContext() {
		return context;
	}

	public CommitStatus setContext(final String context) {
		this.context = context;
		return this;
	}
}
