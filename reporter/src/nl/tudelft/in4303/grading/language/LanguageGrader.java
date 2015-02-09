package nl.tudelft.in4303.grading.language;

import java.io.File;
import java.util.List;

import nl.tudelft.in4303.grading.Grader;
import nl.tudelft.in4303.grading.IResult;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.metaborg.spoofax.testrunner.core.TestRunner;

public class LanguageGrader extends Grader {
	public LanguageGrader() {
		super("tests.xml");
	}

	@Override
	protected IResult grade(File repo, boolean checkOnly) throws Exception {
		
		listener.init();
		
		File lang = new File(repo, "MiniJava");
		File include = new File(lang, "include");
		File esv = new File(include, "MiniJava.packed.esv");
		
		if (!esv.exists()) {
			logger.error("missing ESV file");

			return new IResult() {
				
				@Override
				public String getStatusDescription() {
					return "Your submission misses important files.";
				}
				
				@Override
				public Status getStatus() {
					return Status.ERROR;
				}
				
				@Override
				public String getGrade() {
					return getFeedback();
				}
				
				@Override
				public String getFeedback() {
					return "Cannot find file `MiniJava/include/MiniJava.packed.esv`.";
				}
			};
		}
		
		try {
			TestRunner runner = new TestRunner(project.getAbsolutePath(), "testrunnerfile");
			runner.registerSPT();
			runner.registerLanguage(include.getAbsolutePath());
			
			logger.info("running tests");

			LanguageResult result = new LanguageResult(config.getString("[@name]", ""), listener);
			
			runTests(runner, result);
			
			if (!checkOnly && !result.hasErrors()) {
				result.finishedGroup(analyseTests(config.configurationAt("group")));
				result.succeed();
			}
			
			listener.exit();
			return result;

		} catch (final ConfigurationException e) {
			logger.error("SPT configuration", e);
			throw new RuntimeException(e);
		}
//		} catch (final Exception e) {
//			return new IResult() {
//				
//				@Override
//				public String getStatusDescription() {
//					return "Your submission crashes.";
//				}
//				
//				@Override
//				public Status getStatus() {
//					return Status.ERROR;
//				}
//				
//				@Override
//				public String getGrade() {
//					return getFeedback();
//				}
//				
//				@Override
//				public String getFeedback() {
//					StringWriter sw = new StringWriter();
//					PrintWriter pw = new PrintWriter(sw);
//					e.printStackTrace(pw);
//					return sw.toString();
//				}
//			};
//		}
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
			List<String> descriptions = listener.getDescriptions(spt);
			
			result.finishedSuite(passed, missed, descriptions, desc, points);
		}
		
		for (Object group : config.configurationsAt("group"))
			result.finishedGroup(analyseTests((HierarchicalConfiguration) group));

		result.succeed();
		
		return result;
	}
	
}
