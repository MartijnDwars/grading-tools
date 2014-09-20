package org.metaborg.spt.listener.grading;

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
	private final String builder;
	private final SunshineMainArguments params = new SunshineMainArguments();
	
	public TestRunner(File dir) throws ConfigurationException {

		this.dir = dir;
		this.cache = new File(dir, ".cache");

		final PropertiesConfiguration sptConfig = new PropertiesConfiguration(
				"spt.properties");

		this.builder = sptConfig.getString("spt.builder");
		
		final File spt = new File(sptConfig.getFile().getParentFile(),
				sptConfig.getString("spt.esv"));
		registerLanguage(spt);
	}

	public int runTests() {

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
		
		return new SunshineMainDriver().run();
	}

	public static void registerLanguage(File esv) {

		LanguageDiscoveryService langDiscovery = ServiceRegistry.INSTANCE()
				.getService(LanguageDiscoveryService.class);
		assert langDiscovery != null;
			langDiscovery.discover(esv.toPath());

	}
}
