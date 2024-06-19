class ServerConfig {
  private int port = 6379;
  private int masterPort;
  private String masterHost;

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public int getMasterPort() {
    return masterPort;
  }

  public void setMasterPort(int masterPort) {
    this.masterPort = masterPort;
  }

  public String getMasterHost() {
    return masterHost;
  }

  public void setMasterHost(String masterHost) {
    this.masterHost = masterHost;
  }
}