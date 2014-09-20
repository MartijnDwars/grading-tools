package nl.tudelft.in4303.grading.tests;

import java.io.File;
import java.util.Iterator;

import nl.tudelft.in4303.grading.IResult.Status;
import nl.tudelft.in4303.grading.IRunner;
import nl.tudelft.in4303.grading.TestRunner;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.metaborg.spt.listener.ITestReporter;
import org.metaborg.spt.listener.TestReporterProvider;

public class TestsRunner implements IRunner {

	private final XMLConfiguration config;
	private final TestRunner runner;
	private final File project;
	private final TestsListener listener;

	public TestsRunner(File repo) {
		this("languages.xml", new File(repo, "MiniJava-tests"));
	}

	public TestsRunner(String config, File tests) throws RuntimeException {
		try {
			this.config = new XMLConfiguration(config);
			this.project = this.config.getFile().getParentFile();
			this.runner = new TestRunner(tests);
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

	/* (non-Javadoc)
	 * @see nl.tudelft.in4303.grading.tests.IRunner#run()
	 */
	@Override
	public TestsResult run() {

		listener.init();
		return runTests(config);
	}

	private TestsResult runTests(HierarchicalConfiguration config) {

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

			for (Object group : config.configurationsAt("group"))
				result.finishedGroup(runTests((HierarchicalConfiguration) group));

			result.setStatus(Status.SUCCESS);

		} catch (Exception e) {
			System.err.println(e);
			result.setStatus(Status.ERROR);
		}
		return result;
	}
}
