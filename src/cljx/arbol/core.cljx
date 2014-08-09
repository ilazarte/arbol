(ns arbol.core
  (:require [arbol.internal :as internal]))

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
              fx   (internal/rev-comp (rest spec))]
        (recur (internal/traverse mr sel fx) (rest pr)))))))