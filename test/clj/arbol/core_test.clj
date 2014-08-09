(ns arbol.core-test
  (:require [clojure.string :as str]
            [arbol.core     :as core]
            [midje.sweet    :as sweet]))

(def obj-vec [{:a 1
               :b 2
               :value "apple"
               :child {:x 24
                       :y 25
                       :value "pie"}}
              {:a 3
               :b 4
               :value "orange"}])

(sweet/fact 
  "we can replace simple string value in a collection at any depth in a hierarchy"
  (core/climb 
    obj-vec 
    [:value :.val] #(str "<" % ">")) => [{:a 1
                                          :b 2
                                          :value "<apple>"
                                          :child {:x 24
                                                  :y 25
                                                  :value "<pie>"}}
                                         {:a 3
                                          :b 4
                                          :value "<orange>"}])

(sweet/fact 
  "we target specific depths of the same key name requiring a selector parent"
  (core/climb 
    obj-vec 
    [:child :value :.val] #(str "<" % ">")) => [{:a 1
                                                 :b 2
                                                 :value "apple"
                                                 :child {:x 24
                                                         :y 25
                                                         :value "<pie>"}}
                                                {:a 3
                                                 :b 4
                                                 :value "orange"}])

(sweet/fact
  "vectors can be altered directly as well via :map or :vec keys"
  (core/climb obj-vec [:.vec] last vector) => [{:a 3
                                                :b 4
                                                :value "orange"}])

(def str-vec ["The hills are alive" "with the Sound of Music"])

(sweet/fact
  "we can target only the collection elements" 
  (core/climb
    str-vec 
    [:.val] #(vector :li %)) => [[:li "The hills are alive"] [:li "with the Sound of Music"]])

(sweet/fact
  "go deeper.  we can build up new trees by chaining selectors and operation.
   we can transform from the inside out, almost like an xsl transformation"
  (core/climb
    str-vec 
    [:.val] #(vector :li %)
    []      #(apply vector :ul %)) => [:ul [:li "The hills are alive"] [:li "with the Sound of Music"]])

(def link-data [{:text "Google" :link "http://google.com"}
                {:text "Apple" :link "http://apple.com"}])

(sweet/fact
  "lets make some hiccup links! due to simple selectors, we have to work outside in.
   maybe in breaking change some concept of axis or parentage (aka // or / in xpath)"
  (core/climb
    link-data 
    [:.vec] #(apply vector :ul.links %) 
    [:.map] #(identity [:li [:a {:href (:link %)} (:text %)]])) => 
  [:ul.links 
   [:li 
    [:a {:href "http://google.com"} "Google"]] 
   [:li 
    [:a {:href "http://apple.com"} "Apple"]]])