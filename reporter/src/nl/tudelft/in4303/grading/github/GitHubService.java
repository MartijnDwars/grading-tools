package nl.tudelft.in4303.grading.github;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.egit.github.core.MergeStatus;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.PullRequestService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.InitCommand;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

public class GitHubService {

	protected PullRequestService pullRequestService;
	protected RepositoryService repoService;
	protected ExtendedCommitService commitService;
	protected IssueService issueService;
	protected CredentialsProvider credentialsProvider;
	private boolean dryrun;

	public GitHubService(String token) {
		GitHubClient client = new GitHubClient();
		client.setOAuth2Token(token);

		credentialsProvider = new UsernamePasswordCredentialsProvider(token, "");
		repoService = new RepositoryService(client);
		pullRequestService = new PullRequestService(client);
		commitService = new ExtendedCommitService(client);
		issueService = new IssueService(client);
		dryrun = false;
	}

	public void runDry(boolean dryrun) {
		this.dryrun = dryrun;
	}

	public void addComment(PullRequest request, String comment) throws IOException {
		if (dryrun) {
			System.out.println(comment);
		} else {
			issueService.createComment(request.getBase().getRepo(), request.getNumber(), comment);
		}
	}

	/**
	 * Retrieve all open, non-graded pull requests
	 *
	 * @param org
	 * @param pattern
	 * @param state
	 * @return
	 * @throws IOException
	 */
	protected Collection<PullRequest> getPullRequests(String org, String pattern, String state) throws IOException {
		List<Repository> orgRepositories = repoService.getOrgRepositories(org);
		List<PullRequest> requests = new ArrayList<>();

		for (Repository repo : orgRepositories) {
			if (repo.getName().matches(pattern)) {
				if (state.equals("merged")) {
					for (PullRequest closed : pullRequestService.getPullRequests(repo, "closed")) {
						if (pullRequestService.isMerged(repo, closed.getNumber())) {
							requests.add(closed);
						}
					}
				} else {
					requests.addAll(pullRequestService.getPullRequests(repo, state));
				}
			}
		}

		return requests;
	}

	public Collection<PullRequest> getPullRequests(String org, String pattern, String branch, String state) throws IOException {
		List<Repository> orgRepositories = repoService.getOrgRepositories(org);
		List<PullRequest> requests = new ArrayList<>();

		for (Repository repo : orgRepositories) {
			if (repo.getName().matches(pattern)) {
				for (PullRequest request : pullRequestService.getPullRequests(repo, state)) {
					if (branch.equals(request.getBase().getRef())) {
						requests.add(request);
					}
				}
			}
		}

		return requests;
	}

	protected Collection<PullRequest> getLatestPullRequests(String org, String pattern, String branch, String state) throws IOException {
		List<Repository> orgRepositories = repoService.getOrgRepositories(org);
		Map<String, PullRequest> requests = new Hashtable<>();

		for (Repository repo : orgRepositories) {
			String name = repo.getName();
			if (name.matches(pattern)) {
				for (PullRequest request : pullRequestService.getPullRequests(repo, state)) {
					if (branch.equals(request.getBase().getRef()) && (!requests.containsKey(name) || requests.get(name).getNumber() < request.getNumber())) {
						requests.put(name, request);
					}
				}
			}
		}

		return requests.values();
	}

	protected void setStatus(Repository repo, String sha, ExtendedCommitStatus status) throws IOException {
		if (dryrun) {
			System.out.println(status.getDescription());
		} else {
			commitService.createStatus(repo, sha, status);
		}
	}

	protected Git checkout(Repository repo, RefSpec refSpec, File tmpDir)
			throws GitAPIException, InvalidRemoteException, TransportException,
			RefAlreadyExistsException, RefNotFoundException,
			InvalidRefNameException, CheckoutConflictException {
				// git init
				Git tmpRepo = new InitCommand().setDirectory(tmpDir).call();

				// git fetch the pullRequest
				tmpRepo.fetch()
						.setCredentialsProvider(credentialsProvider)
						.setRemote(repo.getCloneUrl())
						.setRefSpecs(refSpec)
						.call();

				// git checkout the fetched head
				tmpRepo.checkout().setAllPaths(true).setStartPoint("FETCH_HEAD")
						.call();
				return tmpRepo;
			}

	public boolean hasState(PullRequest pullRequest, String context, String expected) throws IOException {
		List<CombinedCommitState> combinedStates = commitService
			.getCombinedStatus(pullRequest.getBase().getRepo(), pullRequest.getHead().getSha());

		for (CombinedCommitState state : combinedStates) {
			if (state.hasState(context, expected)) {
				return true;
			}
		}

		return false;
	}

	public boolean hasState(PullRequest pullRequest, String context) throws IOException {
		List<CombinedCommitState> combinedStates = commitService
			.getCombinedStatus(pullRequest.getBase().getRepo(), pullRequest.getHead().getSha());

		for (CombinedCommitState state : combinedStates) {
			if (state.hasState(context)) {
				return true;
			}
		}

		return false;
	}

	public MergeStatus merge(Repository repo, int number, String string) throws IOException {
		if (dryrun) {
			return null;
		} else {
			return pullRequestService.merge(repo, number, string);
		}
	}

	public boolean isMerged(Repository repo, PullRequest request) throws IOException {
		return pullRequestService.isMerged(repo, request.getNumber());
	}

	public boolean close(PullRequest request) {
		return true;
	}
}
