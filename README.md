# Document AI - Ingestion, Extraction, Post Processing, Corrections, Anomaly & Fraud Detection

Ingestion of documents coming from customers, internal functional representatives and other actors mostly over emails is an Enterprise challenge. The volume and diversity of this inflow of documents is staggerring. Upon ingestion, providing a framework for post processing, data validation, correction and quality improvement is the next hurdle. This also demands automation along with a triggered 'human in the loop' component, for faster yet safe post processing. You also need tools to detect anomalies or fraud in certain scenarios. This repository provides a Solution Accelerator using Azure AI Services including Open AI, Vision APIs, Document Intelligence, Logic App and a few services to address this challenge.

![The End to End Solution][End2EndSolution]

## Architecture

The end to end email (with attachments) ingestion, extraction, insights, fraud/anomaly detection and Human In the Loop with UI has all the code in the [code](code) section.

![The End State Architecture][DocAIArchitecture]

## Prerequisite
- Azure Subscription
- Accepted Application to Azure OpenAI, including GPT-4, and GPT-4 VISION API
- Azure Open AI (AOAI) Instance with deployed embedding model and GPT-4 models
- Azure Document Intelligence Service (formerly known as Azure Form Recognizer)
- Azure AI Search instance (formerly known as Azure Cognitive Search)
- Azure Logic App
- Azure Functions
- Azure CosmosDB NoSQL
- Azure Spring Boot Web App
- Azure Machine Learning Service
- Azure Blob Store
- Java 17
- Python 3.12.0

## Deployment

After you create and configure the prerequisite Azure services listed below, deploy the code for [Azure Logic App](code/logic-apps), [Azure Python Functions](code/python/functions), [Azure C# Functions](code/dotnet) and [Azure Spring Boot Web App](code/web-apps).

In your CosmosDB NoSQL API instance you need to create a database/container as DocAIDatabase/EmailExtracts.

Use the training documents below to train the Azure Document Intelligence Service for your custom Classifier and custom Extractor -

- [Sample Auto Insurance Claims](data/sample-auto-insurance-claims-docs/training)
- [Sample Commercial Insurance Application](data/sample-commercial-insurance-docs/training)
- [Sample Worker Compensation Application](data/sample-worker-compensation-docs/training)


If you do not want to install the end to end solution but learn some of the technologies there are some great examples in the [cli](code/python/cli) or [notebooks](code/python/notebooks) section that demonstrate how to implement - 

1. A text classifier using Azure AI Search and Azure Open AI
2. A Retrieval Augmented Generation (RAG) pattern with Azure Open AI with Azure AI Search leveraging
   - Semantic Hybrid Search
   - Cross-Field Vector Search
   - Hybrid Search
   - Pure Vector Search with a filter
4. Use Azure Document Intelligence Service to classify and extract Documents  


## End to End Document AI Solution UI Screenshots

![Review & Detect Data Inconsistencies][Review Page]

![Detect Anomaly & Fraud Page][Anomaly Page]

[Review Page]: <https://github.com/tirtho/DocAI/blob/main/images/detect-fix-ui.jpg>
[Anomaly Page]: <https://github.com/tirtho/DocAI/blob/main/images/detect-anomaly-ui.jpg>
[End2EndSolution]: <https://github.com/tirtho/DocAI/blob/main/images/End2EndSolution.jpg>
[DocAIArchitecture]: <https://github.com/tirtho/DocAI/blob/main/images/DocAIArchitecture.jpg>
