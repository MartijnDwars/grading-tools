package org.metaborg.spt.listener.grading;

import java.util.Collection;
import java.util.Hashtable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.metaborg.spt.listener.ITestReporter;

/**
 * @author guwac
 *
 */
public class Grader implements ITestReporter {
	
	private static final Logger logger = LogManager.getLogger(Grader.class);

	private Hashtable<String, Boolean> valid     = new Hashtable<String, Boolean>(300);
	private Hashtable<String, Boolean> effective = new Hashtable<String, Boolean>(300);

	private boolean detected = false;
	
	@Override
	public void reset() throws Exception {
	}

	@Override
	public void addTestsuite(String name, String filename) throws Exception {
		logger.error(filename);
	}

	@Override
	public void addTestcase(String testsuiteFile, String description)
			throws Exception {
		if (detected) throw new RuntimeException();
	}

	@Override
	public void startTestcase(String testsuiteFile, String description)
			throws Exception {
	}

	@Override
	public void finishTestcase(String testsuiteFile, String description,
			boolean succeeded, Collection<String> messages) throws Exception {
		
		final String key = testsuiteFile+description;
		if (valid.get(key) == null)
			valid.put(key, succeeded);
		else if (valid.get(key) && !succeeded) {
			effective.put(key, true);
			detected = true;
		}
	}
	
	public int getValid() {
		int i = 0;
		for (Boolean b: valid.values()) {
			if (b) i++;
		}
		return i;
	}
	
	public int getInvalid() {
		return valid.values().size() - getValid();
	}
	
	public int getEffective() {
		int i = 0;
		for (Boolean b: effective.values()) {
			if (b) i++;
		}
		return i;
	}

	public void setLanguage(String variant) {
		detected  = false;
	}
}
