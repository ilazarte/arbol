# Arbol

Arbol is a mixed data type tree transformer using simple selectors available in Clojure and ClojureScript.  The api will most likely change a bit.  The selectors portion definitely as I'd like to experiment with available selection types.

[![Clojars Project](http://clojars.org/ilazarte/arbol/latest-version.svg)](http://clojars.org/ilazarte/arbol)

## Usage

What it can do in a super quick summary.  The same examples and more are available in the a gorilla worksheet.
To launch, clone the repo, and start Gorilla with lein gorila.
Navigate to the launch url and you can load the arbol-demo worksheet.
--EDIT-- The worksheet is currently broken during project.clj changes, will restore.

setup:

    (def mixed 
      [{:key "A" 
        :values [{:x "a" :y 1} 
                 {:x "b" :y 2} 
                 {:x "c" :y 3}]} 
       {:key "B" 
        :values [{:x "d" :y 4} 
                 {:x "e" :y 5} 
                 {:x "f" :y 6}]}])

    (climb 
      mixed 
      [:values :.vec] #(take-last 2 %) reverse vec ; notice the switch back to a vector
      [:values :.map] #(assoc % :orig-y (:y %))    ; save the original
      [:y :.val]      (partial * 2) inc)           ; just a little chaining just for the heck of it
     
 result:
  
    [{:values [{:x "c", :y 7, :orig-y 3} {:x "b", :y 5, :orig-y 2}], :key "A"} 
     {:values [{:x "f", :y 13, :orig-y 6} {:x "e", :y 11, :orig-y 5}], :key "B"}] 

## Plans

Now that predicates are added, will leave this for a bit, to see how it does, is it useful etc.
TODO add more examples, but currently midje tests in source demo how to use them.

## License

Copyright &copy; ilazarte 2014 Released under the Eclipse Public License, the same as Clojure.
