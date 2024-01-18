# Spring App

This Spring app is a Java application
built using [Spring Boot](https://spring.io/projects/spring-boot), 
[Spring Data for 
Cosmos DB](https://docs.microsoft.com/en-us/java/azure/spring-framework/configure-spring-boot-starter-java-app-with-cosmos-db?view=azure-java-stable) and 
[Azure Cosmos DB](https://docs.microsoft.com/en-us/azure/cosmos-db/sql-api-introduction). 

## Requirements

| [Azure CLI](http://docs.microsoft.com/cli/azure/overview) | [Java 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html) | [Maven 3](http://maven.apache.org/) | [Git](https://github.com/) |

## Create Azure Cosmos DB

Create Azure Cosmos DB 
using [Azure CLI 2.0](https://docs.microsoft.com/en-us/cli/azure/install-azure-cli?view=azure-cli-latest) or from the Azure Portal

Create the database/container DocAIDatabase/EmailExtracts

## Running Spring App locally

### STEP 1 - Checkout Spring app

```bash
git clone https://github.com/tirtho/DocAI.git
cd code\web-apps\DocumentAIManagementApp
```  
    
### STEP 2 - Configure the app

Checkout the [pom file](pom.xml) for the list of environment variables under the "<appSettings>" section and set those environment variables in your OS 

### STEP 3 - Run Spring App locally

```bash
mvn package spring-boot:run
```
You can access Spring App here: [http://localhost:8080/](http://localhost:8080/).

### STEP 4 (optional) - Debug Spring App locally with Eclipse IDE

Make sure in Eclipse you have setup a Debug Configuration for "Remote Java Application" for the current DocumentAIManagementApp project, with port 9090 and Standard(Socket Attach) as Connection Type.
```bash
copy pom.xml.debug pom.xml
mvn package spring-boot:run
```
Now when you see the commandline window says it is waiting on port 9090, start Eclipse debugging with the above Debug Configuration.
You can access Spring App here: [http://localhost:8080/](http://localhost:8080/).


## Running Spring App in Azure App Service in Linux

Run the following

```bash
mvn azure-webapp:deploy
```
Details in https://docs.microsoft.com/en-us/azure/app-service/tutorial-java-spring-cosmosdb#deploy-to-app-service-on-linux
 

## Clean up

You can delete Azure resources that you created deleting 
the Azure Resource Group:

```bash
az group delete -y --no-wait -n <your-resource-group-name>
```

## Contributing

This project welcomes contributions and suggestions.  Most contributions require you to agree to a
Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us
the rights to use your contribution. For details, visit https://cla.microsoft.com.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide
a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions
provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/).
For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or
contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.

## Useful link
- [Azure Spring Boot Starters](https://github.com/Microsoft/azure-spring-boot)
- [Azure for Java Developers](https://docs.microsoft.com/en-us/java/azure/)
