package nl.tudelft.in4303.grading.tests;

import nl.tudelft.in4303.grading.Grader;
import nl.tudelft.in4303.grading.GroupResult;
import nl.tudelft.in4303.grading.IResult;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.metaborg.spoofax.testrunner.core.TestRunner;

import java.io.File;

/**
 * A grader that uses languages to grade a student's tests
 */
public class TestsGrader extends Grader {
    private final String testProject;

    /**
     * @param solution    Path to the grading (solution) project
     * @param testProject Path to the submission project (i.e. MiniJava-tests(-names|-types)?)
     */
    public TestsGrader(String solution, String testProject) {
        super(new File(solution, "/languages.xml"));

        this.testProject = testProject;
    }

    protected IResult grade(File repo, boolean checkOnly) {

        listener.init();
        TestRunner runner = new TestRunner(new File(repo, testProject).getAbsolutePath(), "testrunnerfile");

        logger.info("running reference language implementation");

        GroupResult result = runLanguages(runner, config);

        if (!checkOnly && !result.hasErrors()) {

            logger.info("running erroneous language implementations");
            result.finishedGroup(runTests(runner, config.configurationAt("group")));
        }

        listener.exit();
        return result;
    }

    private GroupResult runLanguages(TestRunner runner, HierarchicalConfiguration config) {

        final String name = config.getString("[@name]", "");

        logger.debug("group {}", name);

        TestsResult result = new TestsResult(name, listener);

        try {

            for (Object current : config.configurationsAt("language")) {

                final Configuration langConf = (Configuration) current;
                final String esvPath = langConf.getString("[@esv]");
                final String description = langConf.getString("[@description]");
                final double points = langConf.getDouble("[@points]", 0);

                runner.registerSPT();
                runner.registerLanguage(new File(project, esvPath).getParentFile().getAbsolutePath());
                logger.debug("running {}", esvPath);

                runTests(runner, result);

                result.finishedLanguage(listener.newLanguage(), description, points);
            }

            result.succeed();

        } catch (Exception e) {
            logger.error("language", e);
        }

        return result;
    }

    private GroupResult runTests(TestRunner runner, HierarchicalConfiguration config) {

        GroupResult result = runLanguages(runner, config);

        try {

            for (Object group : config.configurationsAt("group"))
                result.finishedGroup(runTests(runner, (HierarchicalConfiguration) group));

            result.succeed();

        } catch (Exception e) {
            logger.error("group", e);
        }
        return result;
    }
}
