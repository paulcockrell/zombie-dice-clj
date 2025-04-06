(ns zombiedice.frontend.app
  (:require [zombiedice.entities.dice :as dice]
            [zombiedice.entities.player :as player]
            [reagent.core :as r]
            [reagent.dom.client :as rc]
            [cljs.core :as c]))

(def initial-game-state
  {:current-dice []
   :remaining-dice []
   :players []
   :round 0
   :brains 0
   :shots 0})

(defonce game-state
  (r/atom initial-game-state))

(defn save-game-state! [game-state new-game-state]
  (reset! game-state new-game-state))

(defn update-shots [game-state shots]
  (prn (str "Updating shots " shots))
  (assoc game-state :shots (+ (game-state :shots) shots)))

(defn update-brains [game-state brains]
  (prn (str "Updating brains " brains))
  (assoc game-state :brains (+ (game-state :brains) brains)))

(defn get-players [game-state]
  (:players game-state))

(defn get-current-player [game-state]
  (first (get-players game-state)))

(defn get-current-dice [game-state]
  (:current-dice game-state))

(defn add-player
  "Add player to the game states players key"
  [game-state name]
  (prn (str "Adding player " name))
  (let [players (get-players game-state)]
    (assoc game-state :players (conj players (player/init-player name)))))

(defn add-dice
  ([game-state dice]
   (add-dice game-state [] dice))
  ([game-state current-dice remaining-dice]
   (assoc game-state :current-dice current-dice :remaining-dice remaining-dice)))

(defn roll-dice
  "Take 3 dice from the pot of dice and roll them. Returns list of current
  (rolled) dice and remaining dice."
  [game-state]
  (prn "Rolling dice")
  (let [[current-dice remaining-dice] (dice/take-dice (:remaining-dice game-state) 3)]
    (add-dice game-state (dice/roll-dices current-dice) remaining-dice)))

;; Brain functions

(defn get-brains [game-state]
  (:brains game-state))

(defn get-current-player-brains [game-state]
  (:brains (get-current-player game-state)))

(defn eat-brains [game-state]
  (assoc game-state :brains (+ (get-brains game-state) (dice/count-brains (get-current-dice game-state)))))

(defn reset-brains [game-state]
  (assoc game-state :brains 0))

(defn update-player-brains
  "Saves current round brains to player brain tally and resets round"
  [game-state]
  (let [current-player (get-current-player game-state)
        players (get-players game-state)]
    (assoc game-state :players
           (assoc players 0 (player/add-brains current-player (get-brains game-state))))))

;; Shot functions

(defn get-shots [game-state]
  (:shots game-state))

(defn take-shots! [game-state]
  (let [current-shots (get-shots game-state)
        new-shots (dice/count-shots (get-current-dice game-state))]
    (swap! game-state assoc :shots (+ current-shots new-shots))))

(defn reset-shots [game-state]
  (assoc game-state :shots 0))

;; Player functions

(defn get-current-player-shots [game-state]
  (:shots (get-current-player game-state)))

(defn list-players [game-state]
  (let [players (get-players @game-state)]
    [:div "Players:"
     [:ul
      (for [{:keys [name brains]} players]
        ^{:key (random-uuid)} [:li (str name " has eaten " brains " brains.")])]]))

(defn show-current-player [game-state]
  (let [current-player (get-current-player @game-state)]
    [:div "Current player: "
     (str (:name current-player))]))

(defn show-round-brains [game-state]
  [:div "Current brains eaten: "
   (get-brains @game-state)])

(defn show-round-shots [game-state]
  [:div "Current shots taken: "
   (get-shots @game-state)])

(defn set-next-player-turn
  "Puts the current player (first in the players vector) to the end of the vector"
  [game-state]
  (let [players (get-players game-state)]
    (-> game-state
        (reset-shots)
        (add-dice (dice/init-dice))
        (assoc :players (vec (concat (rest players) [(first players)]))))))

(defn check-has-won? [game-state]
  (let [current-player (get-current-player game-state)]
    (when (<= 13 (:brains current-player))
      (.log js/console "You have won the game huzaa!")
      (assoc game-state :status :won))))

(defn win-round [game-state]
  (update-player-brains game-state))

(defn loose-round [game-state]
  (-> game-state
      (reset-shots)
      (reset-brains)))

(defn process-hand [game-state]
  (-> game-state
      (roll-dice)
      (update-shots (get-shots game-state))
      (update-brains (get-brains game-state))))

(defn check-hand [game-state]
  (if (<= 3 (get-shots game-state))
    (loose-round game-state)
    (win-round game-state)))

(defn play-hand! [game-state]
  (let [new-game-state
        (-> @game-state
            (process-hand)
            (check-hand))]
    (save-game-state! game-state new-game-state)))

(defn yield-turn! [game-state]
  (let [new-game-state (set-next-player-turn @game-state)]
    (save-game-state! game-state new-game-state)))

(defn reset-game!
  "Reset the game state"
  [game-state]
  (let [new-game-state
        (-> initial-game-state
            (add-player "Paul")
            (add-player "Bob")
            (add-dice (dice/init-dice)))]
    (save-game-state! game-state new-game-state)))

(defn list-current-dice [game-state]
  (prn "Current dice " (clj->js (get-current-dice @game-state)))
  (let [dices (get-current-dice @game-state)]
    [:ul
     (for [dice dices]
       ^{:key (random-uuid)} [:li (str "Color: " (:color dice) ", face: " (:face dice))])]))

(defn list-remaining-dice [game-state]
  (let [dices (:remaining-dice @game-state)]
    [:div "Remaining dice: "
     [:ul
      (for [dice dices]
        ^{:key (random-uuid)} [:li dice])]]))

(defn show-current-hand [game-state]
  [:div "Current hand: "
   (list-current-dice game-state)])

(defn reset-game [game-state]
  [:button {:on-click (fn [] (reset-game! game-state))}
   "Restart"])

(defn app []
  [:div
   [:button {:on-click (fn [] (play-hand! game-state))}
    "Roll dice..."]
   [:button {:on-click (fn [] (yield-turn! game-state))}
    "Yield turn"]
   [reset-game game-state]
   [show-current-hand game-state]
   [list-remaining-dice game-state]
   [list-players game-state]
   [show-current-player game-state]
   [show-round-brains game-state]
   [show-round-shots game-state]])

(defn mount-root []
  (let [root (rc/create-root (.getElementById js/document "root"))]
    (rc/render root (app))))

(defn init []
  (reset-game! game-state)
  (mount-root)
  (.log js/console "Zombie Dice initialized"))

