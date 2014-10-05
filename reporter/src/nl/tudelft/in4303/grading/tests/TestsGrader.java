package nl.tudelft.in4303.grading.tests;

import java.io.File;

import nl.tudelft.in4303.grading.Grader;
import nl.tudelft.in4303.grading.IResult;
import nl.tudelft.in4303.grading.TestRunner;

import org.apache.commons.configuration.HierarchicalConfiguration;

public class TestsGrader extends Grader {

	public TestsGrader() {
		super("languages.xml");
	}

	protected IResult grade(File repo, boolean checkOnly) {

		listener.init();
		TestRunner runner = new TestRunner(new File(repo, "MiniJava-tests"));
		
		System.out.println("Running reference language implementation");
		
		TestsResult result = runLanguages(runner, config);
		
		if (!checkOnly && !result.hasErrors()) {
		
			System.out.println("Running erroneous language implementations");
			result.finishedGroup(runTests(runner, config.configurationAt("group")));
		}
		
		listener.exit();
		return result;
	}

	private TestsResult runLanguages(TestRunner runner, HierarchicalConfiguration config) {
		
		TestsResult result = new TestsResult(config.getString("[@name]", ""),
				listener);

		try {

			for (Object current : config.configurationsAt("language")) {

				HierarchicalConfiguration langConf = (HierarchicalConfiguration) current;

				final String esvPath = langConf.getString("[@esv]");
				TestRunner.registerLanguage(new File(project, esvPath));

				System.out.println("# run " + esvPath);
				if (!runner.runTests())
					result.error("");

				result.finishedLanguage(listener.newLanguage(),
						langConf.getString("[@description]"), langConf.getDouble("[@points]", 0));
			}
			
			result.succeed();
			
		} catch (Exception e) {
			System.out.println("caught");
			e.printStackTrace(System.err);
			result.error(e.toString());
		}
		
		return result;
	}
	private TestsResult runTests(TestRunner runner, HierarchicalConfiguration config) {

		TestsResult result = runLanguages(runner, config);

		try {

			for (Object group : config.configurationsAt("group"))
				result.finishedGroup(runTests(runner, (HierarchicalConfiguration) group));

			result.succeed();

		} catch (Exception e) {
			result.error(e.toString());
		}
		return result;
	}
}
