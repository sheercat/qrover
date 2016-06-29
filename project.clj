(defproject
  qrover
  "1.1.1"
  :description "I create QR code that is include email address."
  :ring {:handler qrover.handler/app,
         :init qrover.handler/init,
         :destroy qrover.handler/destroy,
         }
  :ragtime {:migrations ragtime.sql.files/migrations,
            :database "jdbc:mysql://localhost:3306/qrover?user=diver&password=deai110"}
  :plugins [[lein-ring "0.8.10"]
            [lein-environ "0.5.0"]
            [ragtime/ragtime.lein "0.3.6"]
            [lein-pprint "1.1.1"]
            [lein-typed "0.3.5"]
            ]
  :url "http://qrover.example.com"
  :profiles {:uberjar {:aot :all },
             :production {:ring {:open-browser? false, :stacktraces? false, :auto-reload? false}
                          :env {:dev false :stage "production"}
                          },
             :dev {:dependencies [[ring-mock "0.1.5"]
                                  [ring/ring-devel "1.3.0"]
                                  [pjstadig/humane-test-output "0.6.0"]
                                  [io.aviso/pretty "0.1.12"]
                                  ;; [colorize "0.1.1"]
                                  ],
                   :injections [(require 'pjstadig.humane-test-output)
                                (pjstadig.humane-test-output/activate!)],
                   :env {:dev true :stage "dev"}}}
  :dependencies [[log4j "1.2.17"
                  :exclusions [javax.mail/mail
                               javax.jms/jms
                               com.sun.jdmk/jmxtools
                               com.sun.jmx/jmxri]]
                 ;; [domina "1.0.2"] ;; un...
                 [selmer "0.6.9"] ;; template engine
                 [com.taoensso/timbre "3.2.1"] ;; logger
                 [com.taoensso/carmine "2.6.2"] ;; for use redis.
                 ;; [com.taoensso/tower "2.0.2"] ;; ?
                 [mysql/mysql-connector-java "5.1.25"] ;; see name
                 [noir-exception "0.2.2"] ;;ex
                 ;; [markdown-clj "0.9.47"] ;; un....
                 [environ "0.5.0"] ;; env module
                 ;; [korma "0.3.2"] ;; DSL for SQL
                 ;; [sqlingvo "0.6.1"] ;; DSL for SQL
                 [honeysql "0.4.3"] ;; DSL for SQL
                 ;; [org.clojure/clojurescript "0.0-2280"] ;; not use
                 ;; [prismatic/dommy "0.1.2"] ;; ?
                 [org.clojure/clojure "1.5.1"] ;; god
                 [org.clojure/core.typed "0.2.63"] ;; god!
                 [ring-server "0.3.1"] ;; base arch
                 [ragtime "0.3.6"] ;; db migrate
                 [ring/ring-anti-forgery "1.0.0"] ;; see name
                 [clj-time "0.7.0"] ;; time module
                 [lib-noir "0.8.4"] ;; thanks waf.
                 [http-kit "2.1.18"] ;; post jetty
                 ;; [net.glxn/qrgen "1.3"]
                 [clj.qrgen "0.1.1"] ;; qr generator but I copy to qrover.ex.qrgen.
                 [org.clojure/java.jdbc "0.3.4"] ;; orm
                 ;;[java-jdbc/dsl "0.1.0"] ;; uh...
                 [org.clojure/data.codec "0.1.0"] ;; base64
                 [crypto-random "1.2.0"] ;; see name
                 [com.draines/postal "1.11.1"] ;; mailer
                 [com.mchange/c3p0 "0.9.2.1"] ;; con pooling
                 ;; [com.cemerick/friend "0.2.1"] ;; basic auth but error
                 [liberator "0.12.0"] ;; restful api fw
                 [ring-basic-authentication "1.0.5"] ;; basic auth
                 ;; [clj-http "0.9.2"] ;; lwp
                 ]
  :repl-options {:init-ns qrover.repl}
  :min-lein-version "2.0.0"
  :main qrover.core
  ;; :jvm-opts ^:replace []
  :jvm-opts ["-server" "-XX:+TieredCompilation" "-XX:TieredStopAtLevel=1"]
  ;; :main qrover.socket
  :core.typed {:check [qrover.core]}
  )
