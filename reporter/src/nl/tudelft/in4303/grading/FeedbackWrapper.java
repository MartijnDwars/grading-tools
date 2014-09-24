package nl.tudelft.in4303.grading;

import java.io.File;

public class FeedbackWrapper implements IGrader {

	private final IFeedbackGrader grader;

	public FeedbackWrapper(IFeedbackGrader grader) {
		this.grader = grader;
	}
	
	@Override
	public IResult check(File dir) {
		return grader.check(dir);
	}

	@Override
	public IResult grade(File dir) {
		final IFeedbackResult result = grader.grade(dir);
		
		return new IResult() {
			
			@Override
			public Status getStatus() {
				return result.getStatus();
			}
			
			@Override
			public String getReport() {
				return result.getFeedback();
			}
		};
	}

}
