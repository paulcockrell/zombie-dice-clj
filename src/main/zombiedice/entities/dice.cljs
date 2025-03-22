(ns zombiedice.entities.dice
  (:require
   [cljs.core :as c]))

(def red-dice
  {:color "red" :faces [:shotgun :shotgun :shotgun :feet :feet :brains]})

(def yellow-dice
  {:color "yellow" :faces [:shotgun :shotgun :feet :feet :brains :brains]})

(def green-dice
  {:color "green" :faces [:shotgun :feet :feet :brains :brains :brains]})

(defn init-dice
  "There are 13 dice to start each play, 3 red, 4 yellow and 6 green. Create shuffled vector"
  []
  (shuffle (concat (repeat 3 red-dice) (repeat 4 yellow-dice) (repeat 6 green-dice))))

(defn take-dice
  "Take n dices from the current dice array"
  [dice-pot count]
  (split-at count dice-pot))

(defn roll-dice
  "Return the dice color and randomly chosen face"
  [d]
  {:color (:color d) :face (rand-nth (d :faces))})

(defn roll-dices [dices]
  (map roll-dice dices))
