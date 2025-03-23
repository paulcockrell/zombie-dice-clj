(ns zombiedice.state.state-manager
  (:require [zombiedice.frontend.kaplay :as kaplay]))

(def default-game-state
  {:players []
   :player-turn 0
   :round 0
   :current-dice []
   :remaining-dice []
   :state :initializing})

(defn get-players [game-state]
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
