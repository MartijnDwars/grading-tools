package nl.tudelft.in4303.grading;

import nl.tudelft.in4303.grading.github.GitHubGrader;
import nl.tudelft.in4303.grading.tests.TestsGrader;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

public class Main {

	public static void main(String[] args) {

		try {
			PropertiesConfiguration user = new PropertiesConfiguration(
					"gh.properties");
			GitHubGrader grader = new GitHubGrader(user.getString("user"),
					user.getString("user2"));

			grader.grade(new TestsGrader("MiniJava-tests"), "assignment1", "^student-"+ args[0] +"(.*)$");

		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
	}
}
