package nl.tudelft.in4303.grading;

import com.beust.jcommander.Parameter;

import java.util.ArrayList;
import java.util.List;

public class CLOptions {
    @Parameter(description = "branch, student and solution to grade against")
    private List<String> parameters = new ArrayList<>();

    @Parameter(names = {"-g", "--grade"}, description = "generate detailed report")
    private boolean details = false;

    @Parameter(names = {"-t", "--test"}, description = "do not send comments to github")
    private boolean runDry = false;

    @Parameter(names = {"-l", "--late"}, description = "number of late days")
    private int late = -1;

    public String getBranch() {
        return parameters.get(0);
    }

    public String getStudent() {
        return parameters.get(1);
    }

    public String getSolution() {
        return parameters.get(2);
    }

    public boolean reportDetails() {
        return details;
    }

    public boolean runDry() {
        return runDry;
    }

    public int getLate() {
        return late;
    }
}
