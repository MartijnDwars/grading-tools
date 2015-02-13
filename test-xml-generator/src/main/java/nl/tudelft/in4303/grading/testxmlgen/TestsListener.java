package nl.tudelft.in4303.grading.testxmlgen;

import java.util.Collection;
import java.util.Map;

import org.metaborg.spt.listener.ITestReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.internal.Maps;

public class TestsListener implements ITestReporter {
    private static final Logger logger = LoggerFactory.getLogger(TestsListener.class);

    public static Map<String, String> fileToName = Maps.newHashMap();


    @Override public void addTestcase(String testsuiteFile, String description) throws Exception {

    }

    @Override public void addTestsuite(String name, String filename) throws Exception {
        fileToName.put(filename, name);
    }


    @Override public void reset() throws Exception {

    }

    @Override public void startTestcase(String testsuiteFile, String description) throws Exception {

    }

    @Override public void finishTestcase(String testsuiteFile, String description, boolean succeeded,
        Collection<String> messages) throws Exception {
        if(!succeeded) {
            logger.error("Test {} in {} failed! Consider removing it from tests.xml.", description, testsuiteFile);
        }
    }
}
