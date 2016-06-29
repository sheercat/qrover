#!/bin/sh

exec 2>&1

_app_root=$(cd `dirname $0`/.. && pwd -P)

USER=`whoami`
export USER

# rbenv
export PATH="$_app_root/bin:~/.rbenv/bin:~/.rbenv/shims:$PATH"

exec "$@"
