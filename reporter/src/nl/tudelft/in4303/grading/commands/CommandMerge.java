package nl.tudelft.in4303.grading.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import java.util.List;

@Parameters(separators = "=", commandDescription = "Merge all PRs for assignment")
public class CommandMerge {
    @Parameter(description = "<branch>")
    protected List<String> args;

    /**
     * Branch to merge
     *
     * @return
     */
    public String getBranch() {
        return args.get(0);
    }
}
