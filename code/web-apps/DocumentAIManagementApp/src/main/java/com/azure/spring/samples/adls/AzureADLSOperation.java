package com.azure.spring.samples.adls;

import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.azure.core.http.rest.Response;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.file.datalake.DataLakeDirectoryClient;
import com.azure.storage.file.datalake.DataLakeFileSystemClient;
import com.azure.storage.file.datalake.DataLakeServiceClient;
import com.azure.storage.file.datalake.DataLakeServiceClientBuilder;

public class AzureADLSOperation {
	public static Logger logger = LoggerFactory.getLogger(AzureADLSOperation.class);

	private DefaultAzureCredential adlsCredential;
	private String sasToken;
	
	public AzureADLSOperation(String sasToken) throws MalformedURLException {
		super();
		if (sasToken != null) {
			this.sasToken = sasToken;
			this.adlsCredential = null;
		} else {
			this.sasToken = null;
			// Use this for MI
			this.adlsCredential = new DefaultAzureCredentialBuilder().build();
		}
		
	}

	public int deleteBlob(String urlToRemove) throws Exception {
		DataLakeServiceClient adlsClient;
		if (sasToken != null) {
			adlsClient = new DataLakeServiceClientBuilder()
		            .endpoint(AzureADLSOperation.blob2AdlsEndpoint(urlToRemove))
		            .sasToken(sasToken)
		            .buildClient();
		} else {
			adlsClient = new DataLakeServiceClientBuilder()
		            .endpoint(AzureADLSOperation.blob2AdlsEndpoint(urlToRemove))
		            .credential(this.adlsCredential)
		            .buildClient();
		}

		DataLakeFileSystemClient fsClient = adlsClient.getFileSystemClient(AzureADLSOperation.blob2ADLSFilesystemName(urlToRemove));

		DataLakeDirectoryClient dClient = fsClient.getDirectoryClient(AzureADLSOperation.urlToDirectoryPath(urlToRemove));

	    Response<Void> response = dClient.deleteRecursivelyWithResponse(null, null, null);
		return response.getStatusCode();
	}

	// For https://blobstorename.blob.core.windows.net/filesystem/path
	// this returns /path
	private static String urlToDirectoryPath(String url) throws MalformedURLException {
		URL theUrl = new URL(url);
		String path = theUrl.getPath();
		String[] paths = path.split("/");
		paths[0] = ""; paths[1] = "";
		String pathOnly = StringUtils.join(paths, "/").replaceFirst("//", "");
		return pathOnly;
	}
	// For https://blobstorename.blob.core.windows.net/filesystem/path
	// this returns filesystem
	private static String blob2ADLSFilesystemName(String url) throws MalformedURLException {
		URL theUrl = new URL(url);
		String path = theUrl.getPath();
		String[] paths = path.split("/");
		return paths[1];
	}
	// For https://blobstorename.blob.core.windows.net/filesystem/path
	// this returns blobstorename.dfs.core.windows.net
	public static String blob2AdlsEndpoint(String blobUrl) throws MalformedURLException {
		String adlsUrl = StringUtils.replace(blobUrl, ".blob.core.windows.net", ".dfs.core.windows.net");
		URL url = new URL(adlsUrl);		
		return url.getProtocol() + "://" + url.getAuthority();
	}
}
