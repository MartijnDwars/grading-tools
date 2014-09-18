package org.metaborg.spt.listener.grading;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.util.Iterator;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.io.FileUtils;
import org.metaborg.spt.listener.ITestReporter;
import org.metaborg.spt.listener.TestReporterProvider;
import org.metaborg.sunshine.Environment;
import org.metaborg.sunshine.drivers.SunshineMainArguments;
import org.metaborg.sunshine.drivers.SunshineMainDriver;
import org.metaborg.sunshine.services.language.ALanguage;
import org.metaborg.sunshine.services.language.LanguageDiscoveryService;
import org.metaborg.sunshine.services.language.LanguageService;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.TermFactory;
import org.spoofax.terms.io.binary.TermReader;

public class Runner {

	
	public static void main(String[] args) {
	
		int i = 0;
		final StringBuilder report = new StringBuilder();
		final int[] detection = new int[]{0, 0};
		
		try {
			final PropertiesConfiguration sptConfig   = new PropertiesConfiguration("spt.properties");
			final PropertiesConfiguration testsConfig = new PropertiesConfiguration("tests.properties");
			final XMLConfiguration langConfig         = new XMLConfiguration("languages.xml");

			final String sptDir   = sptConfig.getFile().getParentFile().getPath() + "/";
//			final String testsDir = testsConfig.getFile().getParentFile().getPath() + "/";
			final String langDir  = langConfig.getFile().getParentFile().getPath() + "/";
			
			register(sptDir + sptConfig.getString("spt.esv"));
			final String tests   = testsConfig.getString("tests");
			final String builder = sptConfig.getString("spt.builder");

			i += runGroup(report, "# ", langDir, langConfig, tests, builder, detection); 
			
		} catch (ConfigurationException e) {
			e.printStackTrace();
			i = 1;
		}
		
		for (Iterator<ITestReporter> reporters = TestReporterProvider.getInstance().getReporters(); reporters.hasNext();) {
			ITestReporter reporter = reporters.next();
			if (reporter instanceof Grader) {
				Grader grader = (Grader) reporter;
				final int valid = grader.getValid();
				final int effective = grader.getEffective();
				report.append("## Summary");
				report.append(System.getProperty("line.separator"));
				report.append(System.getProperty("line.separator"));
				report.append("You have " + valid + " valid tests.");
				report.append(System.getProperty("line.separator"));
				report.append("You have " + grader.getInvalid() + " invalid tests.");
				report.append(System.getProperty("line.separator"));
				report.append(effective + " of your valid tests detected erroneous grammars.");
//				report.append(System.getProperty("line.separator"));
//				report.append("You detected " + detection[0] + " erroneous grammars.");
//				report.append(System.getProperty("line.separator"));
//				report.append("You missed " + detection[1] + " erroneous grammars.");
//				report.append(System.getProperty("line.separator"));
			}
		}
		
		System.out.println(report.toString());
		System.exit(i);
	}
	
	public static int runGroup(StringBuilder report, String header, String project, HierarchicalConfiguration config,
			final String tests, final String builder, final int[] detection) {
		
		int i = 0;
		for (String variant: config.getStringArray("language[@esv]")) {
			i += runVariant(project, tests, builder, detection, variant);
		}
		
		int g = 0;
		for (Object group: config.getList("group[@name]")) {
			
			reportGroupHeader(report, header + group);
			final int[] inner = new int[]{0, 0};
			final StringBuilder subreport = new StringBuilder();
			i += runGroup(subreport, "#" + header, project, config.configurationAt("group(" + g++ +")"), tests, builder, inner);
			detection[0] += inner[0];
			detection[1] += inner[1];
			reportGroupSuccess(report, subreport, group.toString(), detection, inner);
		}
		return i;
	}

	private static int runVariant(String project, final String tests,
			final String builder, final int[] detection, String variant) {
		
		for (Iterator<ITestReporter> reporters = TestReporterProvider.getInstance().getReporters(); reporters.hasNext();) {
			ITestReporter reporter = reporters.next();
			if (reporter instanceof Grader) {
				Grader grader = (Grader) reporter;
				grader.setLanguage(variant);
			}
		}
		
		int i = runTests(project + variant, tests, builder); 
		
		for (Iterator<ITestReporter> reporters = TestReporterProvider.getInstance().getReporters(); reporters.hasNext();) {
			ITestReporter reporter = reporters.next();
			if (reporter instanceof Grader) {
				Grader grader = (Grader) reporter;
				if (grader.isDetected())
					detection[0]++;
				else detection[1]++;
			}
		}
		return i;
	}

