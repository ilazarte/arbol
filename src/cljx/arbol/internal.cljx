(ns arbol.internal)

; Broken out for testing purposes
; This api is not guaranteed to be stable, while core is (or should be!).
; TODO look at potential other libraries for ideas on selector path format.
; TODO is immediate child needed?
; TODO provide configurable keys for path construction?  could conflict.

(defn- debugpath [selector path data]
  (println (str "selector: " selector " path: " path)) 
  (println (str "\tdata:" data)))

(def restv (comp #(if (empty? %) nil %) vec rest))

(defn matches [selector path]
  "see if the selector is matched at the path
   all of the path elements of selector must be found in order in the path
   the selector and the path must also be end with the same element
   this is similar to the old school css selector 'table tr td'
   which matches any td with that parentage even in the case of nested n depth tds"
  (loop [sa path
         sb selector]
    #_(println (str "\t\ttesting selector: " sb " path: " sa))
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

(defn traverse
  "Recursively traverses structure, in place consumes stack.
   Vectors and sequentials preserve order.
   Maps lose any key order which shouldn't be depended on.
   Each level first adds path information, and then applies test.
   If the selector is an empty vector, it matches root object and returns."
  ([mixed selector fx]
    (traverse mixed selector fx []))
  ([mixed selector fx loc]
    (cond
      (empty? selector)
      (fx mixed) 
      (map? mixed)
      (let [nloc (conj loc :.map)
            mfx  #(assoc %1 (first %2) (traverse (second %2) selector fx (conj nloc (first %2))))
            res  (reduce mfx {} mixed)]
        #_(debugpath selector nloc mixed)
        (if (matches selector nloc) (fx res) res))
      (set? mixed)
      (let [nloc (conj loc :.set)
            vfx  #(traverse % selector fx nloc)
            res  (mapv vfx mixed)]
        #_(debugpath selector nloc mixed)
        (if (matches selector nloc) (fx res) res))
      (vector? mixed)
      (let [nloc (conj loc :.vec)
            vfx  #(traverse % selector fx nloc)
            res  (mapv vfx mixed)]
        #_(debugpath selector nloc mixed)
        (if (matches selector nloc) (fx res) res))
      (sequential? mixed)
      (let [nloc (conj loc :.seq)
            sfx  #(conj %1 (traverse %2 selector fx nloc))
            nseq (empty mixed)
            res  (reverse (reduce sfx nseq mixed))]
        #_(debugpath selector nloc mixed)
        (if (matches selector nloc) (fx res) res))
      :else 
      (let [nloc (conj loc :.val)]
        #_(debugpath selector nloc mixed)
        (if (matches selector nloc) (fx mixed) mixed)))))

(defn rev-comp [fns]
  "create the implied order fx"
  (apply comp (reverse fns)))