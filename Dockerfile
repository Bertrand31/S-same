ARG VERSION=ce-6.3.0.4_1
FROM aerospike:$VERSION

# RUN /bin/bash -c 'apt update && apt install -y wget neovim'
RUN apt update && apt install -y --allow-change-held-packages wget neovim
RUN wget -O /etc/aerospike/aerospike.conf "https://raw.githubusercontent.com/Bertrand31/Sesame/master/aerospike.conf"
RUN apt clean -y

# CMD ["/bin/bash"]

EXPOSE 3000
