# Kafka Nasa ElasticSearch
Pulls data from a Nasa API and send it to ElasticSearch through Kafka for indexing

### How to run
Docker compose will spin up all the dependencies: kafka, zookeeper, elasticsearch and kibana  

    docker-compose up

Maybe ElasticSearch will not spin up due to low virtual memory. To increase it run 
 
    sudo sysctl -w vm.max_map_count=262144