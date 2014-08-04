(ns arbol.dev
  (:require [arbol.core :as core]))

; these transformations echo the gorilla repl demo available in ws

(def simple
  [{:key "A" 
    :values "a val"} 
   {:key "B" 
    :values "b val"}])  

(def simple-map
  {:key "A" 
   :values "a val"
   :child {:key "B" 
           :values "b val"}})

(def mixed 
  [{:key "A" 
    :values [{:x "a" :y 1} 
             {:x "b" :y 2} 
             {:x "c" :y 3}]} 
   {:key "B" 
    :values [{:x "d" :y 4} 
             {:x "e" :y 5} 
             {:x "f" :y 6}]}])

(defn run
  "view in console"
  [stmt res]
  (js/console.log (str "cljs: " stmt))
  (js/console.log (str "js result: "))
  (js/console.log (clj->js res)))

(run 
  '(core/climb simple [:values] #(str "<" % ">"))
  (core/climb simple [:values] #(str "<" % ">"))) ; select any :values element in the vector tree

(run 
  '(core/climb simple-map [:values] #(str "<" % ">"))
  (core/climb simple-map [:values] #(str "<" % ">"))) ; select any :values element in the map tree

; notice the switch back to a vector
; save the original y
; just a little chaining just for the heck of it
(run 
  '(core/climb 
     mixed 
     [:values :vec] #(take-last 2 %) reverse vec 
     [:values :map] #(assoc % :orig-y (:y %))    
     [:y]           (partial * 2) inc)
  (core/climb 
    mixed 
    [:values :vec] #(take-last 2 %) reverse vec 
    [:values :map] #(assoc % :orig-y (:y %))    
    [:y]           (partial * 2) inc))
           