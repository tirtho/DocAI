# DocAI

## Prerequisite
- Azure Subscription
- Accepted Application to Azure OpenAI, including GPT-4

## Overview

The code here deploys a REST API based Azure Web Service, that -
- Receives user prompt,
- Calls Azure OpenAI Embedding APIs to create vector corresponding to the prompt,
- Searches indexed documents using hybrid search by Azure Cognitive Search (vector, semantic and full text search) for the given prompt and vector,
- Creates Summary of the sarched information, using Azure OpenAI GTP-4 Completion model
- Sends the augmented and engineered prompt to Azure Open AI
- Returns the completion to the calling end user

The code also provides a service to automate, and index new documents in Azure Cognitive Search

<b> Phase II - Save prompt, completion, user feedback in Cosmos DB and use AML Prompt Flow to improve prompt/completion accuracy </b>
