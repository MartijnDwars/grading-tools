package nl.tudelft.in4303.grading.github;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

import nl.tudelft.in4303.grading.Grader;
import nl.tudelft.in4303.grading.IResult;
import nl.tudelft.in4303.grading.IResult.Status;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.eclipse.egit.github.core.MergeStatus;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GitHubGrader {
	private static final Logger logger = LoggerFactory.getLogger(GitHubGrader.class);
	
	static final String GRADING_ORGANISATION = "TUDelft-IN4303";
	static final String GRADING_CONTEXT = "grading/in4303";

	static final String autoComment  = "*Auto-generated comment*" + System.lineSeparator() + System.lineSeparator();
	
	static final String NO_MERGE  = autoComment + "Make sure your branch can be merged into the branch of the assignment.";
	static final String NO_GRADER = autoComment + "You need to create pull requests against the branch of the assignment you want to submit.";

	private final GitHubService git;

	public static GitHubGrader fromConfig(String config, boolean runDry) throws ConfigurationException {
		return new GitHubGrader(new PropertiesConfiguration(config), runDry);
	}
	
	public GitHubGrader(AbstractConfiguration config, boolean runDry) {
		this(config.getString("token"), runDry);
	}

	public GitHubGrader(String token, boolean runDry) {
		git = new GitHubService(token);
		git.runDry(runDry);
	}

	public void grade(Grader grader, String assignment, String pattern) throws Exception {
	
		try {
			Collection<PullRequest> requests = git.getLatestPullRequests(GRADING_ORGANISATION, pattern, assignment, "closed");

			for (PullRequest request : requests) {
	
				Repository repo = request.getBase().getRepo();
				String sha      = request.getHead().getSha();
				
				logger.info("grading pull request {}#{}", repo.getName(), request.getNumber());
				
				IResult report = grade(grader, repo, sha, new RefSpec("refs/heads/" + assignment));
				
				ExtendedCommitStatus status = new ExtendedCommitStatus(report);
				status.setContext(GRADING_CONTEXT);
				
				git.addComment(request, autoComment + report.getGrade());
				git.setStatus(repo, sha, status);
			}
		
		} catch (IOException e) {
			e.printStackTrace();
		} catch (GitAPIException e) {
			e.printStackTrace();
		}
		
	}
	
	public void feedback(Grader grader, String assignment, String pattern, int late) throws Exception {

		try {
			Collection<PullRequest> requests = git.getPullRequests(GRADING_ORGANISATION, pattern, "open");
			
			for (PullRequest request : requests) {
			
				if (!assignment.equals(request.getBase().getRef())) {
//					git.addComment(request, NO_GRADER);
					continue;
				} 
				
				Repository repo = request.getBase().getRepo();
				int number      = request.getNumber();
				String sha      = request.getHead().getSha();
				
				IResult report = grade(grader, repo, sha, new RefSpec("refs/pull/" + number + "/merge"));
				
				ExtendedCommitStatus status = new ExtendedCommitStatus(report);
				status.setContext(GRADING_CONTEXT);
				
				if (late == -1) {
					git.addComment(request, autoComment + report.getFeedback());
				} else {
					
					if (report.getStatus() == Status.SUCCESS)
						status.setDescription("Successful submission without feedback.");
					
					System.out.println(report.getFeedback());
				}

				git.setStatus(repo, sha, status);

				if (report.getStatus() == Status.SUCCESS) {
					MergeStatus mstatus = git.merge(repo, number, "Merge submission");
				
					if (!mstatus.isMerged()) {
						git.addComment(request, NO_MERGE);
					} else if (late > 0) {
						git.addComment(request, autoComment + "This submission costs you " + late + " late days.");
					}
				} else {
					git.close(request);
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (GitAPIException e) {
			throw new RuntimeException(e);
		}
	}

	private IResult grade(Grader grader, Repository repo, String sha, RefSpec ref) throws Exception {
		ExtendedCommitStatus cstatus = new ExtendedCommitStatus();
		cstatus.setState("pending");
		cstatus.setDescription("The assignment is being graded.");
		cstatus.setContext(GRADING_CONTEXT);
		git.setStatus(repo, sha, cstatus);

		File dir = createTemporaryDirectory();
		Git co = git.checkout(repo, ref, dir);
		
		IResult report = grader.grade(dir);
		
		co.close();
		
		return report;
	}

	private File createTemporaryDirectory() throws IOException {
		Path tmpDirPath = Files.createTempDirectory("in4303-");
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
