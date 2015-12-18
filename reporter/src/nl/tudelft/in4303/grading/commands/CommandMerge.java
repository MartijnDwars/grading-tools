package nl.tudelft.in4303.grading.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import java.util.List;

@Parameters(separators = "=", commandDescription = "Merge all PRs for assignment")
public class CommandMerge {
    @Parameter(description = "<branch>")
    protected List<String> args;

    @Parameter(names = {"-d", "--dryrun"}, description = "No GitHub interaction")
    protected boolean dryrun = false;

    /**
     * Branch to merge
     *
     * @return
     */
    public String getBranch() {
        return args.get(0);
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
