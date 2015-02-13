package nl.tudelft.in4303.grading.testxmlgen;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map.Entry;

import org.metaborg.spoofax.testrunner.core.TestRunner;

public class Main {
    private static String nl = System.getProperty("line.separator");


    public static void main(String[] args) throws Exception {
        final String testsDirectory = args[0];
        final Path testsDirectoryPath = Paths.get(testsDirectory);
        final String languageDirectory = args[1];

        final StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append(nl);
        sb.append("<report name=\"\">");
        sb.append(nl);
        sb.append("<group name=\"Correctness\">");
        sb.append(nl);

        final TestRunner runner = new TestRunner(testsDirectory, "testrunnerfile");
        runner.registerSPT();
        runner.registerLanguage(languageDirectory);
        runner.run();

        for(Entry<String, String> entry : TestsListener.fileToName.entrySet()) {
            final String suiteFile = entry.getKey();
            final String suiteName = entry.getValue();
            final Path suitePath = Paths.get(suiteFile);
            final String suiteRelativeFile = testsDirectoryPath.relativize(suitePath).toString();

            sb.append("<suite spt=\"" + suiteRelativeFile + "\" description=\"" + suiteName + "\" points=\"1.0\"/>");
            sb.append(nl);
        }

        sb.append("</group>");
        sb.append(nl);
        sb.append("</report>");
        sb.append(nl);

        final String output = sb.toString();
        System.out.println(output);
    }
}
