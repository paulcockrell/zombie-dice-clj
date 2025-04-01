(ns zombiedice.entities.player)

(defn init-player []
  {:brains 0 :shots 0})

(defn update-brains [player brains]
  (assoc player :brains (+ (player :brains) brains)))

(defn update-shots [player shots]
  (assoc player :shots (+ (player :shots) shots)))
