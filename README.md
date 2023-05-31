## Sésame

### Design choices

#### Assumptions

We have to work with a dataset the size of Spotify's: [80 million songs](https://moviemaker.minitool.com/moviemaker/how-many-songs-are-on-spotify.html) which are [3mn17s on average](https://indiesongmakers.com/how-long-does-a-song-have-to-be-on-spotify/).
That means we have to store ~16 billion seconds.

Each song is assumed to be no longer than 600 seconds (10 minutes).

### Hashing

We want to store each hash alongside its position within the song, as well as the ID of the song so we can retrieve it.
Thus, the tables that holds the hashes will look like this: ``hash -> (position | songId)``
Each hash is 64 bits long.
Each second of a song results in 21.5 hashes.
Given that songs are assumed to be 600 seconds long at most, the maximum number of hashes generated for a song is `600×21.5` which is `12900`.
Since we want to store each hash with its position within the relevant song, it appears that we need `(log₂12900) + 1`, 15 bits to store that "position".

Furthermore, if we want to futureproof our solution and account for the possibility of storing 200 million songs in the future, we need song IDs to be `log₂(2×10⁸) + 1`, 29 bits long.

Which accounting for database-specific overhead, this means that each key/value pair will be be like so:

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

### Datastore

The solution that was picked is Aerospike.

### Aerospike cheatsheet

    docker build -t sesame:0.1 .
    docker run -dp 3000:3000 sesame:0.1 --early-verbose
    docker exec -it (docker ps -a -q | head -n1) /bin/bash
    docker restart (docker ps -a -q | head -n1)
