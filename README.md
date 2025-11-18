markdown
## Running Locally Without JPA

To run this project locally without connecting to a database, you need to disable the JPA dependency.  
**Steps:**
1. Open the `pom.xml` file.
2. Comment out the following dependency:
   ```xml
   <!--
   <dependency>
     <groupId>org.springframework.boot</groupId>
     <artifactId>spring-boot-starter-data-jpa</artifactId>
   </dependency>
   -->
