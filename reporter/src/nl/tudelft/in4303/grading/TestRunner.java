package nl.tudelft.in4303.grading;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PushbackInputStream;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
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

public class TestRunner {

	private final File dir;
	private final File cache;
	private static String builder;
	private final SunshineMainArguments params = new SunshineMainArguments();
	private final Environment env = Environment.INSTANCE();

	public TestRunner(File dir) {

		this.dir = dir;
		this.cache = new File(dir, ".cache");
		if (builder == null) {
			try {
				PropertiesConfiguration sptConfig = new PropertiesConfiguration(
						"spt.properties");
				builder = sptConfig.getString("spt.builder");
				final File spt = new File(sptConfig.getFile().getParentFile(),
						sptConfig.getString("spt.esv"));
				registerLanguage(spt);
			} catch (ConfigurationException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean runTests() {

		try {
			FileUtils.deleteDirectory(cache);
		} catch (IOException e) {
		}

		env.setMainArguments(params);
		env.setProjectDir(dir);
		params.builder = builder;
		params.filestobuildon = ".";
		params.noanalysis = true;

		return new SunshineMainDriver().run() == 0;
	}

	public static void registerLanguage(File esv) {

		try {
			PushbackInputStream input = new PushbackInputStream(
					new FileInputStream(esv), 100);
			byte[] buffer = new byte[6];
			int bufferSize = input.read(buffer);
			if (bufferSize != -1)
				input.unread(buffer, 0, bufferSize);

			if ((bufferSize == 6 && new String(buffer).equals("Module"))) {
				TermReader reader = new TermReader(
						new TermFactory()
								.getFactoryWithStorageType(IStrategoTerm.MUTABLE));
				IStrategoAppl document = (IStrategoAppl) reader
						.parseFromStream(input);
				ALanguage lang = LanguageDiscoveryService.INSTANCE()
						.languageFromEsv(document,
								esv.getParentFile().getParentFile().toPath());
				LanguageService.INSTANCE().registerLanguage(lang);
			}
		} catch (IOException e) {
			throw new RuntimeException("Failed to load language", e);
		}
	}
}
