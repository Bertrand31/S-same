package sesame.storage.aerospike

object AerospikeConstants:

  val MetadataNamespace = "metadata"
  val MetadataSet = "metadata"

  // Collocated with metadata because of Aerospike CE's limitation to 2 namespaces
  val IDNamespace = MetadataNamespace
  val IDSet = "ids"
  val IDDefaultRecord = "song-id"
  val SingleCounterBin = "id-bin"

  val FooprintNamespace = "footprint"
  val FootprintSet = null // we don't need sets. Not using a set name saves overhead on every record
  val FootprintBin = "" // single-bin namespace implies empty bin name
