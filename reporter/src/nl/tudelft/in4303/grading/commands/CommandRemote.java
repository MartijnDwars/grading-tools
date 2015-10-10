package nl.tudelft.in4303.grading.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import java.util.List;

@Parameters(separators = "=", commandDescription = "Grade remote project")
public class CommandRemote extends AbstractCommand {
    @Parameter(description = "<solution> <project> <organisation> <repository> <branch>")
    protected List<String> args;

    @Parameter(names = {"-d", "--dryrun"}, description = "No GitHub interaction")
    protected boolean dryrun = false;

    /**
     * Path to the solution project
     *
     * @return
     */
    public String getSolution() {
        return args.get(0);
    }

    /**
     * Path to the student's project relative to the root of the repository,
     * i.e. MiniJava or MiniJava-tests-(syntax|names|types).
     *
     * @return
     */
    public String getProject() {
        return args.get(1);
    }

    /**
     * GitHub organisation (e.g. TUDelft-IN4303-2015)
     *
     * @return
     */
    public String getOrganisation() {
        return args.get(2);
    }

    /**
     * GitHub repository (e.g. student-johndoe)
     *
     * @return
     */
    public String getRepository() {
        return args.get(3);
    }

    /**
     * GitHub branch (e.g. assignment1)
     *
     * @return
     */
    public String getBranch() {
        return args.get(4);
    }

    /**
     * Do not interact with GitHub (i.e. no merging, no comment)
     *
     * @return
     */
    public boolean isDryRun() {
        return dryrun;
    }
}
