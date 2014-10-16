package nl.tudelft.in4303.grading;

import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;

import org.metaborg.spt.listener.ITestReporter;

/**
 * @author guwac
 *
 */
public class TestsListener implements ITestReporter {
	
	private final HashSet<String> valid       = new HashSet<String>(500);
	private final HashSet<String> invalid     = new HashSet<String>(500);
	private final HashSet<String> effective   = new HashSet<String>(500);
	private final HashSet<String> ineffective = new HashSet<String>(500);

	private final Hashtable<String, Integer> passed = new Hashtable<>();
	private final Hashtable<String, Integer> missed = new Hashtable<>();
	
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
		if (active && detected) throw new RuntimeException();
	}

	@Override
	public void addTestcase(String testsuiteFile, String description)
			throws Exception {
		if (active && (detected || invalid.contains(testsuiteFile + " " + description))) throw new RuntimeException();	}

	@Override
	public void startTestcase(String testsuiteFile, String description)
			throws Exception {
	}

	@Override
	public void finishTestcase(String testsuiteFile, String description,
			boolean succeeded, Collection<String> messages) throws Exception {
		
		if (succeeded) 
			System.out.println("## Passed test " + description + " in suite " + testsuiteFile);
		else
			System.out.println("## Failed test " + description + " in suite " + testsuiteFile);
			
		if (!active)
			return;
		
		System.out.println("* active");
		
		if (succeeded)
			passed.put(testsuiteFile, passed.get(testsuiteFile) + 1);
		else
			missed.put(testsuiteFile, missed.get(testsuiteFile) + 1);
		
		final String key = testsuiteFile + " " + description;
		
		if (init) {
			if (succeeded) {
				System.out.println("* valid");
				valid.add(key);
			} else {
				System.out.println("* invalid");
				invalid.add(key);
			}
		} else { 
			if (!succeeded && valid.contains(key)) {
				ineffective.remove(key);
				effective.add(key);
				detected = true;
				System.out.println("* effective");
			} else {
				if (succeeded) 
					System.out.println("* ineffective");
				else
					System.out.println("* invalid");
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
}
