# Grading from the command-line

In the current directory, create a gh.properties file containing the user and password of your github account in the following format:

```
user=username
user2=password
```

Now we can run the reporter using Java. You need to put the reporter JAR file, lab languages, and the SPT folder on the classpath, choose the main class, and pass the student to grade as argument.
For example, with the following directory structure:

```
.
|-- gh.properties
|-- grading
|   |-- lab5
|-- grading-releng
|   |-- grading-tools
|   |-- spt
```

run the following command:

```
java -classpath "grading-releng/grading-tools/reporter/target/reporter-1.2.0-SNAPSHOT.jar:grading/lab5/:grading-releng/spt/org.strategoxt.imp.testing/" nl.tudelft.in4303.grading.Main johndoe
```

to grade the student johndoe.
