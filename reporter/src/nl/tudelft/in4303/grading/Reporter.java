package nl.tudelft.in4303.grading;

import com.beust.jcommander.JCommander;
import nl.tudelft.in4303.grading.commands.AbstractCommand;
import nl.tudelft.in4303.grading.commands.CommandLocal;
import nl.tudelft.in4303.grading.commands.CommandMerge;
import nl.tudelft.in4303.grading.commands.CommandRemote;
import nl.tudelft.in4303.grading.github.GitHubGrader;
import nl.tudelft.in4303.grading.github.GitHubService;
import nl.tudelft.in4303.grading.language.LanguageGrader;
import nl.tudelft.in4303.grading.local.LocalGrader;
import nl.tudelft.in4303.grading.tests.TestsGrader;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConfigurationUtils;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.eclipse.egit.github.core.PullRequest;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.TimeZone;

public class Reporter {
    public static void main(String[] args) {
        CommandLocal commandLocal = new CommandLocal();
        CommandRemote commandRemote = new CommandRemote();
        CommandMerge commandMerge = new CommandMerge();

        JCommander jCommander = new JCommander();
        jCommander.addCommand("local", commandLocal);
        jCommander.addCommand("remote", commandRemote);
        jCommander.addCommand("merge", commandMerge);
        jCommander.parse(args);

        try {
            switch (jCommander.getParsedCommand()) {
                case "local":
                    gradeLocal(createReporter(commandLocal), commandLocal);
                    break;
                case "remote":
                    gradeRemote(createReporter(commandRemote), commandRemote);
                    break;
                case "merge":
                    merge(commandMerge);
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
            return new LanguageGrader(command.getSolution(), command.getLanguages());
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

    /**
     * Merge all open pull requests against given branch
     *
     * @param commandMerge
     * @throws ConfigurationException
     * @throws IOException
     */
    private static void merge(CommandMerge commandMerge) throws ConfigurationException, IOException {
        Configuration configuration = new PropertiesConfiguration("gh.properties");

        GitHubService gitHubService = new GitHubService(configuration.getString("token"));
        gitHubService.runDry(commandMerge.isDryRun());

        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy'T'HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("CET"));

        for (PullRequest pullRequest : gitHubService.getPullRequests("TUDelft-IN4303-2015", "student-(.*)", commandMerge.getBranch(), "open")) {
            LocalDate deadline = deadline(commandMerge.getBranch());
            LocalDate createdAt = pullRequest.getCreatedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            if (createdAt.isBefore(deadline)) {
                System.out.println("Merging #" + pullRequest.getNumber() + " for " + pullRequest.getBase().getRepo().getName() + " created at " + format.format(pullRequest.getCreatedAt()));
            } else {
                int lateDays = Period.between(deadline, createdAt).getDays() + 1;

                System.out.println("Merging #" + pullRequest.getNumber() + " for " + pullRequest.getBase().getRepo().getName() + " created at " + format.format(pullRequest.getCreatedAt()) + " (late days: " + lateDays + ")");

                gitHubService.addComment(pullRequest, "This submission costs you " + lateDays + " late day(s).");
            }

            gitHubService.merge(pullRequest.getBase().getRepo(), pullRequest.getNumber(), "Merge submission");
        }
    }

    /**
     * Get date of the deadline for the given assignment
     *
     * @param assignment
     * @return
     */
    private static LocalDate deadline(String assignment) {
        switch (assignment) {
            case "assignment11":
                return LocalDate.of(2015, 12, 13+1);
            case "assignment12":
                return LocalDate.of(2015, 12, 20+1);
            default:
                throw new IllegalArgumentException("Assignment " + assignment + " now known.");
        }
    }
}
