function convertWav() {
    FILENAME=$(echo "$1" | cut -d "/" -f 3- | cut -d "." -f -2).wav
    ffmpeg -y -i "$1" -ac 1 -c:a pcm_s16le -ar 44100 "data/$FILENAME" 2> /dev/null
    echo "=> Converted '$1' to '$FILENAME'"
}

export -f convertWav

find data/rawFiles/ -name "*.flac" -exec bash -c "convertWav \"{}\"" \;
