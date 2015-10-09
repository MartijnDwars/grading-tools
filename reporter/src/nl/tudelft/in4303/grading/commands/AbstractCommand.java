package nl.tudelft.in4303.grading.commands;

import com.beust.jcommander.Parameter;

abstract public class AbstractCommand {
    @Parameter(names = {"-g", "--grade"}, description = "Detailed report")
    protected boolean grade = false;

    public abstract String getSolution();

    public abstract String getProject();

    public boolean isGrade() {
        return grade;
    }
}
