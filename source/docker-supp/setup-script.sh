#!/bin/sh

set -eu

apt-get update
apt-get -y --no-install-recommends install openjdk-8-jre-headless openjdk-8-jdk-headless maven \
    python3-flask python3-pip python3-setuptools python3-wheel curl supervisor

groupadd ihatestau
useradd -m -r -g ihatestau ihatestau

cd /ihatestau-source

# install python code
pip3 install tensorflow==1.7

ln -s /ihatestau-source/neural-net-server /home/ihatestau/neural-net-server
chmod a+x /home/ihatestau/neural-net-server/serve.py

# build java code
(cd iHateStau-parent && mvn package)

cp /ihatestau-source/iHateStau/target/webserver-1.0-SNAPSHOT.jar /home/ihatestau/webserver.jar
cp /ihatestau-source/iHateStau-image-preparation/target/image-preparation-1.0-SNAPSHOT.jar /home/ihatestau/masker.jar
cp /ihatestau-source/verkehrscam-scraper/target/iHateStau-kamerascraper-1.0-SNAPSHOT.jar /home/ihatestau/scraper.jar

# start server once for initial date import
su ihatestau -c 'java -jar /home/ihatestau/webserver.jar -insecure' &
SERVER_PID=$!

# wait until server is up
printf 'Waiting for server to come up...\n'
until $(curl --output /dev/null --silent --head --fail http://localhost:8080/ihatestau/info/html); do
    printf 'wating...\n'
    sleep 2
done

# sync cameras
java -jar /ihatestau-source/spot-synchronizer/target/iHateStau-spot-synchronizer-1.0-SNAPSHOT.jar

# kill background server
kill $SERVER_PID

# install supervisor config file
ln -s /ihatestau-source/docker-supp/supervisor.cfg /home/ihatestau/supervisor.conf

# cleanup
(cd iHateStau-parent && mvn clean)
apt-get -y remove maven openjdk-8-jdk-headless curl python3-pip python3-setuptools python3-wheel
apt-get -y autoremove
apt-get clean
rm -rf /var/lib/apt/lists/*



