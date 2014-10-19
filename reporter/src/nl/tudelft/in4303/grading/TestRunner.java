package nl.tudelft.in4303.grading;

import java.io.File;
import java.io.IOException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.metaborg.sunshine.drivers.SunshineMainDriver;
import org.metaborg.sunshine.environment.ServiceRegistry;
import org.metaborg.sunshine.environment.SunshineMainArguments;
import org.metaborg.sunshine.services.language.LanguageDiscoveryService;

public class TestRunner {

	private final File dir;
	private final File cache;
	private static String builder;
	private final SunshineMainArguments params = new SunshineMainArguments();

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

		ServiceRegistry env = ServiceRegistry.INSTANCE();
		env.reset();
		
		params.builder = builder;
		params.filestobuildon = ".";
		params.noanalysis = true;
		params.project = dir.getPath();
		
		org.metaborg.sunshine.drivers.Main.initServices(env, params);
		
		return new SunshineMainDriver().run() == 0;
	}

	public static void registerLanguage(File esv) {

		ServiceRegistry.INSTANCE().getService(LanguageDiscoveryService.class)
				.discover(esv.getParentFile().getParentFile().toPath());
	}
}
