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
  "Replace a simple key anywhere in a hierarchy"
  (core/climb 
    obj-vec 
    [:value string?] #(str "<" % ">")) => [{:a 1
                                            :b 2
                                            :value "<apple>"
                                            :child {:x 24
                                                    :y 25
                                                    :value "<pie>"}}
                                           {:a 3
                                            :b 4
                                            :value "<orange>"}])

(sweet/fact 
  "Target a specific item with parent selectors."
  (core/climb 
    obj-vec 
    [:child :value string?] #(str "<" % ">")) => [{:a 1
                                                   :b 2
                                                   :value "apple"
                                                   :child {:x 24
                                                           :y 25
                                                           :value "<pie>"}}
                                                  {:a 3
                                                   :b 4
                                                   :value "orange"}])

(sweet/fact
  "Vectors can be altered directly as well via :map or :vec keys"
  (core/climb obj-vec [vector?] last vector) => [{:a 3
                                                  :b 4
                                                  :value "orange"}])

(def str-vec ["The hills are alive" "with the Sound of Music"])

(sweet/fact
  "We can target only the collection elements" 
  (core/climb
    str-vec 
    [string?] #(vector :li %)) => [[:li "The hills are alive"] [:li "with the Sound of Music"]])

(sweet/fact
  "Build up new trees by chaining selectors and operations."
  (core/climb
    str-vec 
    [string?] #(vector :li %)
    []        #(apply vector :ul %)) => [:ul [:li "The hills are alive"] [:li "with the Sound of Music"]])

(def link-data [{:text "Google" :link "http://google.com"}
                {:text "Apple" :link "http://apple.com"}])

(sweet/fact
  "Let's make hiccup links!"
  (core/climb
    link-data 
    [vector?] #(apply vector :ul.links %) 
    [map?] #(identity [:li [:a {:href (:link %)} (:text %)]])) => 
  [:ul.links 
   [:li 
    [:a {:href "http://google.com"} "Google"]] 
   [:li 
    [:a {:href "http://apple.com"} "Apple"]]])

(def squash-me [{:symbol "GOOG"
                 :values [{:x 1
                           :y 2}
                          {:x 3}
                          {:y 4}]}
                {:symbol "AAPL"
                 :values [{:x 5
                           :y 6}
                          {:x 7}
                          {:y 8}]}])

(sweet/fact
  "Predicates (in a vector after selector) allow for sub-selection of a type."
  (core/climb
    squash-me 
    [map? [#(contains? % :symbol)]]
    #(assoc % :y (-> (:values %) last :y))
    #(dissoc % :values)) => [{:symbol "GOOG"
                              :y      4}
                             {:symbol "AAPL"
                              :y      8}])

(sweet/fact
  "Multiple predicates are allowed."
  (core/climb
    squash-me 
    [map? [#(contains? % :x)
           #(= 5 (:x %))]]
    #(update-in % [:x] (partial + 100))) => [{:symbol "GOOG"
                                              :values [{:x 1
                                                        :y 2}
                                                       {:x 3}
                                                       {:y 4}]}
                                             {:symbol "AAPL"
                                              :values [{:x 105
                                                        :y 6}
                                                       {:x 7}
                                                       {:y 8}]}])

(sweet/fact
  "The previous example be simplified to:"
  (core/climb
    squash-me 
    [:x number? [#(= 5 %)]]
    (partial + 100)) => [{:symbol "GOOG"
                          :values [{:x 1
                                    :y 2}
                                   {:x 3}
                                   {:y 4}]}
                         {:symbol "AAPL"
                          :values [{:x 105
                                    :y 6}
                                   {:x 7}
                                   {:y 8}]}])

(def menu [{:label "Blue Beauty"
            :id    0
            :ingredients {:sweetener "Turbinado"
                          :fruit     ["blueberry" "strawberry"]}}
           {:label "Smashing Smoothie"
            :id    1
            :ingredients {:sweetener "Turbinado"
                          :fruit     ["banana" "strawberry"]}}])

(sweet/fact
  "Predicates are good for subselection."
  (core/climb
    menu 
    [map? [#(= (:id %) 1)] :sweetener string?]
    (constantly "Splenda")) => [{:label "Blue Beauty"
                                 :id    0
                                 :ingredients {:sweetener "Turbinado"
                                               :fruit     ["blueberry" "strawberry"]}}
                                {:label "Smashing Smoothie"
                                 :id    1
                                 :ingredients {:sweetener "Splenda"
                                               :fruit     ["banana" "strawberry"]}}])