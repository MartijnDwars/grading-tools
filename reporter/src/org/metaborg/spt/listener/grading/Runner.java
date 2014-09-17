package org.metaborg.spt.listener.grading;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;

import org.apache.commons.io.FileUtils;
import org.metaborg.sunshine.Environment;
import org.metaborg.sunshine.drivers.SunshineMainArguments;
import org.metaborg.sunshine.drivers.SunshineMainDriver;
import org.metaborg.sunshine.services.language.LanguageDiscoveryService;

public class Runner {

	public static void main(String[] args) {

		runTests("/Users/guwac/Cloud/git/in4303/grading/lab1/grammars/MiniJava-correct", "/Users/guwac/Cloud/git/in4303/grading/lab2/tests/");
	}

	public static void runTests(String language, String project) {
	
		try {
			FileUtils.deleteDirectory(new File(project+".cache"));
		} catch (IOException e) {}
	 
		SunshineMainArguments params = new SunshineMainArguments();
		params.autolang = language;
		params.project = project;
		params.builder = "testrunnerfile";
		params.filestobuildon = ".";
		params.noanalysis = true;
		params.validate();
		
		Environment env = Environment.INSTANCE();
		env.setMainArguments(params);
		env.setProjectDir(new File(project));
		LanguageDiscoveryService.INSTANCE().discover(
					FileSystems.getDefault().getPath(language));
		
//		LanguageService.INSTANCE().registerLanguage(
//					LanguageDiscoveryService.INSTANCE().languageFromArguments(params.languageArgs));
		
		if (new SunshineMainDriver().run() == 0) {
			System.exit(0);
		} else {
			System.exit(1);
		}
	}
}
