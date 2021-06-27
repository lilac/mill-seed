import java.io.IOException


/**
 * A simple example of how to use the Java API from an application outside a kubernetes cluster
 *
 * <p>Easiest way to run this: mvn exec:java
 * -Dexec.mainClass="io.kubernetes.client.examples.KubeConfigFileClientExample"
 *
 */
object KubeConfigFileClientExample {
  @throws[IOException]
  @throws[ApiException]
  def main(args: Array[String]): Unit = { // file path to your KubeConfig
    val kubeConfigPath = "/Users/junjun/.kube/config.d/staging.yaml"
    // loading the out-of-cluster config, a kubeconfig from file-system
    val client = Config.fromConfig(kubeConfigPath)
    // val client = Config.defaultClient()
    // set the global default api-client to the in-cluster one from above
    Configuration.setDefaultApiClient(client)
    // the CoreV1Api loads default api-client from global configuration.
    val api = new CoreV1Api()
    // invokes the CoreV1Api client
    val list = api.listPodForAllNamespaces(null, null, null, null, null, null, null, null, null)
    println("Listing all pods: ")
    // import scala.collection.JavaConverters._

    for (item <- list.getItems.asScala) {
      println(item.getMetadata.getName)
    }
  }
}

KubeConfigFileClientExample.main(Array())
