#!/bin/bash

cd Concurrent_C
make
cd ..

for mercat in "Concurrent_C/mercat15j.csv" "Concurrent_C/mercat25j.csv" "Concurrent_C/mercat50j.csv" "Concurrent_C/mercat60j.csv" "Concurrent_C/mercat75j.csv" "Concurrent_C/mercat100j.csv"
do  
	echo $mercat
	echo "Concurrent: "
	time ./Concurrent_C/manfutc 300 $mercat 5
	echo "Secuencial: "
	time ./Secuencial_C/manfutc 300 $mercat
done

