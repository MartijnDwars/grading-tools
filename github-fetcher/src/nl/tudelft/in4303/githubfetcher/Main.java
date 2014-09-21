package nl.tudelft.in4303.githubfetcher;

import java.io.File;

public class Main {
	public static void main(String[] args) {
		GitHubFetcher fetcher = new GitHubFetcher("username", "password");
		fetcher.registerGrader("assignment1", new IReporter() {
			@Override
			public GradeReport getReport() {
				return new GradeReport(null, GradeReport.Status.SUCCESS, "Example report.");
			}

			@Override
			public GradeReport getReport(File tmpDir) {
				return new GradeReport(null, GradeReport.Status.SUCCESS, "Example report.");
			}
		});
		fetcher.run();
	}
}