	public static int runTests(String language, String project, String builder) {
	
		try {
			FileUtils.deleteDirectory(new File(project+".cache"));
		} catch (IOException e) {}
	 
		SunshineMainArguments params = new SunshineMainArguments();
		params.builder = builder;
		params.filestobuildon = ".";
		params.noanalysis = true;
		
		Environment env = Environment.INSTANCE();
		env.setMainArguments(params);
		env.setProjectDir(new File(project));
		
		register(language);
		
//		LanguageService.INSTANCE().registerLanguage(
//					LanguageDiscoveryService.INSTANCE().languageFromArguments(params.languageArgs));
		
		if (new SunshineMainDriver().run() == 0)
			return 0;
		else 
			return 1;
	}

	public static void register(String esv) {
		
		IStrategoAppl document = null;

		try {
			PushbackInputStream input = new PushbackInputStream(new FileInputStream(esv), 100);
			byte[] buffer = new byte[6];
			int bufferSize = input.read(buffer);
			if (bufferSize != -1)
				input.unread(buffer, 0, bufferSize);
			
			if ((bufferSize == 6 && new String(buffer).equals("Module"))) { 
				TermReader reader = new TermReader(
						new TermFactory().getFactoryWithStorageType(IStrategoTerm.MUTABLE));
				document = (IStrategoAppl) reader.parseFromStream(input);
			} 
			
			ALanguage lang = LanguageDiscoveryService.INSTANCE().languageFromEsv(document, new File(esv).toPath().getParent().getParent());
			LanguageService.INSTANCE().registerLanguage(lang);
		} catch (IOException e) {
			throw new RuntimeException("Failed to load language", e); 
		}
	}
	
	private static void reportGroupHeader(StringBuilder report, String header) {
		report.append(header);
		report.append(System.getProperty("line.separator"));
		report.append(System.getProperty("line.separator"));
	}
	
	private static void reportGroupSuccess(StringBuilder report, StringBuilder subreport, String group, final int[] detection, final int[] inner) {
		
		final int detected = detection[0];
		final int missed   = detection[1];
		if (detected == 0) {
			report.append("You missed all erroneous grammars.");
			report.append(System.getProperty("line.separator"));
			report.append(System.getProperty("line.separator"));
			return;
		}
		if (missed == 0) {
			report.append("You detected all erroneous grammars.");
			report.append(System.getProperty("line.separator"));
			report.append(System.getProperty("line.separator"));
			return;
		}
		
		if (detected/missed >= 3.0) {
			report.append("You detected many erroneous grammars.");
			report.append(System.getProperty("line.separator"));
			report.append(System.getProperty("line.separator"));
			if (inner[0] > 0) {
				report.append(subreport);
				report.append(System.getProperty("line.separator"));
				report.append(System.getProperty("line.separator"));
			}
			return;
		}
		
		if (detected > missed) {
			report.append("You detected more erroneous grammars than you missed.");
			report.append(System.getProperty("line.separator"));
			report.append(System.getProperty("line.separator"));
			if (inner[0] > 0) {
				report.append(subreport);
				report.append(System.getProperty("line.separator"));
				report.append(System.getProperty("line.separator"));
			}
			return;			
		}
		
		if (missed/detected >= 3.0) {
			report.append("You missed many erroneous grammars.");
			report.append(System.getProperty("line.separator"));
			report.append(System.getProperty("line.separator"));
			if (inner[0] > 0) {
				report.append(subreport);
				report.append(System.getProperty("line.separator"));
				report.append(System.getProperty("line.separator"));
			}
			return;
		}
		
		if (missed > detected) {
			report.append("You missed more erroneous grammars than you detected.");
			report.append(System.getProperty("line.separator"));
			report.append(System.getProperty("line.separator"));
			if (inner[0] > 0) {
				report.append(subreport);
				report.append(System.getProperty("line.separator"));
				report.append(System.getProperty("line.separator"));
			}
			return;			
		}
	}

}
