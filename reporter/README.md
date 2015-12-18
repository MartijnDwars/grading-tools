# Reporter

The reporter project is used to grade submissions for IN4303.

## Build Eclipse project

For each lab we have an Eclipse project with erroneous language definitions or SPT tests.
Before we can grade student submissions, we need to build the Eclipse project.
Clone the `grading` repository and import the relevant projects (e.g. `lab1`) into Eclipse and run `build.xml`.
Use the following external tools configuration:
* **Refresh**: Select *The project containing the selected resource*
* **Build**: Deselect everything
* **Properties**: Select *Use global properties as specified in the Ant runtime preferences*
* **JRE**: Select *Run in the same JRE as the workspace*

## Installation

First clone the `grading-releng` repository and run scripts `update.sh` and `build.sh`. This will install modified versions of Spoofax and SPT that are used by the reporter.

When using an IDE, make sure these modified versions are on your classpath. For example, in IntelliJ IDEA you need to import the `reporter` project, go to *Module settings > Dependencies > Add JARs or directories..* and add
`org.metaborg.spoofax.core-1.5.0-SNAPSHOT.jar` and `org.strategoxt.imp.testing` from `grading-releng`.

The project expects a single configuration file named `gh.properties` in the root containing the GitHub access token in the following
format:

```
token=<your-token>
```

## Usage

```
Reporter local <solution> <project> [options]
Reporter remote <solution> <project> <organisation> <repository> <branch> [options]
Reporter merge <branch>

Options:
  -g --grade    Create detailed report
  -d --dryrun   No GitHub interaction (only available in 'remote' and 'merge' command)
```

Three commands are supported: `local`, `remote`, `merge`. The first two require the path to the solution and project as
the first two arguments. In the remote command the project path is taken to be relative to the root of the repository.
The `merge` command will merge all open PRs for the given branch without grading.

### Example

The following commands can be used in the labs to grade a local project:

```
lab1:	Reporter local ~/grading/lab1/grading /tmp/student-johndoe/MiniJava-tests-syntax
lab2:	Reporter local ~/grading/lab2/grading /tmp/student-johndoe/MiniJava
lab5:	Reporter local ~/grading/lab5/grading /tmp/student-johndoe/MiniJava-tests-names
lab6:	Reporter local ~/grading/lab6/grading /tmp/student-johndoe/MiniJava
lab7:	Reporter local ~/grading/lab7/grading /tmp/student-johndoe/MiniJava-tests-types
lab8:	Reporter local ~/grading/lab8/grading /tmp/student-johndoe/MiniJava
lab9:	Reporter local ~/grading/lab9/grading /tmp/student-johndoe/MiniJava
```

The following commands can be used in the labs to grade a remote project:

```
lab1:	Reporter remote ~/grading/lab1/grading MiniJava-tests-syntax TUDelft-IN4303-2015 student-johndoe assignment1
lab2:	Reporter remote ~/grading/lab2/grading MiniJava TUDelft-IN4303-2015 student-johndoe assignment2
lab5:	Reporter remote ~/grading/lab5/grading MiniJava-tests-names TUDelft-IN4303-2015 student-johndoe assignment5
lab6:	Reporter remote ~/grading/lab6/grading MiniJava TUDelft-IN4303-2015 student-johndoe assignment6
lab7:	Reporter remote ~/grading/lab7/grading MiniJava-tests-types TUDelft-IN4303-2015 student-johndoe assignment7
lab8:	Reporter remote ~/grading/lab8/grading MiniJava TUDelft-IN4303-2015 student-johndoe assignment8
lab9:	Reporter remote ~/grading/lab9/grading MiniJava TUDelft-IN4303-2015 student-johndoe assignment9
```

**IMPORTANT**: Make sure you run the reporter with the IN4303 version of `spt` on the classpath. This version included
in the `grading-releng` repo. For example, the complete command may look like:

```
java -cp "grading-releng/grading-tools/reporter/target/reporter-1.5.0-SNAPSHOT.jar:grading-releng/spt/org.strategoxt.imp.testing/" nl.tudelft.in4303.grading.Reporter remote ~/grading/lab1/grading MiniJava-tests-syntax TUDelft-IN4303-2015 student-johndoe assignment1
```

## Processes

### Final grade an assignment

1. Merge open submissions: `Reporter merge assignment7`. This will post a comment for the late days.
2. Run the grader in the cloud.

### Fix erroneous submissions

Errors in student submissions (e.g. parse errors in SPT) are quite common. Some of these need to be fixed manually in order to let the grading tool succeed. Apply those fixes in a separate branch, e.g. `assignment1-fix`, create a pull request against the original branch, merge this pull request, and run the grading tool again.
