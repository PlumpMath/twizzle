(ns eu.cassiel.twizzle.interpolators
  "Some simple interpolators.")

(defn interp-default
  "Default interpolation. Treats first value of `nil` as `0`."
  [val-1 val-2 pos]
  (let [val-1 (or val-1 0)]
    (+ val-1 (* (- val-2 val-1) pos))))

(defn interp-vectors
  "Interpolate between vectors (in the informal sense; is also happy with sequences).
   This takes a function to interpolate the elements.

   A nil first vector is treated as `(repeat 0)`."
  [f v1 v2 p]
  (->> (interleave (or v1 (repeat 0)) v2)
       (partition 2)
       (map (fn [[x y]] (f x y p)))))
