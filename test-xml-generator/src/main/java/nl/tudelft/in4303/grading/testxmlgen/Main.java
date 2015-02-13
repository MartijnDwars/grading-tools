package nl.tudelft.in4303.grading.testxmlgen;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
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

        for(Entry<String, Collection<String>> entry : TestsListener.fileToTestcases.asMap().entrySet()) {
            final String suiteFile = entry.getKey();
            final Path suitePath = Paths.get(suiteFile);
            final String suiteRelativeFile = testsDirectoryPath.relativize(suitePath).toString();
            final String suiteName = TestsListener.fileToName.get(suiteFile);
            final Collection<String> names = entry.getValue();

            sb.append("<group name=\"" + capitalizeFirstLetter(suiteName) + "\">");
            sb.append(nl);
            for(String name : names) {
                sb.append("<suite spt=\"" + suiteRelativeFile + "\" description=\"" + name + "\" points=\"1.0\"/>");
                sb.append(nl);
            }
            sb.append("</group>");
            sb.append(nl);
        }

        sb.append("</group>");
        sb.append(nl);
        sb.append("</report>");
        sb.append(nl);

        final String output = sb.toString();
        System.out.println(output);
    }

    private static String capitalizeFirstLetter(String original) {
        if(original.length() == 0)
            return original;
        return original.substring(0, 1).toUpperCase() + original.substring(1);
    }
}
