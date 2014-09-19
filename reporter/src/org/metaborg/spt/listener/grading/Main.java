package org.metaborg.spt.listener.grading;

import java.io.File;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

public class Main {

	public static void main(String[] args) {

		try {
			GroupRunner runner = new GroupRunner("languages.xml",
					new File(new PropertiesConfiguration("tests.properties").getString("tests")));

			runner.run(System.out);
			
		} catch (ConfigurationException e) {
			e.printStackTrace();
			System.exit(1);
		}

		System.exit(0);
	}
}
