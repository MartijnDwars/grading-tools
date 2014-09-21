package nl.tudelft.in4303.grading.tests;

import java.io.File;
import java.util.Iterator;

import nl.tudelft.in4303.grading.IResult.Status;
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
	public TestsResult grade(File repo) {

		listener.init();
		return runTests(new TestRunner(new File(repo, "MiniJava-tests")), config);
	}

	public TestsResult check(File repo) {

		listener.init();
		return runLanguages(new TestRunner(new File(repo, "MiniJava-tests")), config);
	}

	private TestsResult runLanguages(TestRunner runner, HierarchicalConfiguration config) {
		
		TestsResult result = new TestsResult(config.getString("[@name]", ""),
				listener);

		try {

			for (Object current : config.configurationsAt("language")) {

				HierarchicalConfiguration langConf = (HierarchicalConfiguration) current;

				final String esvPath = langConf.getString("[@esv]");
				TestRunner.registerLanguage(new File(project, esvPath));

				runner.runTests();

				result.finishedLanguage(listener.newLanguage(),
						langConf.getDouble("[@points]", 1));
			}
			
			result.setStatus(Status.SUCCESS);

		} catch (Exception e) {
			System.err.println(e);
			result.setStatus(Status.ERROR);
		}
		
		return result;
	}
	private TestsResult runTests(TestRunner runner, HierarchicalConfiguration config) {

		TestsResult result = runLanguages(runner, config);

		try {

			for (Object group : config.configurationsAt("group"))
				result.finishedGroup(runTests(runner, (HierarchicalConfiguration) group));

			result.setStatus(Status.SUCCESS);

		} catch (Exception e) {
			System.err.println(e);
			result.setStatus(Status.ERROR);
		}
		return result;
	}
}
