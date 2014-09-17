package org.metaborg.spt.listener.grading;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class Runner {

	/**
	 * Example of how to use this class.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			FileUtils.deleteDirectory(new File("/Users/guwac/Cloud/git/in4303/grading/lab2/tests/.cache"));
		} catch (IOException e) {}
	 
		org.metaborg.sunshine.drivers.Main.main(
			new String[] { 
				"--auto-lang", "/Users/guwac/Cloud/git/in4303/grading/lab1/grammars/MiniJava-correct",
				"--project", "/Users/guwac/Cloud/git/in4303/grading/lab2/tests/",
				"--builder", "testrunnerfile", "--build-on-all", ".", "--no-analysis"//, "--non-incremental"
			});
	}
}
