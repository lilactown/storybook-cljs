(ns corp.design.button
  (:require
    [corp.design.lib :refer ($)]
    [goog.object :as gobj]))

(defn basic
  [props]
  ($ "button" props (gobj/get props "children")))
