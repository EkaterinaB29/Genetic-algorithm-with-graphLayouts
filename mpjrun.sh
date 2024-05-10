#!/bin/bash
MPJ_HOME=/home/ekaterina/Desktop/mpi/mpj-v0_44  # MPJ home directory
JAVA_HOME=/usr/lib/jvm/openjdk-22 # Java home directory
export MPJ_HOME=/home/ekaterina/Desktop/mpi/mpj-v0_44
export PATH=$JAVA_HOME/bin:$PATH  # Add Java binaries to PATH

$MPJ_HOME/bin/mpjrun.sh -np 4 -classpath /home/ekaterina/Desktop/Genetic/out/production/GeneticAlgorithm GeneticAlgorithmDistributed "$1" "$2" "$3"
