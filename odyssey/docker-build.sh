#!/bin/bash
# Set docker image:
# docker pull vkuznyetsov/odyssey
# 
# To use docker without sudo:
# sudo groupadd docker
# sudo gpasswd -a ${USER} docker
# log out and log back in again.
docker run -i -t -e USRID=$(id -u) --privileged -e USRGR=$(id -g) -v $(pwd):/home/user/app -v ${HOME}/.m2:/home/user/.m2 vkuznyetsov/odyssey-java8-docker-build sh -c 'sudo chown user:user -R /home/user/app && sudo chown user:user -R /home/user/.m2 && mvn clean install; sudo chown $USRID:$USRGR -R /home/user/app; sudo chown $USRID:$USRGR -R /home/user/.m2'
