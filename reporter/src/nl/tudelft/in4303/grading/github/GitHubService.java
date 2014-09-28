package nl.tudelft.in4303.grading.github;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

	public GitHubService(String username, String password) {
		GitHubClient client = new GitHubClient();
		client.setCredentials(username, password);
		credentialsProvider = new UsernamePasswordCredentialsProvider(username,
				password);

		repoService = new RepositoryService(client);
		pullRequestService = new PullRequestService(client);
		commitService = new ExtendedCommitService(client);
		issueService = new IssueService(client);
	}

	protected void addComment(PullRequest request, String comment) throws IOException {
		issueService.createComment(request.getBase().getRepo(), request.getNumber(), comment);
	}

	protected List<PullRequest> getPullRequests(String org, String pattern, String state) throws IOException {
		
		List<Repository> orgRepositories = repoService
				.getOrgRepositories(org);
	
		// Retrieve all open, non-graded pull requests
		List<PullRequest> requests = new ArrayList<PullRequest>();
		for (Repository repo : orgRepositories) 
			if (repo.getName().matches(pattern))
				if (state.equals("merged")) {
					for (PullRequest closed : pullRequestService.getPullRequests(repo, "closed"))
						if (pullRequestService.isMerged(repo, closed.getNumber()))
							requests.add(closed);
				} else
					requests.addAll(pullRequestService.getPullRequests(repo, state));
		
		return requests;
	}

	protected void setStatus(PullRequest request, ExtendedCommitStatus status) throws IOException {
		commitService.createStatus(request.getBase().getRepo(), request.getHead().getSha(), status);
	}

	protected Git checkout(PullRequest pullRequest, File tmpDir)
			throws GitAPIException, InvalidRemoteException, TransportException,
			RefAlreadyExistsException, RefNotFoundException,
			InvalidRefNameException, CheckoutConflictException {
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
				return tmpRepo;
			}

	public boolean hasState(PullRequest pullRequest, String context, String expected) throws IOException {
		List<CombinedCommitState> combinedStates = commitService
				.getCombinedStatus(pullRequest.getBase().getRepo(), pullRequest
						.getHead().getSha());
	
		for (CombinedCommitState state : combinedStates) {
			if (state.hasState(context, expected))
				return true;
		}
		
		return false;
	}

	public boolean hasState(PullRequest pullRequest, String context) throws IOException {
		List<CombinedCommitState> combinedStates = commitService
				.getCombinedStatus(pullRequest.getBase().getRepo(), pullRequest
						.getHead().getSha());
	
		for (CombinedCommitState state : combinedStates) {
			if (state.hasState(context))
				return true;
		}
		
		return false;
	}
	public MergeStatus merge(Repository repo, int number, String string) throws IOException {
		return pullRequestService.merge(repo, number, string);
	}

	public boolean isMerged(Repository repo, PullRequest request) throws IOException {
		return pullRequestService.isMerged(repo, request.getNumber());
	}
}