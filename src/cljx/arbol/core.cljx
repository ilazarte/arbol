(ns arbol.core)

(def ^:private restv (comp #(if (empty? %) nil %) vec rest))

(defn- matches [selector path]
  "see if the selector is matched at the path
   all of the path elements of selector must be found in order in the path
   the selector and the path must also be end with the same element
   this is similar to the old school css selector 'table tr td'
   which matches any td with that parentage even in the case of nested n depth tds"
  (loop [sa path
         sb selector]  
    (cond
      (= sa sb) true 
      (and (not sa) (not sb)) true
      (and (not sa) sb) false
      (and sa (not sb)) false
      :else (let [fa (first sa)
                  fb (first sb)]
              (if (and (= fa fb) (> (count sb) 1)) 
                (recur (restv sa) (restv sb))
                (recur (restv sa) sb))))))

(defn- traverse
  ([mixed selector fx]
    (traverse mixed selector fx []))
  ([mixed selector fx loc]
    (cond
      (map? mixed)
      (let [nloc (conj loc :map)
            mfx  #(assoc %1 (first %2) (traverse (second %2) selector fx (conj nloc (first %2))))
            res  (reduce mfx {} mixed)]
        (if (matches selector nloc) (fx res) res))
      (vector? mixed)
      (let [nloc (conj loc :vec)
            vfx  #(traverse % selector fx nloc)
            res  (mapv vfx mixed)]
        (if (matches selector nloc) (fx res) res))
      (sequential? mixed)
      (let [nloc (conj loc :seq)
            sfx  #(conj %1 (traverse %2 selector fx nloc))
            nseq (empty mixed)
            res  (reverse (reduce sfx nseq mixed))]
        (if (matches selector nloc) (fx res) res))
      :else (if (matches selector loc) (fx mixed) mixed))))

(defn- rev-comp [fns]
  "create the implied order fx"
  (apply comp (reverse fns)))

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
              fx   (rev-comp (rest spec))]
        (recur (traverse mr sel fx) (rest pr)))))))