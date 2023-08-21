#!/bin/bash

docker exec -it oracle-db-prod expdp C##DUMPER/1234@XE tables=member directory=dump_vol dumpfile=230821_1.dmp logfile=230821_1.log