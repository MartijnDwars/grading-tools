package org.metaborg.spt.listener.grading;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class Runner {

	public static void main(String[] args) {

		runTests("/Users/guwac/Cloud/git/in4303/grading/lab2/tests/");
	}

	public static void runTests(String project) {
	
		try {
			FileUtils.deleteDirectory(new File(project+".cache"));
		} catch (IOException e) {}
	 
		org.metaborg.sunshine.drivers.Main.main(
			new String[] { 
				"--auto-lang", "/Users/guwac/Cloud/git/in4303/grading/lab1/grammars/MiniJava-correct",
				"--project", project,
				"--builder", "testrunnerfile", "--build-on-all", ".", "--no-analysis"//, "--non-incremental"
			});
	}
}
