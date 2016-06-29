# qrover

QR creator.

## Prerequisites

You will need [Leiningen][1] 2.0 or above installed.

[1]: https://github.com/technomancy/leiningen

## Running

To start a web server for the application, run for develop:

    lein run

## install

### install jdk (reuire 1.7 or higher)

    yum install java-1.7.0-openjdk.x86_64 

### install ruby (require 2.0 or higher)

    cd
    git clone git://github.com/sstephenson/rbenv.git .rbenv
    echo 'export PATH="$HOME/.rbenv/bin:$PATH"' >> ~/.bash_profile
    echo 'eval "$(rbenv init -)"' >> ~/.bash_profile
    exec $SHELL -l
    git clone git://github.com/sstephenson/ruby-build.git ~/.rbenv/plugins/ruby-build
    rbenv install 2.0.0-p481
    rbenv global 2.0.0-p481
    rbenv local 2.0.0-p481

### install ruby modules

    gem install bundler mechanize mail

### install mysql (5.5 or higher)

    yum install mysql55.x86_64

#### and start

    service mysqld start

### install redis (with yum repos in http://powerstack.org)

    yum install redis

#### and start

    service redis start

### git clone me

    mkdir -p qrover
    cd qrover
    git clone <me> current
    cd current
    lein deps
    ./sbin/build.sh
    ./sbin/release.sh

### install leiningen

    cd
    mkdir bin
    cp -a ...qrover/current/bin/lein ~/bin/lein
    chmod 755 ~/bin/lein

#### and set ~/bin to your PATH

    lein

### install service for svc

    cp -a ...qrover/current/etc/service/qrover-jar /service/.qrover-jar

### install postfix config

    cp ..qrover/current/etc/postfix/admin103/* /etc/postfix/
    cd /etc/postfix/
    postmap /etc/postfix/transport.regexp
    postmap /etc/postfix/virtual
    postfix reload

### setup mysql

    create database qrover
    # and grant user
    
    cd qrover/current
    lein ragtime migrate

## boot

### svc

    mv /service/.qrover-jar /service/qrover-jar

## License

MIT
