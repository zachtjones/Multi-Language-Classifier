# Multi-Language-Classifier

This project trains an ML model on which language a sentence is based on the input date (located in /src/main/resources/data/odyssey*.txt).

Detects the following:
* English
* French
* German
* Spanish
* Swedish

These languages were chosen because they all share the Latin/Roman character set,
so there is some skill involved in knowing what language the sentence is.

"The Odyssey" was chosen as input data as it is in the public domain and it a fairly large text. 

## Setup
This project is the backend for a language classification API, and related API's.

This is written using:
* Kotlin (for the JVM)
  * requires Java 17+
* Spring Boot
* Netflix DGS (GraphQL framework for Spring)

To use in an IDE, import using gradle and set it to use java 17+

## Background: Artificial Intelligence / Machine Learning

I've learned some techniques in college but did not go very in depth. This project showcases a lot more depth.

* Data refining and pre-processing
  * I downloaded 5 different language translations of *The Odyssey* by Homer from the internet.
  * Many regex operations were used to get them into paragraph form.
* I am familiar with machine learning techniques:
  * genetic algorithms -- I use this to determine what "features" distinguish two languages
  * decision trees -- a simple way to have an algorithm "learn" how to use these features to distinguish two langauges
  * boosting techniques -- I use adaboost to improve the accuracy and reduce "overfitting" of the training data 
* I have extensive backend & cloud expertise
  * you can demo this on https://www.zach-jones.com/ - under the "Language Classifier" section
  * built using AWS and the technologies listed in the setup.

### Feature selection:
  - specific words (the, a, an, and their equivalents)
    - mutation: pick a new word, given the entire list of words, with the proportional probability to the word count
    - crossover: blends don't make sense, so just return one of the parents
  - a word ends with a sequence of characters
    - mutation: add/remove a random character, drawn from the distribution of letters
    - crossover: all sub-sequences from the two suffixes blended
  - a word starts with a sequence of characters
    - mutation & crossover is similar to word endings
  - a word contains a sequence of characters
    - mutation & crossover is similar to word endings and starts
  - letter count of one letter greater than another letter
    - mutation: one letter changes according to the distribution of letters
    - crossover: the 4 combinations of two letter combinations
  
### Multiple classification:
  - since there's many languages to decide between, I'm going to use binary classifiers in a one vs one approach.
  - requires training `K (K − 1) / 2` binary classifiers, and then taking the number of +1 votes on each classification, the one with the max is the language decided.


## Results

All of these results are based on a series of models trained based on:
* Training data ID: 1cd98469-5366-48c9-b837-438ca8ba042c
* Testing data ID: 35f4fd52-959d-4828-86d6-2193d4e22844
* Computer resources: Apple Macbook Pro 2021 w/ M1 Max

### Decision Trees


### Adaptive Boosting


trainModel(input: {
trainingDataId: "1cd98469-5366-48c9-b837-438ca8ba042c",
attributeGenerations: 100,
attributePoolSize: 200,
modelType: ADAPTIVE_BOOSTING_TREE,
ensembleSize: 20


result is "trainingAccuracyPercentage": 72.21

Binary classifier training accuracy (GERMAN vs SWEDISH): 87.52499999999999
Binary classifier training accuracy (ENGLISH vs GERMAN): 94.525
Binary classifier training accuracy (FRENCH vs SWEDISH): 99.325
Binary classifier training accuracy (FRENCH vs GERMAN): 98.75
Binary classifier training accuracy (ENGLISH vs SWEDISH): 98.925
Binary classifier training accuracy (FRENCH vs SPANISH): 96.75
Binary classifier training accuracy (SPANISH vs SWEDISH): 99.775
Binary classifier training accuracy (GERMAN vs SPANISH): 99.15
Binary classifier training accuracy (ENGLISH vs FRENCH): 92.475
Binary classifier training accuracy (ENGLISH vs SPANISH): 99.625

Some languages get more love than others, since they use a common pool of attributes.


Will be iterating on the methods used to try to improve this number

## Building the JAR

Install:
* Gradle
* Java 17+

`gradle bootJar` to build the code into a jar suitable for running as `java -jar NAME`

You can run this jar in the cloud by configuring the environment variable, `SPRING_PROFILES_ACTIVE`=`cloud`, and any other setup as you need in application-cloud.yml.

This project uses Spring Boot, Netflix DGS (for GraphQL).

## Running

1. Use Intellij or any other IDE after importing this via gradle project
2. Run in the cloud by any provider that can run a JAR (ex: aws elastic beanstalk)

## Data files

Data files are used for downloaded language data (for training/testing), stored in plain text.

Trained models are serialized and written to the data directory as well.

#### `data/*`

This is where the generated models and downloaded language data will be written to.

This is excluded from version control.

#### `/src/main/resources/data/*`

This directory will be copied to `/data/*` on application startup. 

### Cloud

The cloud will have a server with files present from the resources folder. 

Any files generated from the running application will not be persisted long term unless copied and added to the resources folder.
