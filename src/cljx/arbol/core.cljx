(ns arbol.core
  (:require [arbol.internal :as internal]))

(comment
  "Query expressions is that traversal points to the items to be manipulated.
   Predicates (functions in brackets) are applied directly on the items pointed to.
   Any form inside the predicate is evaluated for its truthiness.
   The query expression is greedy which means the data structure must always be evaluated.
   For example [map? [:key]] will traverse all maps even child maps of a top map.
   This is consistent with selectors familiar to web devs.
   For example 'div' would match self and all child divs.")

(defn climb [mixed & specs]
  "the climb function traverse the trees any functions following a vector spec
   you might think of this as an implicit thread first"
  (let [pairs (map #(apply concat %) (partition 2 (partition-by vector? specs)))]
    (loop [mr mixed
           pr pairs]
      (if (empty? pr)
        mr
        (let [spec (first pr)
              sel  (first spec)
              fx   (apply comp (reverse (rest spec)))]
        (recur (internal/traverse mr sel fx) (rest pr)))))))