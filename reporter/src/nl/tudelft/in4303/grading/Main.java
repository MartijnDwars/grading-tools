package nl.tudelft.in4303.grading;

import nl.tudelft.in4303.grading.github.GitHubGrader;
import nl.tudelft.in4303.grading.tests.TestsGrader;

public class Main {

	public static void main(String[] args) {

		GitHubGrader grader = new GitHubGrader("user", "password");
		grader.registerRunner("assignment1", new TestsGrader());
		grader.run();
		
	}
}
