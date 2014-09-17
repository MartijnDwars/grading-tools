package org.metaborg.spt.listener.grading;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.metaborg.spt.listener.ITestReporter;

/**
 * @author guwac
 *
 */
public class Grader implements ITestReporter {

	private static final Logger logger = LogManager.getLogger(Grader.class);
	
	@Override
	public void reset() throws Exception {
		logger.error("reset");
	}

	@Override
	public void addTestsuite(String name, String filename) throws Exception {
		logger.error("add suite '" + name + "' from " + filename);
	}

	@Override
	public void addTestcase(String testsuiteFile, String description)
			throws Exception {
		logger.error("add case '" + description + "' from suite " + testsuiteFile);
	}

	@Override
	public void startTestcase(String testsuiteFile, String description)
			throws Exception {
		logger.error("start case '" + description + "' from suite " + testsuiteFile);		
	}

	@Override
	public void finishTestcase(String testsuiteFile, String description,
			boolean succeeded, Collection<String> messages) throws Exception {
		logger.error("finished case '" + description + "' from suite " + testsuiteFile + ": " + succeeded + " with messages: " + messages);
		
		try {
			// FIXME make configurable
			File out = new File("/Users/guwac/Cloud/git/in4303/grading/lab2/tests/output2.txt");
			FileWriter writer = new FileWriter(out);
			writer.write("yeah");
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
