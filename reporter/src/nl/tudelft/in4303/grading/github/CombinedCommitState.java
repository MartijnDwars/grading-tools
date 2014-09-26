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
	
	public String getState(String context) {
		for (ExtendedCommitStatus status : statuses)
			if (context.equals(status.getContext()))
				return status.getState();
		return null;
	}
	
	public boolean hasState(String context) {
		return getState() != null;
	}
	
	public boolean hasState(String context, String state) {
		return state == getState(context);
	}
}
