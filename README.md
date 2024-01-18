# DocAI

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

## Overview

The end to end email (with attachments) ingestion, extraction, insights, fraud/anomaly detection and Human In the Loop with UI has all the code in the [code](code) section. 

After you create and configure the prerequisite Azure services above, deploy the code for [Azure Logic App](code/logic-apps), [Azure Python Functions](code/python/functions), [Azure C# Functions](code/dotnet) and [Azure Spring Boot Web App](code/web-apps).

In your CosmosDB NoSQL API instance you need to create a database/container as DocAIDatabase/EmailExtracts.

Use the training documents below to train the Azure Document Intelligence Service for your custom Classifier and custom Extractor -

- [Sample Auto Insurance Claims](data/sample-claims-docs/training)
- [Sample Commercial Insurance Application](data/sample-commercial-insurance-docs/training)
- [Sample Worker Compensation Application](data/sample-worker-compensation-docs/training)


If you do not want to install the end to end solution but learn some of the technologies there are some great examples in the [cli](code/python/cli) or [notebooks](code/python/notebooks) section that demonstrate how to implement - 

1. A text classifier
2. A Retrieval augmented generation (RAG) pattern to get answers from Azure Open AI with 

- Receives user prompt,
- Calls Azure OpenAI Embedding APIs to create vector corresponding to the prompt,
- Searches indexed documents using hybrid search by Azure Cognitive Search (vector, semantic and full text search) for the given prompt and vector,
- Creates Summary of the sarched information, using Azure OpenAI GTP-4 Completion model
- Sends the augmented and engineered prompt to Azure Open AI
- Returns the completion to the calling end user
