ARG VERSION=ce-6.3.0.4_1
FROM aerospike:$VERSION

RUN apt update \
  && apt install -qq -o=Dpkg::Use-Pty=0 -y wget >/dev/null \
  && apt clean -qq -o=Dpkg::Use-Pty=0 -y >/dev/null \
  # && rm /etc/aerospike/aerospike.conf \
  && wget --quiet "https://raw.githubusercontent.com/Bertrand31/Sesame/master/aerospike.conf" -O /etc/aerospike/aerospike2.conf

EXPOSE 3000
