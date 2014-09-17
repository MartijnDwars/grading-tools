package org.metaborg.spt.listener.grading;

import java.util.Collection;

import org.metaborg.spt.listener.ITestReporter;

/**
 * @author guwac
 *
 */
public class Grader implements ITestReporter {
	
	@Override
	public void reset() throws Exception {
	}

	@Override
	public void addTestsuite(String name, String filename) throws Exception {
	}

	@Override
	public void addTestcase(String testsuiteFile, String description)
			throws Exception {
	}

	@Override
	public void startTestcase(String testsuiteFile, String description)
			throws Exception {
	}

	@Override
	public void finishTestcase(String testsuiteFile, String description,
			boolean succeeded, Collection<String> messages) throws Exception {
	}

}
