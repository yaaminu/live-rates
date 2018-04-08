#!/bin/bash

DIR_NAME="/home/yaaminu/stocks/"
   
mkdir -p $DIR_NAME

curl -L "http://dev.kwayisi.org/apis/gse/live" > $DIR_NAME"stock-"`date +%F`