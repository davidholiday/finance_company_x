# sample java app that polls directory for currency exchange data

## what is this? 
This is a Java application written using [Apache Camel](https://camel.apache.org/manual/latest/faq/what-is-camel.html) that polls a given directory for the most up to date currency exchange data. 

It assumes a company, __finance company X__, is updating a given directory on the filesystem with two types of data files. The first, `buildID.txt` acts as a pointer. There will only ever be one `buildID.txt` file that gets replaced by __X__ when a new data file is present. The second file type is a data file that contains timestamped currency exchange rates encoded as JSON.  

When the app is running, it will poll a given directory at a given frequency (all determined via app properties). When the app detects a change in the buildID, it will load whatever currency exchange data that buildID points to into a bean, replacing whatever data was previously there.  

## how to I make it do stuff? 

### prerequisites
* Java 11
* maven 3

[sdk man](https://sdkman.io/) is a great way to install these if you don't already have them 

### setup 
* build the project with maven using `$ mvn clean install` This will compile everything and run tests.
* run helper script `mock.sh`. this will copy mock files into `/tmp/exchange`, the directory the app is currently set to poll for updates.  

### run the app
The app can be run from the project root directory with the following command:
```shell
$ mvn camel:run
```

### how do I?

##### change the frequency by which the app polls the data directory?
In `src/main/resources/application.properties` you will find a property called `route_from_period`. That number, in milliseconds, is what determines the polling period.  

##### change the directory the app will poll?
In `src/main/resources/application.properties` you will find a property called `data_directory`. That determines the directory the app polls. 

