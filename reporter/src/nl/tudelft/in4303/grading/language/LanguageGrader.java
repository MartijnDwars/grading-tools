package nl.tudelft.in4303.grading.language;

import java.io.File;

import nl.tudelft.in4303.grading.Grader;
import nl.tudelft.in4303.grading.IResult;
import nl.tudelft.in4303.grading.TestRunner;

import org.apache.commons.configuration.HierarchicalConfiguration;

public class LanguageGrader extends Grader {

	public LanguageGrader() {
		super("tests.xml");
	}

	@Override
	protected IResult grade(File repo, boolean checkOnly) {
		
		listener.init();
		
		TestRunner.registerLanguage(new File(new File(new File(repo, "MiniJava"), "include"), "MiniJava.packed.esv"));
		
		LanguageResult result = new LanguageResult(config.getString("[@name]", ""), listener);
		
		if (!new TestRunner(project).runTests())
			result.error("");
		
		if (!checkOnly && !result.hasErrors()) {
			result.finishedGroup(analyseTests(config.configurationAt("group")));
			result.succeed();
		}
		
		listener.exit();
		return result;
	}

	private LanguageResult analyseTests(HierarchicalConfiguration config) {
		
		LanguageResult result = new LanguageResult(config.getString("[@name]", ""), listener);
		
		for (Object current : config.configurationsAt("suite")) {

			HierarchicalConfiguration suiteConf = (HierarchicalConfiguration) current;

			String spt    = new File(project, suiteConf.getString("[@spt]")).getAbsolutePath();
			double points = suiteConf.getDouble("[@points]", 0);
			String desc   = suiteConf.getString("[@description]");
			int    passed = listener.getPassed(spt);
			int    missed = listener.getMissed(spt);
	
			result.finishedSuite(passed, missed, points, desc);
		}
		
		for (Object group : config.configurationsAt("group"))
			result.finishedGroup(analyseTests((HierarchicalConfiguration) group));

		result.succeed();
		
		return result;
	}
	
}
