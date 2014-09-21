package nl.tudelft.in4303.grading.github;

import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_REPOS;
import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_STATUSES;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.egit.github.core.CommitStatus;
import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.PagedRequest;
import org.eclipse.egit.github.core.service.CommitService;

import com.google.gson.reflect.TypeToken;

public class ExtendedCommitService extends CommitService {
	private static final String SEGMENT_COMMITS = "/commits";
	private static final String SEGMENT_STATUS = "/status";

	public ExtendedCommitService(GitHubClient client) {
		super(client);
	}
	
	/**
	 * Create status for commit SHA-1
	 *
	 * @param repository
	 * @param sha
	 * @param status
	 * @return created status
	 * @throws IOException
	 */
	public ExtendedCommitStatus createStatus(IRepositoryIdProvider repository,
			String sha, ExtendedCommitStatus status) throws IOException {
		String id = getId(repository);
		if (sha == null)
			throw new IllegalArgumentException("SHA-1 cannot be null"); //$NON-NLS-1$
		if (sha.length() == 0)
			throw new IllegalArgumentException("SHA-1 cannot be empty"); //$NON-NLS-1$
		if (status == null)
			throw new IllegalArgumentException("Status cannot be null"); //$NON-NLS-1$

		Map<String, String> params = new HashMap<String, String>(4, 1);
		if (status.getState() != null)
			params.put("state", status.getState());
		if (status.getTargetUrl() != null)
			params.put("target_url", status.getTargetUrl());
		if (status.getDescription() != null)
			params.put("description", status.getDescription());
		if (status.getContext() != null)
			params.put("context", status.getContext());

		StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
		uri.append('/').append(id);
		uri.append(SEGMENT_COMMITS);
		uri.append('/').append(sha);
		uri.append(SEGMENT_STATUSES);

		return client.post(uri.toString(), params, ExtendedCommitStatus.class);
	}

	/**
	 * Get statuses for commit SHA-1
	 *
	 * @param repository
	 * @param sha
	 * @return list of statuses
	 * @throws IOException
	 */
	public List<CommitStatus> getStatuses(IRepositoryIdProvider repository,
			String sha) throws IOException {
		String id = getId(repository);
		if (sha == null)
			throw new IllegalArgumentException("SHA-1 cannot be null"); //$NON-NLS-1$
		if (sha.length() == 0)
			throw new IllegalArgumentException("SHA-1 cannot be empty"); //$NON-NLS-1$

		StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
		uri.append('/').append(id);
		uri.append(SEGMENT_COMMITS);
		uri.append('/').append(sha);
		uri.append(SEGMENT_STATUSES);
		PagedRequest<CommitStatus> request = createPagedRequest();
		request.setType(new TypeToken<List<ExtendedCommitStatus>>() {
		}.getType());
		request.setUri(uri);
		return getAll(request);
	}
	
	/**
	 * Get combined statuses for commit SHA-1
	 *
	 * @param repository
	 * @param sha
	 * @return list of statuses
	 * @throws IOException
	 */
	public List<CombinedCommitState> getCombinedStatus(IRepositoryIdProvider repository,
			String sha) throws IOException {
		String id = getId(repository);
		if (sha == null)
			throw new IllegalArgumentException("SHA-1 cannot be null"); //$NON-NLS-1$
		if (sha.length() == 0)
			throw new IllegalArgumentException("SHA-1 cannot be empty"); //$NON-NLS-1$

		StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
		uri.append('/').append(id);
		uri.append(SEGMENT_COMMITS);
		uri.append('/').append(sha);
		uri.append(SEGMENT_STATUS);
		PagedRequest<CombinedCommitState> request = createPagedRequest();
		request.setType(new TypeToken<CombinedCommitState>() {
		}.getType());
		request.setUri(uri);
		return getAll(request);
	}
}
