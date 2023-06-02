# Sésame

This is a fun little side project I started working on because I've always found ⫙µ$ї¢ 𐅾℮∁օ₲∏⫯էℹ︎օη services to be cool. Don't sue please 😬

While I've been using this service with only a handful of songs I have handy, I've tried to make all design decisions assuming this service was going to be used by a large company which would have massive music catalogue (Spotify).

## (Very) brief overview of the algorithm

The algorithm behind this service has been described in a paper released in 2003 by ✨a company✨ I won't name because they have seemingly been acting like patent trolls on open-source, side project-type codebases.

Anyway, the basic idea is to slice up a song into same-length segments, and use the Fourier transformation on each of these segments to get spectrograms.
We then pick relevant "peaks" from each spectrogram, and combine them into hashes which become the footprint of that song.

For a much better and more detailed explanation, head to this [article](https://tinyurl.com/sesame-bertrand) or, even better, read [the original paper](https://tinyurl.com/sesame-bertrand-2).

## Design choices

### Assumptions

We have to work with a dataset the size of Spotify's: [80 million songs](https://moviemaker.minitool.com/moviemaker/how-many-songs-are-on-spotify.html) which are [3mn17s long on average](https://indiesongmakers.com/how-long-does-a-song-have-to-be-on-spotify/).
That means we have to store ~16 billion seconds.

Each song is assumed to be no longer than 600 seconds (10 minutes).

### Hashing

We want to store each hash alongside its position within the song, as well as the ID of the song so we can retrieve it.
Thus, the data stored for each hash making up a song's footprint will look like this: ``hash -> (position | songId)``. The pipe (`|`) is used here to represent a sum type.

Each hash produced by the footprinting algorithm is 64 bits long.
It has been empirically determined that, using the current encoding, each second of a song results in 21.5 hashes.

Given that songs are assumed to be 600 seconds long at most, the maximum number of hashes generated for a song is `600×21.5` which is `12900`.
Since we want to store each hash with its position within the relevant song, it appears that we need `(log₂12900) + 1`, 15 bits to store that "position".

Furthermore, if we want to futureproof our solution and account for the possibility of storing 200 million songs in the future, we need song IDs to be `log₂(2×10⁸) + 1`, 29 bits long.

Without accounting for database-specific overhead, this means that each key/value pair will be be like so:

     hash -> (position | songId)
      |          |          |
    64 bits   15 bits     29 bits

This means each key/value pair will weight 108 bits, or 14 bytes.

An average song will thus weight the following:

    14  ×  21.5  ×  197
     |      |        |
    k/v  hashes/s  avg. song length

This means that, without accounting for datastore-specific overhead, an average song will require 59297 bytes.
That means a total dataset size of `8×10⁷×59297` which gives us ~4.7 TB.

This could be optimized further by only using the necessary bits for both position indexes and song IDs, but there is not point in getting too far down this rabbit hole before having figured out the exact storage details (which may involve compression).

### Datastore

The solution that was picked is Aerospike, for the following reasons:
- ability to scale massively and seamlessly (multiple TBs/node, automatic hash-based sharding)
- out-of-the-box, automatic replication with automatic rebalancing
- priority given to availability over consistency, while enabling use to enforce strong consistency on a specific namespace
- support for both in-memory and on-disk storage (we will use both)
- support for both key/value and document models (we will also use both)
- no single point of failure

We use a replication factor of 2, because it allows us to keep operating with faulty nodes and minimizes the risk of data loss.

However, while this data is crucial for us to be able to operate, it is easily re-generated. For us, replication is much more about availability than preventing data loss.

#### How metadata is stored

We will assume all input audio files have adequate metadata attached, with at least a `TITLE` and an `ARTIST` attributes.
Upon ingestion, files' metadata is stored in dedicated namespace (Aerospike's name for a database) `metadata`.

Inside this namespace is a set (Aerospike's name for a table), also called `metadata`. Each record (row) is made up of a key, which is the song ID, and bins (columns). Those bins' names are not predetermined, we simply extract all of the file's metadata and store it as is. Some level of normalization will have to be introduced in the future.

The use of the words rows and columns here is only meant to help the reader follow, but the resulting model is very much a documents store model, as we do not enforce any type constraints on any of the values, and the labels themselves will vary.

Data in this namespace is compressed using LZ4, with a compression level of 5 (1 is least agressive, 9 is most aggressive).

#### How footprints are stored

Footprints are stored in a namespace called `footprint`, inside which records are stored directly (no set).
This makes each record a little lighter, as it doesn't store information regarding which set it belongs to (look up "if using a set name" [here](https://docs.aerospike.com/server/operations/plan/capacity)).

Data in this namespace is compressed using LZ4 with a compression level of 1.

#### Where do IDs come from?

Generating sequential IDs in a distributed system without creating a Single Point Of Failure or a performance bottleneck is always a tricky topic.
In our case, we use a separate, in-memory namespace with full linearizability enabled as well as replication, which contains only one key-value pair: "songId" and the last ID used. Every time we ingest a song, we `increment` that number (Aeorspike supports atomic increments on integer bins) and use the result as a new ID. The `increment` and the following `get` operations are part of a transaction.

While this very simple solution may not provide the performance of some more sophisticated systems, it is important to note that the number of queries on that namespace will be very, very low compared to the two others. Especially the `footprint` one.
It is then highly unlikely for it to become a bottleneck. And even if it somehow did, it would only slow down the ingestion process, not the end user's experience.

## Aerospike cheatsheet

    docker build -t sesame:0.1 .
    docker run -dp 3000:3000 sesame:0.1
    docker exec -it (docker ps -a -q | head -n1) /bin/bash
    docker restart (docker ps -a -q | head -n1)
