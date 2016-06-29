DELETE FROM mysql.user WHERE user='diver';
GRANT ALL ON *.* TO diver@'127.0.0.%' IDENTIFIED BY 'hogehoge110';
GRANT ALL ON *.* TO diver@'10.%'      IDENTIFIED BY 'hogehoge110';
GRANT ALL ON *.* TO diver@'localhost' IDENTIFIED BY 'hogehoge110';
FLUSH PRIVILEGES;

DELETE FROM mysql.user WHERE user='diver_slave';
GRANT SELECT ON *.* TO diver_slave@'127.0.0.%' IDENTIFIED BY 'hogehoge119';
GRANT SELECT ON *.* TO diver_slave@'10.%'      IDENTIFIED BY 'hogehoge119';
GRANT SELECT ON *.* TO diver_slave@'localhost' IDENTIFIED BY 'hogehoge119';
FLUSH PRIVILEGES;
