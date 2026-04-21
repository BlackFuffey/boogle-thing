#!/bin/sh

case "$1" in
    ""|"build") javac -d ./build Main.java ;;

    "test") java -cp ./build Main ;;

    *) echo "bad option" ;;

esac
