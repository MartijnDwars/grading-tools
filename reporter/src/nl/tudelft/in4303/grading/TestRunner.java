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

	private final File cache;
	
	public TestRunner(File dir) throws ConfigurationException  {
		PropertiesConfiguration sptConfig = new PropertiesConfiguration("spt.properties");

		final SunshineMainArguments params = new SunshineMainArguments();

		this.cache = new File(dir, ".cache");
		params.project        = dir.getPath();
		params.filestobuildon = ".";
		params.noanalysis     = true;
		params.builder        = sptConfig.getString("spt.builder");
		
		File spt = new File(sptConfig.getFile().getParentFile(), sptConfig.getString("spt.esv"));

		ServiceRegistry env = ServiceRegistry.INSTANCE();
		env.reset();

		org.metaborg.sunshine.drivers.Main.initServices(env, params);
		registerLanguage(spt.getParentFile());
	}

	public boolean runTests() {

		try {
			FileUtils.deleteDirectory(cache);
		} catch (IOException e) {
		}

		return new SunshineMainDriver().run() == 0;
	}

	public void registerLanguage(File dir) {

		LanguageDiscoveryService discoveryService = ServiceRegistry.INSTANCE().getService(LanguageDiscoveryService.class);
		
		discoveryService.discover(dir.toPath());
	}
}
