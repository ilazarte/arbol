(ns arbol.internal_test
  (:require [clojure.string :as str]
            [arbol.internal :as internal]
            [midje.sweet    :as sweet]))

(sweet/fact 
  "elements do not resolve to their parent"
  (internal/matches [:vec] [:vec :.val]) => false)


