package nl.tudelft.in4303.githubfetcher;

public class Main {
	public static void main(String[] args) {
		GitHubFetcher fetcher = new GitHubFetcher("username", "password");
		fetcher.run();
	}
}
