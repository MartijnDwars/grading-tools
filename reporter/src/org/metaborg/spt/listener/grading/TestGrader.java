package org.metaborg.spt.listener.grading;

import java.util.Collection;
import java.util.HashSet;

import org.metaborg.spt.listener.ITestReporter;

/**
 * @author guwac
 *
 */
public class TestGrader implements ITestReporter {
	
	private final HashSet<String> valid       = new HashSet<String>(500);
	private final HashSet<String> invalid     = new HashSet<String>(500);
	private final HashSet<String> effective   = new HashSet<String>(500);
	private final HashSet<String> ineffective = new HashSet<String>(500);

	private boolean detected = false;
	private boolean init = false;
	
	@Override
	public void reset() throws Exception {
	}

	@Override
	public void addTestsuite(String name, String filename) throws Exception {
		if (detected) throw new RuntimeException();
	}

	@Override
	public void addTestcase(String testsuiteFile, String description)
			throws Exception {
		if (detected || invalid.contains(testsuiteFile + " " + description)) throw new RuntimeException();	}

	@Override
	public void startTestcase(String testsuiteFile, String description)
			throws Exception {
	}

	@Override
	public void finishTestcase(String testsuiteFile, String description,
			boolean succeeded, Collection<String> messages) throws Exception {
		
		final String key = testsuiteFile + " " + description;
		
		if (init) 
			(succeeded ? valid : invalid).add(key);
		else 
			if (!succeeded && valid.contains(key)) {
				ineffective.remove(key);
				effective.add(key);
				detected = true;
			}
	}
	
	public void init() {
		init = true;
	}
	
	public boolean newLanguage() {
		
		if (init) {
			ineffective.addAll(valid);
			init = false;
			return true;
		}  
		
		boolean result = detected;
		detected = false;
		return result;
	}
	
	public int getValid() {
		return valid.size();
	}
	
	public int getInvalid() {
		return invalid.size();
	}
	
	public int getEffective() {
		return effective.size();
	}
}
