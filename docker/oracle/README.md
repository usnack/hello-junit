# Oracle 데이터 dump 

## Summary    
Oracle Dump 기능을 Docker 환경에서 테스트한다.       

## Resources      
- docker-compose.yaml : 원본 데이터를 가지는 export 대상 컨테이너(prod)와 import 대상 컨테이너(dev)로 이루어진 docker compose   
- startup: 컨테이너 실행 시 실행할 sql      
- script: oracle 컨테이너, export, import 를 위한 스크립트    

## Getting Started      

1. Oracle 이미지 생성   
- Oracle에서 공식적으로 제공하는 Repository를 통해 이미지를 로컬에 생성한다.   
    ```shell
    git clone https://github.com/oracle/docker-images.git
    cd docker-images/OracleDatabase/SingleInstance/dockerfiles
    ./buildDockerImage.sh -v 18.4.0 -x
    ```

- Oracle 컨테이너를 실행한다.   
    ```shell
    docker run -d --name oracle-plain \
    -e ORACLE_PWD=1234 \
    -e ORACLE_CHARACTERSET=AL32UTF8 \
    -p 1521:1521 \
    -p 5500:5500 \
    oracle/database:18.4.0-xe
    ```

- 초기 구동 속도를 높이기 위해 현재 실행중인 Oracle 컨테이너의 snapshot 이미지를 생성한다.          
    ```shell
    docker commit \
    <conatiner-id> \
    oracle/database:18.4.0-xe-snapshot
    ```

2. Prod DB Export             
export.sh 실행

3. Dev DB Import   
import.sh 실행