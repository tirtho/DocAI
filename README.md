# DocAI

## Prerequisite
- Azure Subscription
- Accepted Application to Azure OpenAI, including GPT-4
- Azure Open AI (AOAI) Instance with deployed embedding model and GPT-4 model
- Azure Cognitive Search instance

## Overview

The few code samples in [cli](code/python/cli) or [notebooks](code/python/notebooks) to demonstrate how to implement - 

1. A text classifier
2. A Retrieval augmented generation (RAG) pattern to get answers from Azure Open AI with 

- Receives user prompt,
- Calls Azure OpenAI Embedding APIs to create vector corresponding to the prompt,
- Searches indexed documents using hybrid search by Azure Cognitive Search (vector, semantic and full text search) for the given prompt and vector,
- Creates Summary of the sarched information, using Azure OpenAI GTP-4 Completion model
- Sends the augmented and engineered prompt to Azure Open AI
- Returns the completion to the calling end user
