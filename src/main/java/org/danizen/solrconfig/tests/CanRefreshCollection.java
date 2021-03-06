package org.danizen.solrconfig.tests;

import java.util.List;
import java.io.IOException;

import static org.junit.Assert.*;
import static org.junit.Assume.*;
import static org.hamcrest.CoreMatchers.*;

import java.nio.file.Files;

import org.junit.Before;
import org.junit.Test;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.CollectionAdminRequest;
import org.apache.solr.client.solrj.response.CollectionAdminResponse;
import org.apache.solr.common.util.NamedList;
import org.apache.commons.lang3.RandomStringUtils;

import org.danizen.solrconfig.SolrConfig;

public class CanRefreshCollection {

  private SolrConfig config = SolrConfig.getInstance();

  @Before
  public void setUp() throws Exception {
    assumeTrue(Files.exists(config.getPath()));
    assumeTrue(Files.exists(config.getSolrConfigPath()));
    assumeTrue(Files.exists(config.getSchemaPath()));
    assumeFalse(config.getReloadCollection());
  }

  public String newCollectionName(SolrClient client) throws IOException, SolrServerException  {
    NamedList<Object> response = client.request(new CollectionAdminRequest.List());
    System.out.println(config.formatResponse(response));
    NamedList<Object> header = (NamedList<Object>) response.get("responseHeader");
    assertThat((Integer)header.get("status"), is(equalTo(0)));
    List<String> collections = (List<String>) response.get("collections");
    
    String newName = null;
    boolean matches = true;
    
    while (matches) {
      newName = RandomStringUtils.randomAlphabetic(8);
      matches = false;
      for (String collectionName : collections) {
        if (newName.equalsIgnoreCase(collectionName)) {
          matches = true;
          break;
        }
      }
    }
    return newName;
  }
  
  public boolean collectionNameExists(SolrClient client, String collectionName) 
      throws IOException, SolrServerException  
  {
    NamedList<Object> response = client.request(new CollectionAdminRequest.List());
    System.out.println(config.formatResponse(response));
    NamedList<Object> header = (NamedList<Object>) response.get("responseHeader");
    assertThat((Integer)header.get("status"), is(equalTo(0)));
    List<String> collections = (List<String>) response.get("collections");
    
    for (String existingCollectionName : collections) {
      if (collectionName.equals(existingCollectionName)) {
        return true;
      }
    }
    return false;
  }

  
  @Test
  public void test() throws IOException, SolrServerException {
    boolean reload = false;
    SolrClient client = config.getSolrClient();
    String collectionName = config.getCollectionName();
    if (collectionName == null) {
      collectionName = newCollectionName(client);
      config.setCollectionName(collectionName);
    } else { 
      reload = collectionNameExists(client, collectionName);
    }
    
    CollectionAdminResponse response = new CollectionAdminResponse();
    
    if (reload) { 
      CollectionAdminRequest.Reload request = new CollectionAdminRequest.Reload();
      request.setCollectionName(collectionName);
      response.setResponse(client.request(request));
    } else {
      CollectionAdminRequest.Create request = new CollectionAdminRequest.Create();
      request.setConfigName(config.getConfigName());
      request.setCollectionName(collectionName);
      request.setNumShards(1);
      request.setReplicationFactor(1);      
      response.setResponse(client.request(request));
    }
    assertTrue(response.isSuccess());
  }
}
