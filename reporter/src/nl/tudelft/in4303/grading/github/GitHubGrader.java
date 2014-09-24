package nl.tudelft.in4303.grading.github;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import nl.tudelft.in4303.grading.IResult;
import nl.tudelft.in4303.grading.IGrader;

import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.PullRequestService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.InitCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.util.FileUtils;

public class GitHubGrader {

	private static final String GRADING_CONTEXT = "grading/in4303";

	private PullRequestService pullRequestService;
	private RepositoryService repoService;
	private ExtendedCommitService commitService;
	private IssueService issueService;
	private CredentialsProvider credentialsProvider;
	
	private HashMap<String, IGrader> graders;

	public GitHubGrader(String username, String password) {
		GitHubClient client = new GitHubClient();
		client.setCredentials(username, password);
		credentialsProvider = new UsernamePasswordCredentialsProvider(username,
				password);

		repoService = new RepositoryService(client);
		pullRequestService = new PullRequestService(client);
		commitService = new ExtendedCommitService(client);
		issueService = new IssueService(client);
		
		this.graders = new HashMap<String, IGrader>();
	}
	
	public void registerRunner(String exercise, IGrader runner) {
		this.graders.put(exercise, runner);
	}

	public void check(String pattern) {
		run(true, pattern);
	}
	
	public void grade(String pattern) {
		run(false, pattern);
	}
	
	private void run(boolean checkOnly, String pattern) {
		try {
			List<Repository> orgRepositories = repoService
					.getOrgRepositories("TUDelft-IN4303");

			// Retrieve all open pull requests
			List<PullRequest> openPullRequests = new ArrayList<PullRequest>();
			for (Repository repo : orgRepositories) {
				if (isStudentRepository(repo, pattern)) {
					openPullRequests.addAll(getOpenPullRequests(repo));
				}
			}

			// Grade all the pull requests
			for (PullRequest pullRequest : openPullRequests) {
				if (!pullRequestIsGraded(pullRequest)) {
					IGrader grader = this.graders.get(pullRequest.getBase().getRef());
					if (grader != null) {
						
						if (!checkOnly)
							setStatusPending(pullRequest);
						
						System.out.println(String.format("Started grading pull request %s of user %s", pullRequest.getTitle(), pullRequest.getUser().getLogin()));
						IResult report = gradePullRequest(pullRequest, grader, checkOnly);
						
						System.out.println(String.format("Uploading report to pull-request %s of user %s (%s)", pullRequest.getTitle(), pullRequest.getUser().getLogin(), report.getStatus().toString()));
						if (!checkOnly)
							uploadReportAndStatus(pullRequest, report);
					} else {
						uploadReportAndStatus(pullRequest, new NoGraderResult());
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static boolean isStudentRepository(Repository repo, String pattern) {
		return repo.getName().matches(pattern);
	}

	private List<PullRequest> getOpenPullRequests(Repository repo)
			throws IOException {
		return pullRequestService.getPullRequests(repo, "open");
	}

	private boolean pullRequestIsGraded(PullRequest pullRequest)
			throws IOException {
		List<CombinedCommitState> combinedStates = commitService
				.getCombinedStatus(pullRequest.getBase().getRepo(), pullRequest
						.getHead().getSha());

		ExtendedCommitStatus gradingStatus = null;
		for (CombinedCommitState state : combinedStates) {
			for (ExtendedCommitStatus status : state.getStatuses()) {
				if (GRADING_CONTEXT.equals(status.getContext())) {
					gradingStatus = status;
				}
			}
		}

		if (gradingStatus == null) {
			return false;
		} else {
			return !"pending".equals(gradingStatus.getState());
		}
	}

	private IResult gradePullRequest(PullRequest pullRequest, IGrader grader, boolean checkOnly) {
		try {
			File tmpDir = createTemporaryDirectory();

			// git init
			Git tmpRepo = new InitCommand().setDirectory(tmpDir).call();

			// git fetch the pullRequest
			tmpRepo.fetch()
					.setCredentialsProvider(credentialsProvider)
					.setRemote(pullRequest.getBase().getRepo().getCloneUrl())
					.setRefSpecs(
							new RefSpec("refs/pull/" + pullRequest.getNumber()
									+ "/merge")).call();

			// git checkout the fetched head
			tmpRepo.checkout().setAllPaths(true).setStartPoint("FETCH_HEAD")
					.call();

			// execute a test program to get a report back
			IResult report;
			if (checkOnly) {
				report = grader.check(tmpDir);
			} else {
				report = grader.grade(tmpDir);
			}
			// close the repo
			tmpRepo.close();
			
			return report;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (GitAPIException e) {
			throw new RuntimeException(e);
		}
	}

	private void setStatusPending(PullRequest pullRequest) throws IOException {
		ExtendedCommitStatus status = new ExtendedCommitStatus();
		status.setState("pending");
		status.setDescription("The assignment is being graded.");
		status.setContext(GRADING_CONTEXT);
		commitService.createStatus(pullRequest.getBase().getRepo(), pullRequest
				.getHead().getSha(), status);
	}

	private void uploadReportAndStatus(PullRequest pullRequest, IResult report) throws IOException {
		ExtendedCommitStatus status = new ExtendedCommitStatus();

		switch (report.getStatus()) {
		case SUCCESS:
			status.setState("success");
			status.setDescription("The assignment was graded with a PASS.");
			break;
		case FAILURE:
			status.setState("failure");
			status.setDescription("The assignment was graded with a FAIL.");
			break;
		case ERROR:
		default:
			status.setState("error");
			status.setDescription("An error occurred while grading the assignment.");
			break;
		}
		status.setContext(GRADING_CONTEXT);

		commitService.createStatus(pullRequest.getBase().getRepo(), pullRequest
				.getHead().getSha(), status);

		String comment = "*Auto-generated comment*" + System.lineSeparator()
				+ System.lineSeparator() + report.getReport();
		issueService.createComment(pullRequest.getBase().getRepo(),
				pullRequest.getNumber(), comment);
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
	
	private final class NoGraderResult implements IResult {
		@Override
		public Status getStatus() {
			return Status.ERROR;
		}
		
		@Override
		public String getReport() {
			return "You need to file pull requests against the branch of the assignment you want to receive feedback on. Otherwise, the grading tool does not pick it up.";
		}

	}

}
