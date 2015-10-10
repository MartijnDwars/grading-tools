package nl.tudelft.in4303.grading.local;

import nl.tudelft.in4303.grading.Grader;
import nl.tudelft.in4303.grading.IResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class LocalGrader {
    private static final Logger logger = LoggerFactory.getLogger(LocalGrader.class);

    public void grade(Grader grader, File dir) throws Exception {
        IResult report = grader.grade(dir);

        System.out.println(report.getGrade());
    }

    public void feedback(Grader grader, File dir) throws Exception {
        IResult report = grader.grade(dir);

        System.out.println(report.getFeedback());
    }
}
