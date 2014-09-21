package nl.tudelft.in4303.grading;

import java.io.File;

import nl.tudelft.in4303.grading.tests.TestsRunner;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

public class Main {

	public static void main(String[] args) {

		try {
						
			PropertiesConfiguration config = new PropertiesConfiguration("tests.properties");
			
			for (String repo : config.getStringArray("repo")) {

				System.out.println(repo);
				System.out.println();
				
				IRunner runner = new TestsRunner(new File(repo));
				System.out.println(runner.run().getReport());
			}
		} catch (ConfigurationException e) {
			e.printStackTrace();
			System.exit(1);
		}

		System.exit(0);
	}
}
