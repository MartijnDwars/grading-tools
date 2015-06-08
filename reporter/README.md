# Build Eclipse project

For each lab, we have an Eclipse project with erroneous language definitions or SPT tests.
Before we can grade student submissions, we need to build the Eclipse project.
Clone the `grading` repository and import the relevant project (e.g. `lab1`) into Eclipse and run `build.xml`.
Use the following external tools configuration: 
* Refresh: Select *The project containing the selected resource*
* Build: Deselect everything
* Properties: Select *Use global properties as specified in the Ant runtime preferences*
* JRE: Select *Run in the same JRE as the workspace*

# Build grading tool

To build the grading tools, clone the `grading-releng` repository and run scripts `update.sh` and `build.sh`. This will install the correct dependencies. I imported the `reporter` project in Eclipse and changed the 'Module settings' to overwrite spoofax-core.jar and org.strategoxt.imp.testing (both with the equivalent version from grading-releng).

# Usage

In the current directory, create a `gh.properties` file containing the GitHub access token in the following format:

```
token=<your-token>
```

Now, you can run the reporter using Java. You need to put the reporter JAR file and the SPT folder on the classpath, choose the main class, and pass the NetID of the student, the path to the grading project and the test dir to grade (usually `MiniJava-tests(-names|-types)?`) as argument. For example, with the following directory structure:

```
.
|-- gh.properties
|-- grading
|   |-- lab1
|-- grading-releng
|   |-- grading-tools
|   |-- spt
```

run the following command:

```
java -cp "grading-releng/grading-tools/reporter/target/reporter-1.5.0-SNAPSHOT.jar:grading-releng/spt/org.strategoxt.imp.testing/" nl.tudelft.in4303.grading.Main johndoe grading/lab7 MiniJava-tests-types
```

to grade the student `johndoe`.

## Options

The reporter accepts the following options:

```
-d --dryrun	    Do not post anything to GitHub
-g --grade      Post detailed grade (by default the student only receives feedback)
-l --late       Specify a positive number of late days. A comment will be placed to notify the student.
-b --branch     Branch on GitHub to grade
-p --project    Path to a local project to grade

Use either --branch or --project.
```

# Fix erroneous submissions

Errors in student submissions (e.g. parse errors in SPT) are quite common. 
Some of these need to be fixed manually in order to let the grading tool succeed.
Apply those fixes in a separate branch, e.g. `assignment1-fix`, 
create a pull request against the original branch,
merge this pull request, 
and run the grading tool again.
