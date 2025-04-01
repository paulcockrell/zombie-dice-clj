(ns zombiedice.entities.player)

(defn init-player []
  {:brains 0})

(defn update-brains [player brains]
  (assoc player :brains (+ (player :brains) brains)))
