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
  (rolled) dice and remaining dice. If your current dice have 'feet' these will
  be kept for the next roll and the others replaced from the pot"
  [game-state]

  (let [last-round-feet-dice (dice/filter-feet (:current-dice game-state))
        number-of-new-dices-to-take (if (< 0 (count last-round-feet-dice))
                                      (- 3 (count last-round-feet-dice))
                                      3)
        [current-dice remaining-dice] (dice/take-dice (:remaining-dice game-state) number-of-new-dices-to-take)
        new-dice (into current-dice (dice/get-colors last-round-feet-dice))]
    (add-dice game-state (dice/roll-dices new-dice) remaining-dice)))

;; Brain functions

(defn get-round-brains [game-state]
  (:brains game-state))

(defn get-current-player-brains [game-state]
  (:brains (get-current-player game-state)))

(defn update-round-brains [game-state]
  (let [current-brains (get-round-brains game-state)
        new-brains (dice/count-brains (get-current-dice game-state))]
    (assoc game-state :brains (+ current-brains new-brains))))

(defn reset-brains [game-state]
  (assoc game-state :brains 0))

(defn update-player-brains
  "Saves current round brains to player brain tally"
  [game-state]
  (let [current-player (get-current-player game-state)
        players (get-players game-state)
        brains (get-round-brains game-state)]
    (assoc game-state :players
           (assoc players 0 (player/add-brains current-player brains)))))

;; Shot functions

(defn get-shots [game-state]
  (:shots game-state))

(defn update-round-shots [game-state]
  (let [current-shots (get-shots game-state)
        new-shots (dice/count-shots (get-current-dice game-state))]
    (assoc game-state :shots (+ current-shots new-shots))))

(defn reset-shots [game-state]
  (assoc game-state :shots 0))

;; Player functions

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
   (get-round-brains @game-state)])

(defn show-round-shots [game-state]
  [:div "Current shots taken: "
   (get-shots @game-state)])

(defn process-hand [game-state]
  (-> game-state
      (roll-dice)
      (update-round-shots)
      (update-round-brains)))

(defn move-current-player-to-last [game-state]
  (let [players (get-players game-state)]
    (assoc game-state :players (vec (concat (rest players) [(first players)])))))

(defn yield-turn! [game-state]
  (let [new-game-state
        (-> @game-state
            (update-player-brains)
            (move-current-player-to-last)
            (reset-shots)
            (reset-brains)
            (add-dice (dice/init-dice)))]
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

(defn loose-round [game-state]
  (prn "Oh no you got shot too many times, you loose all your brains from this round!")
  (-> game-state
      (move-current-player-to-last)
      (reset-shots)
      (reset-brains)
      (add-dice (dice/init-dice))))

(defn win-round [game-state]
  (prn (str "Player " (:name (get-current-player game-state)) " has won the game!"))
  game-state)

(defn check-hand [game-state]
  (let [player-total-brains (+ (get-current-player-brains game-state) (get-round-brains game-state))
        shots (get-shots game-state)]
    (cond
      (<= 3 shots) (loose-round game-state)
      (<= 13 player-total-brains) (win-round game-state)
      :else game-state)))

(defn play-hand! [game-state]
  (let [new-game-state
        (-> @game-state
            (process-hand)
            (check-hand))]
    (save-game-state! game-state new-game-state)))

(defn list-current-dice [game-state]
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

(defn reset-game-btn [game-state]
  [:button {:on-click (fn [] (reset-game! game-state))}
   "Restart"])

(defn yield-turn-btn [game-state]
  [:button {:on-click (fn [] (yield-turn! game-state))}
   "Yield turn"])

(defn roll-dice-btn [game-state]
  [:button {:on-click (fn [] (play-hand! game-state))}
   "Roll dice..."])

(defn app []
  [:div
   [roll-dice-btn game-state]
   [yield-turn-btn game-state]
   [reset-game-btn game-state]
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

