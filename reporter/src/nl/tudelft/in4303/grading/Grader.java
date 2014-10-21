package nl.tudelft.in4303.grading;

import java.io.File;
import java.util.Iterator;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.metaborg.spt.listener.ITestReporter;
import org.metaborg.spt.listener.TestReporterProvider;

public abstract class Grader {

	protected static final Logger logger = LogManager.getLogger();
	protected final XMLConfiguration config;
	protected final File project;
	protected final TestsListener listener;

	public Grader(String config) {
		try {
			XMLConfiguration xmlConfiguration = new XMLConfiguration();
			xmlConfiguration.setDelimiterParsingDisabled(true);
			xmlConfiguration.load(config);

			this.config = xmlConfiguration;
			this.project = this.config.getFile().getParentFile();
		} catch (ConfigurationException e) {
			logger.fatal("configuration", e);
			throw new RuntimeException(e);
		}

		ITestReporter reporter = null;

		Iterator<ITestReporter> reporters = TestReporterProvider.getInstance()
				.getReporters();

		while (!(reporter instanceof TestsListener) && reporters.hasNext())
			reporter = reporters.next();

		listener = (TestsListener) reporter;

	}

	public IResult check(File repo) {
	
		return grade(repo, true);
	}

	public IResult grade(File repo) {
	
		return grade(repo, false);
	}

	protected abstract IResult grade(File repo, boolean checkOnly);

	protected void runTests(TestRunner runner, GroupResult result) {
		if (!runner.runTests()) {
			result.error("");
			logger.error("could not run tests");
		}
	}

}