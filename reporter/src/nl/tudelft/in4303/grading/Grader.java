package nl.tudelft.in4303.grading;

import java.io.File;
import java.util.Iterator;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.metaborg.spt.listener.ITestReporter;
import org.metaborg.spt.listener.TestReporterProvider;

public abstract class Grader implements IGrader {

	protected final XMLConfiguration config;
	protected final File project;
	protected final TestsListener listener;

	public Grader(String config) {
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
	public IResult check(File repo) {
	
		return grade(repo, true);
	}

	@Override
	public IResult grade(File repo) {
	
		return grade(repo, false);
	}

	protected abstract IResult grade(File repo, boolean b);

}