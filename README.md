# Arbol

Arbol is a mixed data type tree transformer using simple selectors available in Clojure and ClojureScript.  The api will most likely change a bit.  The selectors portion definitely as I'd like to experiment with available selection types.

[![Clojars Project](http://clojars.org/ilazarte/arbol/latest-version.svg)](http://clojars.org/ilazarte/arbol)

## Usage

What it can do in a super quick summary taken from the midje tests.
The test package is the way to learn the api (which is still in progress)

setup:

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

## Plans

Now that predicates are added, will leave this for a bit, to see how it does, is it useful etc.
Recently converted pseudo key selectors to fn based selectors.
Potential inclusion are direct parentage options or index selectors for vectors.
Will keep using and trying to find more usage patterns.

## License

Copyright &copy; ilazarte 2014 Released under the Eclipse Public License, the same as Clojure.
