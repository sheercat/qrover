#!/bin/sh
BUILD_TARGET="all"
RESTART=0
TAIL=0
DEBUG=0
NO_USER_CHECK=0
while getopts b:rtld OPT
do
    case $OPT in
        b)
            BUILD_TARGET=$OPTARG
            ;;
        r)
            RESTART=1
            ;;
        t)
            TAIL=1
            ;;
        l)
            NO_USER_CHECK=1
            ;;
        d)
            DEBUG=1
            ;;
    esac
done

if [ $NO_USER_CHECK = 0 ]; then
    user_check(){
        EXEC_USER=`whoami`
        if [ $EXEC_USER != "qrover" ]; then
            echo "exec sh not qrover user"
            exit 1;
        fi
    }
    user_check
fi

if [ ! $BUILD_TARGET = "no" ]; then

    BUILD_NAME=`lein pprint :name | sed -e 's/"//g'`
    BUILD_VERSION=`lein pprint :version | sed -e 's/"//g'`
    BUILD_JARNAME=$BUILD_NAME-$BUILD_VERSION-standalone.jar

    echo "lein clean"
    lein clean

    if [ $BUILD_TARGET = "all" -o $BUILD_TARGET = "web" ]; then
        # web
        echo "lein uberjar (web)"
        lein uberjar

        pushd target && cp -a $BUILD_JARNAME $BUILD_NAME-release-standalone.jar && popd
    fi


    if [ $BUILD_TARGET = "all" -o $BUILD_TARGET = "api" ]; then
        # api
        echo "lein uberjar (api)"
        lein uberjar qrover.api

        pushd target && cp -a $BUILD_JARNAME $BUILD_NAME-release-standalone-api.jar && popd
    fi

fi

if [ $RESTART = "1" ]; then
    # daemontools
    echo "svc -t /service/qrover*"
    sudo svc -t /service/qrover-api-jar
    sudo svc -t /service/qrover-jar
fi

if [ $TAIL = "1" ]; then
    # tail
    tail -F /service/qrover-api-jar/log/main/current /service/qrover-jar/log/main/current | tai64nlocal
fi
