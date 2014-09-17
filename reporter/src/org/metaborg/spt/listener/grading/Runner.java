package org.metaborg.spt.listener.grading;

public class Runner {

	/**
	 * Example of how to use this class.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		org.metaborg.sunshine.drivers.Main.main(
			new String[] { 
				"--auto-lang", "/Users/guwac/Cloud/git/in4303/grading/lab1/grammars/MiniJava-correct",
				"--project", "/Users/guwac/Cloud/git/in4303/grading/lab2/tests/",
				"--builder", "testrunnerfile", "--build-on-all", ".", "--no-analysis"//, "--non-incremental"
			});
	}
}
