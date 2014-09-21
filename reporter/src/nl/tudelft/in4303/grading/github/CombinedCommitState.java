package nl.tudelft.in4303.grading.github;

import java.util.List;

public class CombinedCommitState {
	private String state;
	
	private List<ExtendedCommitStatus> statuses;

	public String getState() {
		return state;
	}

	public CombinedCommitState setState(String state) {
		this.state = state;
		return this;
	}

	public List<ExtendedCommitStatus> getStatuses() {
		return statuses;
	}

	public CombinedCommitState setStatuses(List<ExtendedCommitStatus> statuses) {
		this.statuses = statuses;
		return this;
	}
}
