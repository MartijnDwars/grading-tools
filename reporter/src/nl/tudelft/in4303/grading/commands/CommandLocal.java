package nl.tudelft.in4303.grading.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import java.util.List;

@Parameters(separators = "=", commandDescription = "Grade local project")
public class CommandLocal extends AbstractCommand {
    @Parameter(description = "<solution> <project>")
    protected List<String> args;

    /**
     * Path to the solution project
     *
     * @return
     */
    public String getSolution() {
        return args.get(0);
    }

    /**
     * Absolute path to the student's submission
     *
     * @return
     */
    public String getProject() {
        return args.get(1);
    }
}
