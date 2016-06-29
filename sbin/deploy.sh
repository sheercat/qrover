#!/bin/sh

TARGET=$1
if [ ! -f $TARGET ]; then
    exit 1
fi

DISTINATION=server001.kvm:/home/qrover/qrover/current/target/$TARGET

rsync -e ssh -avzC $TARGET $DISTINATION


