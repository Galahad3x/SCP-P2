#!/bin/bash

cd Concurrent_C
make
cd ..

for mercat in "mercat15j.csv" "mercat25j.csv" "mercat50j.csv" "mercat60j.csv" "mercat75j.csv" "mercat100j.csv"
do  
	echo "Concurrent C $mercat: "
	time ./Concurrent_C/manfutc 300 "Concurrent_C/$mercat" 5
	echo "Secuencial C $mercat: "
	time ./Secuencial_C/manfutc 300 "Concurrent_C/$mercat"
	echo "Concurrent Java $mercat: "
	javac /Concurrent_Java/src/Manfutc.java
	time java Concurrent_Java/src/Manfut 300 "Concurrent_C/$mercat" 5
	echo "Secuencial Java $mercat: "
	cd Secuencial_Java/Manfut/src
	javac *.java
	time java Manfut 300 "../../$mercat"
	cd ../../..
done

