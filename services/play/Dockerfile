ARG OPENJDK_TAG=15
FROM openjdk:${OPENJDK_TAG}

USER root
RUN id -u tstone 1>/dev/null 2>&1 || (( getent group 0 1>/dev/null 2>&1 || ( type groupadd 1>/dev/null 2>&1 && groupadd -g 0 root || addgroup -g 0 -S root )) && ( type useradd 1>/dev/null 2>&1 && useradd --system --create-home --uid 1001 --gid 0 tstone || adduser -S -u 1001 -G root tstone ))

WORKDIR /tstone
COPY target/docker/stage/opt/docker /tstone
USER root
RUN ["chmod", "-R", "u=rX,g=rX", "/tstone"]
RUN ["chmod", "u+x,g+x", "/tstone/bin/tstone"]
RUN ["chown", "-R", "tstone", "/tstone"]
USER 1001:0
EXPOSE 9000
ENTRYPOINT ["/tstone/bin/tstone"]
CMD []
