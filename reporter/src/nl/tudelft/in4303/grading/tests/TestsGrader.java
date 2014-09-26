package nl.tudelft.in4303.grading.tests;

import java.io.File;
import java.util.Iterator;

import nl.tudelft.in4303.grading.IGrader;
import nl.tudelft.in4303.grading.TestRunner;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.metaborg.spt.listener.ITestReporter;
import org.metaborg.spt.listener.TestReporterProvider;

public class TestsGrader implements IGrader {

	private final XMLConfiguration config;
	private final File project;
	private final TestsListener listener;

	public TestsGrader() {
		this("languages.xml");
	}

	public TestsGrader(String config) throws RuntimeException {
		try {
			this.config = new XMLConfiguration(config);
			this.project = this.config.getFile().getParentFile();
		} catch (ConfigurationException e) {
			throw new RuntimeException(e);
		}

		ITestReporter reporter = null;

		Iterator<ITestReporter> reporters = TestReporterProvider.getInstance()
				.getReporters();

		while (!(reporter instanceof TestsListener) && reporters.hasNext())
			reporter = reporters.next();

		listener = (TestsListener) reporter;
	}

	@Override
	public TestsResult check(File repo) {

		return grade(repo, true);
	}

	@Override
	public TestsResult grade(File repo) {

		return grade(repo, false);
	}

	private TestsResult grade(File repo, boolean checkOnly) {

		listener.init();
		TestRunner runner = new TestRunner(new File(repo, "MiniJava-tests"));
		TestsResult result = runLanguages(runner, config);
		if (checkOnly || result.hasErrors())
			return result;
		else
			result.finishedGroup(runTests(runner, config.configurationAt("group")));
			return result;
	}

	private TestsResult runLanguages(TestRunner runner, HierarchicalConfiguration config) {
		
		TestsResult result = new TestsResult(config.getString("[@name]", ""),
				listener);

		try {

			for (Object current : config.configurationsAt("language")) {

				HierarchicalConfiguration langConf = (HierarchicalConfiguration) current;

				final String esvPath = langConf.getString("[@esv]");
				TestRunner.registerLanguage(new File(project, esvPath));

				if (!runner.runTests())
					result.error("");

				result.finishedLanguage(listener.newLanguage(),
						langConf.getString("[@description]"), langConf.getDouble("[@points]", 0));
			}
			
			result.succeed();
			
		} catch (Exception e) {
			System.out.println("caught");
			e.printStackTrace(System.err);
			result.error(e.toString());
		}
		
		return result;
	}
	private TestsResult runTests(TestRunner runner, HierarchicalConfiguration config) {

		TestsResult result = runLanguages(runner, config);

		try {

			for (Object group : config.configurationsAt("group"))
				result.finishedGroup(runTests(runner, (HierarchicalConfiguration) group));

			result.succeed();

		} catch (Exception e) {
			result.error(e.toString());
		}
		return result;
	}
}
