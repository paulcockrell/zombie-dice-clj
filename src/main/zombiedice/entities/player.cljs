(ns zombiedice.entities.player)

(defn init-player
  [& {:keys [name position]}]
  {:name name :position position :brains 0})

(defn add-brains [player brains]
  (assoc player :brains (+ (player :brains) brains)))

(defn set-position [player position]
  (assoc player :position position))
