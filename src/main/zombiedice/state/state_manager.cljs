(ns zombiedice.state.state-manager
  (:require [zombiedice.frontend.kaplay :as kaplay]
            [clojure.string :as str]))

;; Initial state
;; Player 1 takes 3 dice
;; Player 1 rolls 3 dice
;; Player 1 collects any brain dice
;; > If Player has 13 brains they win the game
;; Player 1 collects any shotgun dice
;; > If the player has 3 shotgun blasts they loose the brains they have collected for the current round
;; Player 1 Rerolls if has any footprint dice.
;; > Take extra dice as you must always roll 3 dice.
;; > Player may simply pass, and the turn moves to next player

;; XXX How about using state like this!
;; https://www.learn-clojurescript.com/section-4/lesson-22-managing-state/

(def add
  "adds shit together (add 1 2)"
  [a b]
  (+ a b))
(add 32 34)

(def game-state-key "game-state")

(def default-game-state
  {:current-dice []
   :current-player 0
   :players []
   :remaining-dice []
   :round 0
   :state :initializing})

(defn initialize-game-state! [k]
  (kaplay/add k [default-game-state game-state-key]))

(defn get-game-state [k]
  (first (kaplay/get-children k game-state-key)))

(defn get-players [k]
  ()
  (:players game-state))

(defn get-current-player [game-state]
  (get (get-players game-state) (:player-turn game-state 0)))

(defn add-player [game-state player]
  (let [players (get-players game-state)]
    (assoc game-state :players (conj players player))))

(defn add-dice
  ([game-state dice] (add-dice game-state [] dice))
  ([game-state current-dice remaining-dice]
   (assoc game-state :current-dice current-dice :remaining-dice remaining-dice)))

(defn update-state [game-state state]
  (assoc game-state :state state))

(defn update-state-playing [game-state]
  (update-state game-state :playing))
