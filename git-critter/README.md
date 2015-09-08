# Git-Critter script

The git-critter script is a small node script with some convenience commands when hosting a course on github.
To install it, make sure you have `node` installed. Checkout this directory and run `npm install -g` to install it globally.

## The `push_files` command

In order to push files to student directories, open the `template` repository and make sure all changes are made.
Then, execute the following command
`git-critter push_files --organisation="TUDelft-IN4303-2015" --repository="student-(.*)" -- the_branch`
to push the branch `the_branch` to all student repositories. Note that all arguments after the double dash `--` are passed directly to `git push`, so other arguments are valid as well (e.g. `--all`).

*NOTE:* The script will gives errors when it has already been run in the directory, because the remotes for the student repositories already exist. These can be safely ignored. Make sure to check the output for other errors, however.

### Authentication

This script uses the OAuth token to get a list of the repositories. The repositories are added as remotes over SSH. Hence, *you* should have access to the student repositories. In 2015, we did this by creating a "Staff" team, adding the TA's to that team, and then adding the student repos with write access to that team.
