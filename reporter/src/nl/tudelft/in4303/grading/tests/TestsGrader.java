package nl.tudelft.in4303.grading.tests;

import com.google.inject.Guice;
import com.google.inject.Injector;
import nl.tudelft.in4303.grading.Grader;
import nl.tudelft.in4303.grading.GroupResult;
import nl.tudelft.in4303.grading.IResult;
import nl.tudelft.in4303.grading.TestRunner;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.metaborg.spoofax.core.SpoofaxModule;
import org.metaborg.sunshine.environment.ServiceRegistry;
//import org.metaborg.spoofax.testrunner.core.TestRunner;

import java.io.File;

/**
 * A grader that uses languages to grade a student's tests
 */
public class TestsGrader extends Grader {
    /**
     * The location of the test project, e.g MiniJava-tests-syntax
     */
    private final String testProject;

    /**
     * Directory of the student's repository
     */
    private File repo;

    /**
     * @param solution    Path to the grading (solution) project
     * @param testProject Path to the submission project, i.e. MiniJava-tests-(syntax|names|types)
     */
    public TestsGrader(String solution, String testProject) {
        super(new File(solution, "/languages.xml"));

        this.testProject = testProject;
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

                Injector injector = Guice.createInjector(new NewSpoofaxModule());
                ServiceRegistry.INSTANCE().setInjector(injector);

                TestRunner testRunner = injector.getInstance(TestRunner.class);
                testRunner.registerSPT();
                testRunner.registerLanguage(new File(project, esvPath).getParentFile());

                // Create a new runner for every language to make sure nothing is left behind (see #1)
//                TestRunner runner = new TestRunner(new File(repo, testProject).getAbsolutePath(), /*"testrunnerfile"*/"Run testsuites");
//                runner.registerSPT();
//                runner.registerLanguage(new File(project, esvPath).getParentFile().getAbsolutePath());

                logger.debug("running {}", esvPath);

                testRunner.runTests(new File(repo, testProject));
//                runTests(runner, result);

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
