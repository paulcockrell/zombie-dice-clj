(ns zombiedice.frontend.app
  (:require [zombiedice.entities.dice :as dice]
            [zombiedice.entities.player :as player]
            [reagent.core :as r]
            [reagent.dom.client :as rc]
            [cljs.core :as c]))

(defonce game-state
  (r/atom
   {:current-dice []
    :remaining-dice []
    :players []
    :round 0
    :state :initializing}))

(defn get-current-player [game-state]
  (first (:players @game-state)))

(defn get-players [game-state]
  (:players @game-state))

(defn add-player!
  "Add player to the game states players key"
  [game-state name]
  (prn (str "Adding player " name))
  (let [players (get-players game-state)]
    (swap! game-state assoc :players (conj players (player/init-player name)))))

(defn add-dice!
  ([game-state dice]
   (add-dice! game-state [] dice))
  ([game-state current-dice remaining-dice]
   (swap! game-state assoc :current-dice current-dice)
   (swap! game-state assoc :remaining-dice remaining-dice)))

(defn reset-game!
  "Reset the game state"
  [game-state]
  (add-player! game-state "Paul")
  (add-player! game-state "Bob")
  (add-dice! game-state (dice/init-dice)))

(defn roll
  "Take 3 dice from the pot of dice and roll them. Returns list of current
  (rolled) dice and remaining dice. Updates the game state"
  [game-state]
  (let [[current-dice remaining-dice] (dice/take-dice (:remaining-dice @game-state) 3)]
    (add-dice! game-state (dice/roll-dices current-dice) remaining-dice)))

(defn list-current-dice [game-state]
  (let [dices (:current-dice @game-state)]
    [:ul
     (for [dice dices]
       ^{:key (random-uuid)} [:li (str "Color: " (:color dice) ", face: " (:face dice))])]))

(defn list-remaining-dice [game-state]
  (let [dices (:remaining-dice @game-state)]
    [:ul
     (for [dice dices]
       ^{:key (random-uuid)} [:li dice])]))

(defn count-shots [game-state]
  (count (filter (fn [x]
                   (= :shotgun (:face x))) (:current-dice @game-state))))

(defn count-brains [game-state]
  (count (filter (fn [x]
                   (= :brains (:face x))) (:current-dice @game-state))))

(defn eat-brains! [game-state]
  (let [current-player (get-current-player game-state) players (get-players game-state)]
    (swap! game-state assoc :players
           (assoc players 0 (player/update-brains current-player (count-brains game-state))))))

(defn reset-brains! [game-state]
  (let [current-player (get-current-player game-state) players (get-players game-state)]
    (swap! game-state assoc :players
           (assoc players 0 (player/update-brains current-player 0)))))

(defn get-shot! [game-state]
  (let [current-player (get-current-player game-state) players (get-players game-state)]
    (swap! game-state assoc :players
           (assoc players 0 (player/update-shots current-player (count-shots game-state))))))

(defn reset-shots! [game-state]
  (let [current-player (get-current-player game-state) players (get-players game-state)]
    (swap! game-state assoc :players
           (assoc players 0 (player/update-shots current-player 0)))))

(defn get-current-brains [game-state]
  (:brains (get-current-player game-state)))

(defn get-current-shots [game-state]
  (:shots (get-current-player game-state)))

(defn list-players [game-state]
  (let [players (get-players game-state)]
    [:ul
     (for [{:keys [name]} players]
       ^{:key (random-uuid)} [:li name])]))

(defn show-current-player [game-state]
  (let [current-player (get-current-player game-state)]
    (str (:name current-player))))

(defn set-next-player-turn!
  "Puts the current player (first in the players vector) to the end of the vector"
  [game-state]
  (let [players (get-players game-state)]
    (reset-shots! game-state) ;; reset current player shot counts as they are yielding their turn
    (add-dice! game-state (dice/init-dice)) ;; reset the dice
    (swap! game-state assoc :players (vec (concat (rest players) [(first players)])))
    (prn (clj->js @game-state))))

(defn check-has-won? [game-state]
  (let [current-player (get-current-player game-state)]
    (if (<= 13 (:brains current-player))
      (.log js/console "You have won the game huzaa!")
      (.log js/console "You still need to eat 13 brains to win"))))

(defn check-too-many-shots? [game-state]
  (let [current-player (get-current-player game-state)]
    (if (<= 3 (:shots current-player))
      (do
        (.log js/console "Oh no you've been shot too many times, you will loose all your brains")
        (reset-brains! game-state)
        (reset-shots! game-state)
        (set-next-player-turn! game-state))
      (.log js/console "You're still un-dead, be careful not to get shot 3 times!"))))

(defn app []
  [:div
   [:button {:on-click (fn []
                         (roll game-state)
                         (eat-brains! game-state)
                         (get-shot! game-state)
                         (check-too-many-shots? game-state)
                         (check-has-won? game-state))}
    "Roll dice..."]
   [:button {:on-click (fn [] (set-next-player-turn! game-state))}
    "Yield turn"]
   [:button {:on-click (fn [] (reset-game! game-state))}
    "Restart"]
   [:div  "Hand " [list-current-dice game-state]]
   [:div "Pot " [list-remaining-dice game-state]]
   [:div "Players: " [list-players game-state]]
   [:div "Player: " [show-current-player game-state]]
   [:div "Brains: " [get-current-brains game-state]]
   [:div "Shots: " [get-current-shots game-state]]])

(defn mount-root []
  (let [root (rc/create-root (.getElementById js/document "root"))]
    (rc/render root (app))))

(defn init []
  (reset-game! game-state)
  (mount-root)
  (.log js/console "Zombie Dice initialized"))

