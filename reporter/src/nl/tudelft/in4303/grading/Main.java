package nl.tudelft.in4303.grading;

import nl.tudelft.in4303.grading.github.GitHubGrader;
import nl.tudelft.in4303.grading.language.LanguageGrader;
import nl.tudelft.in4303.grading.tests.TestsGrader;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConfigurationUtils;

import com.beust.jcommander.JCommander;

public class Main {

	public static void main(String[] args) {

		CLOptions options = new CLOptions();
		new JCommander(options, args);
		
		try {
			GitHubGrader grader = new GitHubGrader(options.runDry());
			
			Grader reporter;

			if (ConfigurationUtils.locate("tests.xml") != null)
				reporter = new LanguageGrader();
			else
				reporter = new TestsGrader();
		
			String pattern = "^student-"+ options.getStudent() +"(.*)$";
			if (options.reportDetails())
				grader.grade(reporter, options.getBranch(), pattern);
			else
				grader.feedback(reporter, options.getBranch(), pattern, options.getLate());
			
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
	}
}
