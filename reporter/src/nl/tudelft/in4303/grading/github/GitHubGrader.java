package nl.tudelft.in4303.grading.github;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

import nl.tudelft.in4303.grading.IGrader;
import nl.tudelft.in4303.grading.IResult;

import org.eclipse.egit.github.core.MergeStatus;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.PullRequestMarker;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.util.FileUtils;

public class GitHubGrader {

	static final String GRADING_ORGANISATION = "TUDelft-IN4303";
	static final String GRADING_CONTEXT = "grading/in4303";
	static final String autoComment  = "*Auto-generated comment*" + System.lineSeparator() + System.lineSeparator();
	static final String NO_GRADER = autoComment + "You need to create pull requests against the branch of the assignment you want to submit.";

	private final HashMap<String, IGrader> graders;

	private final GitHubService git;

	public GitHubGrader(String username, String password) {
		git = new GitHubService(username, password);		
		graders = new HashMap<String, IGrader>();
	}
	
	public void registerRunner(String exercise, IGrader runner) {
		graders.put(exercise, runner);
	}

	public void merge(String pattern, int late) {

		try {

			List<PullRequest> openPullRequests = git.getPullRequests(GRADING_ORGANISATION, pattern, "open");

			// Merge all open pull requests
			for (PullRequest request : openPullRequests) {
				PullRequestMarker base   = request.getBase();
				Repository        repo   = base.getRepo();
				int               number = request.getNumber();

				if (graders.get(base.getRef()) == null) {
					git.addComment(request, NO_GRADER);
				} else {
					MergeStatus status = git.merge(repo, number, "Merge submission");
					
					if (!status.isMerged()) {
						uploadReportAndStatus(request, new NoMergeResult());	
					} else if (late > 0) {
						git.addComment(request, autoComment + "This submission costs you " + late + " late days.");
					}
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void check(String pattern) {
		try {
			run(true, pattern);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void grade(String pattern) {
		try {
			run(false, pattern);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void resetState(String pattern, String expected) {
		try {
			for (PullRequest pullRequest : git.getPullRequests(GRADING_ORGANISATION, pattern, "closed")) 
				if (git.hasState(pullRequest, GRADING_CONTEXT, expected)) {
					ExtendedCommitStatus status = new ExtendedCommitStatus();
					status.setState("pending");
					status.setDescription("The assignment is being graded.");
					status.setContext(GRADING_CONTEXT);
					git.setStatus(pullRequest, status);	
				}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void run(boolean checkOnly, String pattern) throws IOException {

		List<PullRequest> requests = git.getPullRequests(GRADING_ORGANISATION, pattern, "open");
		
		for (PullRequest request : requests) 
			if (isGraded(request))
				requests.remove(request);
		
//		requests.addAll(git.getPullRequests(GRADING_ORGANISATION, pattern, "merged"));
		
		for (PullRequest request : requests) 
			{
				IGrader grader = graders.get(request.getBase().getRef());
				if (grader == null) {
					git.addComment(request, NO_GRADER);
						
					return;
				} 
				if (!checkOnly) {
					
					ExtendedCommitStatus status = new ExtendedCommitStatus();
					status.setState("pending");
					status.setDescription("The assignment is being graded.");
					status.setContext(GRADING_CONTEXT);
					git.setStatus(request, status);	
				}
				
				IResult report = gradePullRequest(request, grader, checkOnly);

				if (!checkOnly)
					uploadReportAndStatus(request, report);
				
			}
	}

	private boolean isGraded(PullRequest request) throws IOException {
		return git.hasState(request, GRADING_CONTEXT, "success") || git.hasState(request, GRADING_CONTEXT, "pending");
	}

	private IResult gradePullRequest(PullRequest pullRequest, IGrader grader, boolean checkOnly) {
		try {
			File tmpDir = createTemporaryDirectory();
			Git tmpRepo = git.checkout(pullRequest, tmpDir);
			
			IResult report;
			if (checkOnly)
				report = grader.check(tmpDir);
			else
				report = grader.grade(tmpDir);
			
			tmpRepo.close();
			
			return report;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (GitAPIException e) {
			throw new RuntimeException(e);
		} 
	}

	private void uploadReportAndStatus(PullRequest pullRequest, IResult report) throws IOException {
		ExtendedCommitStatus status = new ExtendedCommitStatus();

		switch (report.getStatus()) {
		case SUCCESS:
			status.setState("success");
			break;
		case FAILURE:
			status.setState("failure");
			break;
		case ERROR:
		default:
			status.setState("error");
			break;
		}
		status.setDescription(report.getStatusDescription());
		status.setContext(GRADING_CONTEXT);

		git.setStatus(pullRequest, status);
		if (pullRequest.isMerged())
			git.addComment(pullRequest, report.getGrade());
		else
			git.addComment(pullRequest, report.getFeedback());
	}

	private File createTemporaryDirectory() throws IOException {
		Path currentDir = FileSystems.getDefault().getPath(".");
		Path tmpDirPath = Files.createTempDirectory(currentDir, "in4303-");
		final File tmpDir = tmpDirPath.toFile();
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					FileUtils.delete(tmpDir, FileUtils.RECURSIVE);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}));
		return tmpDir;
	}

}
