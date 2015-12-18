package nl.tudelft.in4303.grading;

import java.util.*;

import org.metaborg.spt.listener.ITestReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestsListener implements ITestReporter {
	private static final Logger logger = LoggerFactory.getLogger(TestsListener.class);

	private final HashSet<String> valid       = new HashSet<String>(500);
	private final HashSet<String> invalid     = new HashSet<String>(500);
	private final HashSet<String> effective   = new HashSet<String>(500);
	private final HashSet<String> ineffective = new HashSet<String>(500);

	private final Hashtable<String, Integer> passed = new Hashtable<>();
	private final Hashtable<String, Integer> missed = new Hashtable<>();
	private final Hashtable<String, List<String>> descriptions = new Hashtable<>();

	private boolean active = false;
	private boolean detected = false;
	private boolean init = false;

	@Override
	public void reset() throws Exception {
	}

	@Override
	public void addTestsuite(String name, String filename) throws Exception {
		passed.put(filename, 0);
		missed.put(filename, 0);
		descriptions.put(filename, new ArrayList<String>());
//		if (active && detected) throw new RuntimeException();
	}

	@Override
	public void addTestcase(String testsuiteFile, String description)
			throws Exception {
//		if (active && (detected || invalid.contains(testsuiteFile + " " + description))) throw new RuntimeException()
	}

	@Override
	public void startTestcase(String testsuiteFile, String description)
			throws Exception {
	}

	@Override
	public void finishTestcase(String testsuiteFile, String description,
			boolean succeeded, Collection<String> messages) throws Exception {

		if (!active)
			return;

		if (succeeded) {
			passed.put(testsuiteFile, passed.get(testsuiteFile) + 1);
		} else {
			missed.put(testsuiteFile, missed.get(testsuiteFile) + 1);
			descriptions.get(testsuiteFile).add(description);
		}
		final String key = testsuiteFile + " " + description;

		if (init) {
			if (succeeded) {
				valid.add(key);
				logger.trace("valid test {} in suite {}", description, testsuiteFile);
			} else {
				invalid.add(key);
				logger.debug("invalid test '{}' in suite {}", description, testsuiteFile);
			}
		} else {
			if (succeeded) {
				logger.trace("ineffective test {} in suite {}", description, testsuiteFile);
			} else if (valid.contains(key) && !invalid.contains(key)) {
				ineffective.remove(key);
				effective.add(key);
				detected = true;
				logger.debug("effective test {} in suite {}", description, testsuiteFile);
			}
		}
	}

	public void init() {
		active = true;
		init = true;
		valid.clear();
		invalid.clear();
		effective.clear();
		ineffective.clear();
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

	public void exit() {
		active = false;
	}

	public int getPassed(String spt) {
		return passed.get(spt);
	}

	public int getMissed(String spt) {
		return missed.get(spt);
	}

	public List<String> getDescriptions(String spt) {
		return descriptions.get(spt);
	}
}
