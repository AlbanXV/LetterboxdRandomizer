# Letterboxd Randomizer
A program to add users' watchlist from Letterboxd and add all the movies into a list
and pick one random movie from the list. Instead of manually scrolling through your 
watchlist or others' watchlist and picking a random movie to watch, let the program 
do the job for you.

## Installation
The requirements for this project is: [Apache Maven](https://maven.apache.org/) (This project uses maven build) and ReactJS

## Usage

There are two ways you can run this program. In fact, there are two programs.
The first one is CLI based (Terminal based), the other is Sprint Boot based.

### Alternative 1 (CLI)
To run the code, cd into the directory where `LetterboxdrandomizerCLI.java` is, run:
```
javac LetterboxdrandomizerCLI.java
```
then compile it:
```
java LetterboxdrandomizerCLI
```

or use any preferred IDE of yours to run it directly if possible.

### Alternative 2 (Spring Boot)
To run the backend program, cd into the directory where `LetterboxdRandomizerApplication` is located, then run:
```
mvn spring-boot:run
```
or use any preferred IDE of yours to run it directly if possible.

To run the frontend, cd into the frontend directory and run:
```
npm start
```
