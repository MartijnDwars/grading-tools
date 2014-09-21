package nl.tudelft.in4303.githubfetcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

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

public class GitHubFetcher {
	private static final String GRADING_CONTEXT = "grading/in4303";

	private PullRequestService pullRequestService;
	private RepositoryService repoService;
	private ExtendedCommitService commitService;
	private IssueService issueService;
	private CredentialsProvider credentialsProvider;

	public GitHubFetcher(String username, String password) {
		GitHubClient client = new GitHubClient();
		client.setCredentials(username, password);
		credentialsProvider = new UsernamePasswordCredentialsProvider(username,
				password);

		repoService = new RepositoryService(client);
		pullRequestService = new PullRequestService(client);
		commitService = new ExtendedCommitService(client);
		issueService = new IssueService(client);
	}

	public void run() {
		try {
			List<Repository> orgRepositories = repoService
					.getOrgRepositories("TUDelft-IN4303");

			// Retrieve all open pull requests
			List<PullRequest> openPullRequests = new ArrayList<PullRequest>();
			for (Repository repo : orgRepositories) {
				if (isStudentRepository(repo)) {
					openPullRequests.addAll(getOpenPullRequests(repo));
				}
			}

			// Grade all the pull requests
			List<GradeReport> gradeReports = new ArrayList<GradeReport>();
			for (PullRequest pullRequest : openPullRequests) {
				if (!pullRequestIsGraded(pullRequest)
						&& "assignment1".equals(pullRequest.getBase().getRef())) {
					if (pullRequest.isMergeable()) {
						setStatusPending(pullRequest);
						gradeReports.add(gradePullRequest(pullRequest));
					} else {
						String report = "**Auto-generated comment**"
								+ System.lineSeparator()
								+ System.lineSeparator()
								+ "The commits are not mergeable with the base, please merge manually and try again.";
						GradeReport notMergeableReport = new GradeReport(
								pullRequest, GradeReport.Status.ERROR, report);
						gradeReports.add(notMergeableReport);
					}
				}
			}

			for (GradeReport report : gradeReports) {
				uploadReportAndStatus(report);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static boolean isStudentRepository(Repository repo) {
		return repo.getName().matches("^student-(.*)$");
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

	private GradeReport gradePullRequest(PullRequest pullRequest) {
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
			Process lsProcess = Runtime.getRuntime().exec("ls",
					new String[] {}, tmpDir);
			BufferedReader output = new BufferedReader(new InputStreamReader(
					lsProcess.getInputStream()));
			String line;
			String report = "";
			while ((line = output.readLine()) != null) {
				report += line + System.lineSeparator();
			}
			try {
				lsProcess.waitFor();
			} catch (InterruptedException e) {
			}
			// end test

			// close the repo
			tmpRepo.close();

			return new GradeReport(pullRequest, GradeReport.Status.SUCCESS,
					report);
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

	private void uploadReportAndStatus(GradeReport report) throws IOException {
		PullRequest pullRequest = report.getPullRequest();
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
}
