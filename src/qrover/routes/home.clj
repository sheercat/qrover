(ns qrover.routes.home
  (:use compojure.core)
  (:require [qrover.layout :as layout]
            ))

(defn home-page [] (layout/render "home.html" ))

(defn about-page [] (layout/render "about.html"))

(defroutes home-routes
  (GET "/" [] (home-page))
  (GET "/about" [] (about-page)))
