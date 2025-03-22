(ns zombiedice.entities.dice
  (:require
   [cljs.core :as c]))

;; 3 red, 4 yellow, 6 green
(defrecord Dice [color num-shotguns num-feet num-brains])
(defn make-dice ([color num-s num-f num-b]
                 (->Dice color num-s num-f num-b)))
(def red-dice (make-dice "red" 3 2 1))
(def yellow-dice (make-dice "yellow" 2 2 2))
(def green-dice (make-dice "green" 1 2 3))

(defn init-dice
  "There are 13 dice to start each play, 3 red, 4 yellow and 6 green"
  []
  (concat (repeat 3 red-dice) (repeat 4 yellow-dice) (repeat 6 green-dice)))

(defn roll-dice [count dice-pot]
  (.log js/console (clj->js dice-pot))
  (repeatedly count #(rand-nth dice-pot)))

(comment (let [dice-pot (init-dice)] (repeatedly 3 #(rand-nth dice-pot))))
