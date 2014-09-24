package nl.tudelft.in4303.grading;

import java.io.File;

public interface IFeedbackGrader extends IGrader {

	@Override
	public abstract IFeedbackResult check(File dir);
	
	@Override
	public abstract IFeedbackResult grade(File dir);

}
