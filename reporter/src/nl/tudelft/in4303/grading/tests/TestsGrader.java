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
    /**
     * Directory of the student's repository
     */
    private File repo;

    /**
     * @param solution    Path to the grading (solution) project
     */
    public TestsGrader(String solution) {
        super(new File(solution, "/languages.xml"));
    }

    protected IResult grade(File repo, boolean checkOnly) {
        this.repo = repo;

        listener.init();

        logger.info(".LANGUAGES " + countLanguages(config));

        logger.info("running reference language implementation");

        GroupResult result = runLanguages(config);

        if (!checkOnly && !result.hasErrors()) {

            logger.info("running erroneous language implementations");
            result.finishedGroup(runTests(config.configurationAt("group")));
        }

        listener.exit();

        return result;
    }

    /**
     * Count all languages starting at the given node
     *
     * @param config
     * @return
     */
    private int countLanguages(HierarchicalConfiguration config) {
        int i = config.configurationsAt("language").size();

        for (Object group : config.configurationsAt("group")) {
            i += countLanguages((HierarchicalConfiguration) group);
        }

        return i;
    }

    private GroupResult runLanguages(HierarchicalConfiguration config) {
        final String name = config.getString("[@name]", "");

        logger.debug("group {}", name);

        TestsResult result = new TestsResult(name, listener);

        try {
            for (Object current : config.configurationsAt("language")) {
                final Configuration langConf = (Configuration) current;
                final String esvPath = langConf.getString("[@esv]");
                final String description = langConf.getString("[@description]");
                final double points = langConf.getDouble("[@points]", 0);

                // Create a new runner for every language to make sure nothing is left behind (see #1)
                TestRunner runner = new TestRunner(repo.getAbsolutePath(), "testrunnerfile");
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

    private GroupResult runTests(HierarchicalConfiguration config) {
        GroupResult result = runLanguages(config);

        try {
            for (Object group : config.configurationsAt("group")) {
                result.finishedGroup(runTests((HierarchicalConfiguration) group));
            }

            result.succeed();
        } catch (Exception e) {
            logger.error("group", e);
        }

        return result;
    }
}
