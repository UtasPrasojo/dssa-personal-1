#!/usr/bin/env bash
set -e
rm -rf bin
mkdir -p bin
javac -d bin $(find src -name "*.java")
java --module-path bin --module personal1/Mahasiswa.WebServer
