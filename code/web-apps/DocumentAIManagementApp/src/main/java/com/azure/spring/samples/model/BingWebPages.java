package com.azure.spring.samples.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class BingWebPages{
	    private HashMap<String, String> relevantHeaders;
	    private String jsonResponse;
	    
	    public BingWebPages(HashMap<String, String> headers, String json) {
	        relevantHeaders = headers;
	        jsonResponse = json;
	    }
	    
	    public List<SearchResult> getSearchResults() {
	        JsonParser parser = new JsonParser();
	        JsonObject json = parser.parse(jsonResponse).getAsJsonObject();
	        JsonArray jsonValueArray = null;
	    	try {
	    		jsonValueArray = json.get("webPages").getAsJsonObject().getAsJsonArray("value");
	    	} catch (Exception e) {
	    		return null;
	    	}
	    	Iterator<JsonElement> it = jsonValueArray.iterator();
	    	List<SearchResult> results = null;
	    	while (it.hasNext()) {
	   		  JsonElement elem = it.next();
	   		  if (elem == null) {
	   			  continue;
	   		  }
	   		  String snippetStr = null;
	   		  String urlStr = null;
	   		  String urlName = null;
	   		  try {
	   			snippetStr = elem.getAsJsonObject().get("snippet").getAsString();
	   		  } catch (Exception e) {
	   			  // nothing to do
	   		  }
	   		  try {
	   			urlStr = elem.getAsJsonObject().get("url").getAsString();
	   		  } catch (Exception e) {
	   			  // nothing to do
	   		  }
	   		  try {
	   			urlName = elem.getAsJsonObject().get("name").getAsString();
	   		  } catch (Exception e) {
	   			  // nothing to do
	   		  }
	   		  if (snippetStr == null && urlStr == null && urlName == null) {
	   			  // no data found really so skip
	   			  continue;
	   		  }
	   		  if (results == null) {
	   			results = new ArrayList<SearchResult>();
	   		  }
	   		  SearchResult sr = new SearchResult(
	   				  					SearchResult.SEARCH_TYPE_BING, 
	   				  					snippetStr, 
	   				  					UUID.randomUUID().toString(), 
	   				  					urlStr, 
	   				  					urlName, 
	   				  					null,
	   				  					null);
	   		  results.add(sr);
	    	}
	        return results;
	    }

		public HashMap<String, String> getRelevantHeaders() {
			return relevantHeaders;
		}

		public void setRelevantHeaders(HashMap<String, String> relevantHeaders) {
			this.relevantHeaders = relevantHeaders;
		}

		public String getJsonResponse() {
			return jsonResponse;
		}

		public void setJsonResponse(String jsonResponse) {
			this.jsonResponse = jsonResponse;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((jsonResponse == null) ? 0 : jsonResponse.hashCode());
			result = prime * result + ((relevantHeaders == null) ? 0 : relevantHeaders.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			BingWebPages other = (BingWebPages) obj;
			if (jsonResponse == null) {
				if (other.jsonResponse != null)
					return false;
			} else if (!jsonResponse.equals(other.jsonResponse))
				return false;
			if (relevantHeaders == null) {
				if (other.relevantHeaders != null)
					return false;
			} else if (!relevantHeaders.equals(other.relevantHeaders))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "BingWebPages [relevantHeaders=" + relevantHeaders + ", jsonResponse=" + jsonResponse + "]";
		}
	    
}
