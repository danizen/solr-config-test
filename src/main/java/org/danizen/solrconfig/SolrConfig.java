package org.danizen.solrconfig;

import java.nio.file.Paths;
import java.util.Properties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SolrConfig {
  
  // constructor is private so no one else can create it
  private SolrConfig() {
  }
  
  // there's a single instance of this class
  private static SolrConfig instance = new SolrConfig();
  
  // and a public method allows access to this method 
  public static SolrConfig getInstance() {
    return instance;
  }
  
  // There is an internal method that specifies the valid methods for the test 
  public enum Method { CLOUD, EMBEDDED };
  
  // the attributes are more like a struct than a POJO
  private Method method = Method.EMBEDDED;
  private Path path = Paths.get(".");
  private String zkhost = null;
  private String zkroot = null;
  private Path xmloutpath = null;
  
  public Method getMethod() {
    return method;
  }

  public void setMethod(Method method) {
    this.method = method;
  }
  
  public void setMethod(String method) {
    this.method = SolrConfig.Method.valueOf(method.toUpperCase());
  }

  public Path getPath() {
    return path;
  }

  public void setPath(Path path) {
    this.path = path;
  }
  
  public void setPath(String path) {
    this.path = Paths.get(path);
  }

  public String getZkHost() {
    return zkhost;
  }

  public void setZkHost(String zkhost) {
    this.zkhost = zkhost;
  }

  public String getZkRoot() {
    return zkroot;
  }

  public void setZkRoot(String zkroot) {
    this.zkroot = zkroot;
  }

  public Path getXmlOutPath() {
    return xmloutpath;
  }

  public void setXmlOutPath(Path xmloutpath) {
    this.xmloutpath = xmloutpath;
  }

  public Path getSolrConfigPath() {
    return path.resolve("solrconfig.xml");
  }
  
  public Path getSchemaPath() {
    return path.resolve("schema.xml");
  }

  // but it does know how to load defaults from an properties file
  public void loadDefaults(Path defaults) {
    Properties p = new Properties();
    try {
      p.load(Files.newInputStream(defaults));
    } catch (IOException e) {
      System.err.println("Invalid format or I/O error reading "+defaults);
    }
    String v = null;
    if ((v = p.getProperty("method")) != null)
      this.method = SolrConfig.Method.valueOf(v.toUpperCase());
    if ((v = p.getProperty("zkhost")) != null)
      this.zkhost = v;
    if ((v = p.getProperty("zkroot")) != null)
      this.zkroot = v;
  }
  
  // and it knows a canonical path to that
  public void loadDefaults() {
    Path userhome = Paths.get(System.getProperty("user.home"));
    Path defaultConfigFile = userhome.resolve(".solrconfigtest");    
    if (Files.exists(defaultConfigFile)) {
      this.loadDefaults(defaultConfigFile);
    }
  }
}