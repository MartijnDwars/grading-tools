package org.metaborg.spt.listener.grading;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

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

public class Runner {

	public static void main(String[] args) {

		runTests("/Users/guwac/Cloud/git/in4303/grading/lab1/grammars/MiniJava-correct", "/Users/guwac/Cloud/git/in4303/grading/lab2/tests/");
	}

	public static void runTests(String language, String project) {
	
		try {
			FileUtils.deleteDirectory(new File(project+".cache"));
		} catch (IOException e) {}
	 
		SunshineMainArguments params = new SunshineMainArguments();
		params.builder = "testrunnerfile";
		params.filestobuildon = ".";
		params.noanalysis = true;
		
		Environment env = Environment.INSTANCE();
		env.setMainArguments(params);
		env.setProjectDir(new File(project));
		
		Path locationPath = FileSystems.getDefault().getPath(language);
		assert locationPath != null;
		File location = locationPath.toFile();
		if (!location.exists() || !location.isDirectory() || !location.canRead()) {
			throw new RuntimeException(
					"Language source location is does not exist, is not a directory or cannot be read: "
							+ location.getAbsolutePath());
		}
		
		Collection<ALanguage> languages = new LinkedList<ALanguage>();
		
		Iterator<File> esvs = FileUtils.iterateFiles(location, new String[] { "packed.esv" }, true);
		while (esvs.hasNext()) {
			try {
				ALanguage language1 = readFromESV(esvs.next());
				languages.add(language1);
			} catch (IOException e) {
				throw new RuntimeException("Failed to load language", e);
			}
		}
		LanguageService.INSTANCE().registerLanguage(languages);
		
//		LanguageService.INSTANCE().registerLanguage(
//					LanguageDiscoveryService.INSTANCE().languageFromArguments(params.languageArgs));
		
		if (new SunshineMainDriver().run() == 0) {
			System.exit(0);
		} else {
			System.exit(1);
		}
	}

	public static ALanguage readFromESV(File esv)
			throws FileNotFoundException, IOException {
		
		IStrategoAppl document = null;

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
		return LanguageDiscoveryService.INSTANCE().languageFromEsv(document, esv.toPath().getParent().getParent());
	}
}
