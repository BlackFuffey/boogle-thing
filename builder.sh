#!/bin/sh

case "$1" in
    ""|"build") javac -d ./build -sourcepath src src/Main.java ;;

    "run") java -cp ./build Main ;;

    *) echo "bad option" ;;

esac
