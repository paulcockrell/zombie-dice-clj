(ns zombiedice.frontend.app
  (:require [zombiedice.entities.dice :as dice]
            [zombiedice.entities.player :as player]
            [reagent.core :as r]
            [reagent.dom.client :as rc]))

(defonce game-state
  (r/atom
   {:current-dice []
    :remaining-dice []
    :current-player 0
    :players []
    :round 0
    :state :initializing}))

(defn add-player!
  "Add player to the game states players key"
  [game-state player]
  (swap! game-state update-in [:players] conj player))

(defn add-dice!
  ([game-state dice]
   (add-dice! game-state [] dice))
  ([game-state current-dice remaining-dice]
   (swap! game-state assoc :current-dice current-dice)
   (swap! game-state assoc :remaining-dice remaining-dice)))

(defn roll
  [game-state]
  (let [[current-dice remaining-dice] (dice/take-dice (:remaining-dice @game-state) 3)]
    (add-dice! game-state current-dice remaining-dice)
    (.log js/console (str "XXX current dice: " current-dice ", rolled  dice: " (dice/roll-dices current-dice)))))

(defn state-ful-with-atom []
  [:div {:on-click (fn [] (roll game-state))}
   "Roll dice..."])

(defn mount-root []
  (let [root (rc/create-root (.getElementById js/document "root"))]
    (rc/render root (state-ful-with-atom))))

(defn init []
  (add-player! game-state (player/init-player "Paul"))
  (add-dice! game-state (dice/init-dice))

  (mount-root)

  (.log js/console (clj->js @game-state))
  (.log js/console "Zombie Dice initialized"))

