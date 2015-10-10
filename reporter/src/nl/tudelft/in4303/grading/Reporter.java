package nl.tudelft.in4303.grading;

import com.beust.jcommander.JCommander;
import nl.tudelft.in4303.grading.commands.AbstractCommand;
import nl.tudelft.in4303.grading.commands.CommandLocal;
import nl.tudelft.in4303.grading.commands.CommandRemote;
import nl.tudelft.in4303.grading.github.GitHubGrader;
import nl.tudelft.in4303.grading.language.LanguageGrader;
import nl.tudelft.in4303.grading.local.LocalGrader;
import nl.tudelft.in4303.grading.tests.TestsGrader;
import org.apache.commons.configuration.ConfigurationUtils;

import java.io.File;

public class Reporter {
    public static void main(String[] args) {
        CommandLocal commandLocal = new CommandLocal();
        CommandRemote commandRemote = new CommandRemote();

        JCommander jCommander = new JCommander();
        jCommander.addCommand("local", commandLocal);
        jCommander.addCommand("remote", commandRemote);
        jCommander.parse(args);

        try {
            switch (jCommander.getParsedCommand()) {
                case "local":
                    gradeLocal(createReporter(commandLocal), commandLocal);
                    break;
                case "remote":
                    gradeRemote(createReporter(commandRemote), commandRemote);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Create a reporter depending on whether we're grading tests or a language
     *
     * @param command
     * @return
     */
    public static Grader createReporter(AbstractCommand command) {
        if (ConfigurationUtils.locate(command.getSolution(), "tests.xml") != null) {
            return new LanguageGrader(command.getSolution());
        } else {
            return new TestsGrader(command.getSolution());
        }
    }

    /**
     * Grade a local project
     *
     * @param reporter
     * @param commandLocal
     * @throws Exception
     */
    private static void gradeLocal(Grader reporter, CommandLocal commandLocal) throws Exception {
        LocalGrader grader = new LocalGrader();

        if (commandLocal.isGrade()) {
            grader.grade(reporter, new File(commandLocal.getProject()));
        } else {
            grader.feedback(reporter, new File(commandLocal.getProject()));
        }
    }

    /**
     * Grade a remote project
     *
     * @param reporter
     * @param commandRemote
     * @throws Exception
     */
    private static void gradeRemote(Grader reporter, CommandRemote commandRemote) throws Exception {
        GitHubGrader grader = GitHubGrader.fromConfig("gh.properties", commandRemote.isDryRun());

        if (commandRemote.isGrade()) {
            grader.grade(reporter, commandRemote.getProject(), commandRemote.getBranch(), commandRemote.getRepository(), commandRemote.getOrganisation());
        } else {
            grader.feedback(reporter, commandRemote.getProject(), commandRemote.getBranch(), commandRemote.getRepository(), commandRemote.getOrganisation(), -1);
        }
    }
}
