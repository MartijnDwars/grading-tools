package org.metaborg.spt.listener.grading;

import java.io.File;
import java.io.PrintStream;
import java.util.Iterator;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.metaborg.spt.listener.ITestReporter;
import org.metaborg.spt.listener.TestReporterProvider;

public class GroupRunner {

	private final XMLConfiguration config;
	private final TestRunner runner;
	private final File project;
	private final TestGrader grader;
	
	public GroupRunner(String config, File tests) throws ConfigurationException {
		this.config  = new XMLConfiguration(config);
		this.project = this.config.getFile().getParentFile();
		this.runner  = new TestRunner(tests);
		
		ITestReporter reporter = null;
		
		Iterator<ITestReporter> reporters = 
				TestReporterProvider.getInstance().getReporters(); 
		
		while ( !(reporter instanceof TestGrader) && reporters.hasNext())
			reporter = reporters.next(); 
		
		grader = (TestGrader) reporter;
	}
	
	public void run(PrintStream stream) {
		
		grader.init();	
		runGroup(config).finishedGrading(stream, grader);
	}
	
	private GroupResult runGroup(HierarchicalConfiguration config) {
		
		GroupResult result = new GroupResult(config.getString("[@name]", ""));
		
		for (Object current : config.configurationsAt("language")) {
			
			HierarchicalConfiguration langConf = (HierarchicalConfiguration) current;
			
			final String esvPath = langConf.getString("[@esv]");
			TestRunner.registerLanguage(new File(project, esvPath));			
			
			runner.runTests();
			
			result.finishedLanguage(grader.newLanguage(), langConf.getDouble("[@points]", 1));
		}
		
		for (Object group : config.configurationsAt("group"))
			result.finishedGroup(runGroup((HierarchicalConfiguration) group)); 
		
		return result;
	}
}
