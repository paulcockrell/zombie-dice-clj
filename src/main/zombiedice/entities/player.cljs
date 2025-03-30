(ns zombiedice.entities.player)

(defn init-player [name]
  {:brains 0 :name name})

(defn inc-round [player]
  (assoc player :round (inc (player :round))))

(defn update-brains [player brains]
  (assoc player :brains (+ (player :brains) brains)))
