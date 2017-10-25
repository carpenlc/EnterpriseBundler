# EnterpriseBundler
Repository containing the source code associated with an application designed to read/write product data that resides on any file system that has a Java NIO2 file system implementation.  It was specifically designed for use with a local file system and an AWS S3 file system. 

## Pre-requisites
* Java (1.8 or higher) 
* git (v1.7 or higher)
* Maven (v3.3.8 or higher)
## Includes
* Modified version of Apache Commons Compress v1.14
  * Modified to change all file access to utilize the Java 7 NIO 2 stream libraries.  (Fork not yet uploaded to GIT)
  * Modified JAR file is at ~/src/main/resources/commons-compress-1.14.jar
* Modified version of Amazon-S3-Filesystem-NIO2 v1.5.3
  * Modified to handle authentication via IAM roles.  
  * Modified JAR file is at ~/src/main/resources/s3fs-1.5.3.jar
  * Fork of original project containing modified source is available at:  https://github.com/carpenlc/Amazon-S3-FileSystem-NIO2.git

## Download the Source and Build the EAR File
* Download source
```
# cd /var/local/src
# git clone https://github.com/carpenlc/EnterpriseBundler.git
```
* Execute the Maven targets to build the output EAR
```
# cd /var/local/src/EnterpriseBundler/parent
# mvn clean package 
```
* The deployable EAR file will reside at the following location
```
/var/local/src/EnterpriseBundler/parent/Bundler/target/bundler.ear
```
## Customizations
The Hibernate/JPA persistence.xml should be modified to identify the container-managed datasource.  The persistence.xml can be found at the following location:
```
/var/local/src/EnterpriseBundler/parent/BundlerCommon/src/main/resources/META-INF/persistence.xml
```
This application properties file containing the filesystem and AWS-related settings can be found at:
```
/var/local/src/EnterpriseBundler/parent/Bundler/src/main/application/lib/config.jar/bundler.properties
```

