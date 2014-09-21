package nl.tudelft.in4303.grading;

import java.io.File;

public interface IGrader {

	public abstract IResult check(File dir);
	public abstract IResult grade(File dir);

}