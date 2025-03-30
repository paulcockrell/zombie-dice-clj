(ns zombiedice.entities.dice
  (:require
   [cljs.core :as c]))

(defonce red-dice [:shotgun :shotgun :shotgun :feet :feet :brains])
(defonce yellow-dice [:shotgun :shotgun :feet :feet :brains :brains])
(defonce green-dice [:shotgun :feet :feet :brains :brains :brains])

(defn get-dice
  "Gets a dices faces by color. Can be red yellow or green"
  [dice-color]
  (case dice-color
    :red red-dice
    :yellow yellow-dice
    :green green-dice))

(defn init-dice
  "There are 13 dice to start each play, 3 red, 4 yellow and 6 green. Create shuffled vector"
  []
  (shuffle (concat
            (repeat 3 :red)
            (repeat 4 :yellow)
            (repeat 6 :green))))

(defn take-dice
  "Take n dices from the current dice array"
  [dice-pot count]
  (split-at count dice-pot))

(defn roll-dice
  "Return the dice color and randomly chosen face"
  [d]
  {:color d :face (rand-nth (get-dice d))})

(defn roll-dices [dices]
  (map roll-dice dices))
