package nl.tudelft.in4303.githubfetcher;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

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
import org.metaborg.spt.listener.grading.GroupRunner;

public class GitHubFetcher {
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
			orgRepositories.parallelStream()
					.filter(GitHubFetcher::isStudentRepository)
					.flatMap(this::getOpenPullRequests)
					.filter(this::pullRequestIsNotGraded)
					.map(this::gradePullRequest)
					.forEach(this::uploadReportAndStatus);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static boolean isStudentRepository(Repository repo) {
		return repo.getName().matches("^student-(.*)$");
	}

	private Stream<PullRequest> getOpenPullRequests(Repository repo) {
		try {
			return pullRequestService.getPullRequests(repo, "open").stream();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private boolean hasGradingContext(ExtendedCommitStatus status) {
		return "grading/in4303".equals(status.getContext());
	}

	private boolean pullRequestIsNotGraded(PullRequest pullRequest) {
		try {
			Optional<ExtendedCommitStatus> gradingStatus = commitService
					.getCombinedStatus(pullRequest.getBase().getRepo(),
							pullRequest.getHead().getSha()).get(0)
					.getStatuses().stream().filter(this::hasGradingContext)
					.findFirst();

			if (gradingStatus.isPresent()) {
				String state = gradingStatus.get().getState();
				return "pending".equals(state);
			} else {
				return true;
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private GradeReport gradePullRequest(PullRequest pullRequest, IReporter reporter) {
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
			GradeReport report = reporter.getReport();
			
			// close the repo
			tmpRepo.close();

			return report;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (GitAPIException e) {
			throw new RuntimeException(e);
		}
	}

	private void uploadReportAndStatus(GradeReport report) {
		PullRequest pullRequest = report.getPullRequest();
		ExtendedCommitStatus status = new ExtendedCommitStatus();
		
		switch(report.getStatus()) {
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
		status.setContext("grading/in4303");
		
		try {
			commitService.createStatus(pullRequest.getBase().getRepo(), pullRequest.getHead().getSha(), status);
			
			String comment = "*Auto-generated comment*" + System.lineSeparator() + System.lineSeparator() + report.getReport();
			issueService.createComment(pullRequest.getBase().getRepo(), pullRequest.getNumber(), comment);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
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
