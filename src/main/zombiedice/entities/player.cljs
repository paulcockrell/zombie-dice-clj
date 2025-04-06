(ns zombiedice.entities.player)

(defn init-player [name]
  {:name name :brains 0})

(defn add-brains [player brains]
  (assoc player :brains (+ (player :brains) brains)))
