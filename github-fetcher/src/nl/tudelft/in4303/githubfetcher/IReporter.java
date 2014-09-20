package nl.tudelft.in4303.githubfetcher;

import java.io.File;

public interface IReporter {

	public GradeReport getReport();

	public GradeReport getReport(File tmpDir);

}
