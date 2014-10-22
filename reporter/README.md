# Build Eclipse project

For each lab, we have an Eclipse project with erroneous language definitions or SPT tests.
Before we can grade student submissions, we need to build the Eclipse project.
Clone the `grading` repository and import the relevant project (e.g. `lab1`) into Eclipse and run `build.xml`.

# Build grading tool

To build the grading tools, clone the `grading-releng` repository and run scripts `update.sh` and `build.sh`.

# Grade from the command-line

In the current directory, create a `gh.properties` file containing the user and password of your github account in the following format:

```
user=username
user2=password
```

Now, you can run the reporter using Java. You need to put the reporter JAR file, the lab project, and the SPT folder on the classpath, choose the main class, and pass the NetID of the student to grade as argument.
For example, with the following directory structure:

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
java -classpath "grading-releng/grading-tools/reporter/target/reporter-1.2.0-SNAPSHOT.jar:grading/lab1/:grading-releng/spt/org.strategoxt.imp.testing/" nl.tudelft.in4303.grading.Main johndoe
```

to grade the student `johndoe`.
