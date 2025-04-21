(ns zombiedice.state.state-manager
  [:require
   [zombiedice.entities.dice :as dice]
   [zombiedice.entities.player :as player]
   [reagent.core :as r]])

;; Basic state rules
;;
;; Player 1 rolls 3 dice
;; Player 1 collects any brain dice
;; > If Player has 13 brains they win the game
;; Player 1 collects any shotgun dice
;; > If the player has 3 shotgun blasts they loose the brains they have collected for the current round
;; Player 1 Rerolls if has any footprint dice.
;; > Take extra dice as you must always roll 3 dice.
;; > Player may simply pass, and the turn moves to next player

(def initial-game-state
  {:current-dice []
   :remaining-dice []
   :action :adding-players
   :players []
   :round 0
   :throws () ;; list of maps recording dice faces
   :brains 0
   :shots 0})

(def allowed-actions #{:adding-players :in-game :game-over})

(def transition-rules
  {:adding-players #{:in-game}
   :in-game        #{:game-over}
   :game-over      #{:adding-players}})

(defn valid-transition? [from to]
  (contains? (get transition-rules from #{}) to))

(defn set-action! [game-state new-action]
  (let [current-action (:action @game-state)]
    (cond
      (not (allowed-actions new-action))
      (prn "Invalid action" new-action)

      (not (valid-transition? current-action new-action))
      (prn "Invalid state transition:" current-action "→" new-action)

      :else
      (swap! game-state assoc :action new-action))))

(defonce game-state
  (r/atom initial-game-state))

(defn save-game-state! [game-state new-game-state]
  (reset! game-state new-game-state))

(defn get-players [game-state]
  (:players game-state))

(defn get-active-players [game-state]
  (filter (fn [p] (> 13 (:brains p))) (get-players game-state)))

(defn get-current-player [game-state]
  (first (get-players game-state)))

(defn get-current-dice [game-state]
  (:current-dice game-state))

(defn invalid-name-size? [name]
  (or
   (< (count name) 2)
   (> (count name) 10)))

(defn name-taken? [game-state name]
  (some (partial = name) (map :name (get-players game-state))))

(defn max-players? [game-state]
  (<= 5 (count (get-players game-state))))

(defn valid-name? [game-state name]
  (cond
    (invalid-name-size? name) false
    (name-taken? game-state name) false
    (max-players? game-state) false
    :else true))

(defn add-player
  "Add player to the game states players key"
  [game-state name]
  (if (valid-name? game-state name)
    (let [players (get-players game-state)
          position (+ 1 (count players))
          new-player (player/init-player :name name :position position)]
      (assoc game-state :players (conj players new-player)))
    (do (prn "Invalid name. Must be between 2 and 10 characters and not be taken.")
        game-state)))

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

(defn record-throw [game-state]
  (let [dice (get-current-dice game-state)
        feet-count (dice/count-feet dice)
        shot-count (dice/count-shots dice)
        brain-count (dice/count-brains dice)
        current-throws (:throws game-state)
        new-throw {:throw (+ 1 (count current-throws)) :feet feet-count :shots shot-count :brains brain-count}]
    (assoc game-state :throws (cons new-throw current-throws))))

(defn get-throws [game-state]
  (:throws @game-state))

(defn- merge-sum [maps]
  (reduce (fn [acc m]
            (merge-with + acc m))
          {}
          maps))

(defn get-throw-totals [game-state]
  (merge-sum (:throws @game-state)))

(defn process-hand [game-state]
  (-> game-state
      (roll-dice)
      (record-throw)
      (update-round-shots)
      (update-round-brains)))

(defn move-current-player-to-last [game-state]
  (let [players (get-active-players game-state)]
    (assoc game-state :players (vec (concat (rest players) [(first players)])))))

(defn update-round-counter [game-state]
  (assoc game-state :round (inc (:round game-state))))

(defn loose-round [game-state]
  (prn "Oh no you got shot too many times, you loose all your brains from this round!")
  (-> game-state
      (move-current-player-to-last)
      (update-round-counter)
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
