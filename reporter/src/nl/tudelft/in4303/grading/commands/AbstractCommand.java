package nl.tudelft.in4303.grading.commands;

import com.beust.jcommander.Parameter;

import java.util.ArrayList;
import java.util.List;

abstract public class AbstractCommand {
    @Parameter(names = {"-g", "--grade"}, description = "Detailed report")
    protected boolean grade = false;

    @Parameter(names = {"-l", "--language"}, description = "Language to load (can use multiple times)")
    protected List<String> languages = new ArrayList<>();

    public abstract String getSolution();

    public abstract String getProject();

    public boolean isGrade() {
        return grade;
    }

    public List<String> getLanguages() {
        return languages;
    }
}
