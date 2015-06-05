package nl.tudelft.in4303.grading;

import com.beust.jcommander.JCommander;
import nl.tudelft.in4303.grading.github.GitHubGrader;
import nl.tudelft.in4303.grading.language.LanguageGrader;
import nl.tudelft.in4303.grading.local.LocalGrader;
import nl.tudelft.in4303.grading.tests.TestsGrader;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConfigurationUtils;

import java.io.File;

public class Main {

    public static void main(String[] args) throws Exception {

        CLOptions options = new CLOptions();
        new JCommander(options, args);

        try {
            Grader reporter;

            if (ConfigurationUtils.locate(options.getSolution(), "tests.xml") != null) {
                reporter = new LanguageGrader(options.getSolution());
            } else {
                reporter = new TestsGrader(options.getSolution(), options.getTestProject());
            }

            if (options.getProject() != null) {
                LocalGrader grader = new LocalGrader();

                if (options.reportDetails()) {
                    grader.grade(reporter, new File(options.getProject()));
                } else {
                    grader.feedback(reporter, new File(options.getProject()));
                }
            } else {
                GitHubGrader grader = GitHubGrader.fromConfig("gh.properties", options.runDry());
                String pattern = "^student-" + options.getStudent() + "(.*)$";
                if (options.reportDetails()) {
                    grader.grade(reporter, options.getBranch(), pattern);
                } else {
                    grader.feedback(reporter, options.getBranch(), pattern, options.getLate());
                }
            }

        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
    }
}
