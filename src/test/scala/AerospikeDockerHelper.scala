package sesame

import org.scalatest.Suite
import com.whisk.docker._
import com.whisk.docker.testkit._
import com.spotify.docker.client.messages.PortBinding
import com.spotify.docker.client.messages.HostConfig.Bind
import com.whisk.docker.testkit.scalatest.DockerTestKitForAll

trait AerospikeDocker extends DockerTestKitForAll { self: Suite =>

  private val ContainerName = s"${getClass.getSimpleName}-aerospike"

  protected val container =
    ContainerSpec("aerospike:ce-6.3.0.4_1")
      .withName(ContainerName)
      .withPortBindings(
        3000 -> PortBinding.of(null, 3000),
        3001 -> PortBinding.of(null, 3001),
        3002 -> PortBinding.of(null, 3002),
      )
      .withReadyChecker(DockerReadyChecker.LogLineContains("soon there will be cake"))
      .withVolumeBindings(
        Bind
          .from("/home/bertrand/Code/sésame/aerospike_conf")
          .to("/opt/aerospike/etc/").build
      )
      .withCommand("--config-file", "/opt/aerospike/etc/aerospike.conf")
      .toContainer

  override val managedContainers = container.toManagedContainer
}
