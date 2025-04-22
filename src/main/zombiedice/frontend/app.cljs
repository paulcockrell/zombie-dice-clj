(ns zombiedice.frontend.app
  (:require [zombiedice.entities.dice :as dice]
            [reagent.core :as r]
            [reagent.dom.client :as rc]
            [zombiedice.frontend.components :as components]
            [zombiedice.state.state-manager :as state]
            [cljs.core :as c]))

(defn yield-turn! [game-state]
  (let [new-game-state
        (-> @game-state
            (state/update-player-brains)
            (state/move-current-player-to-last)
            (state/reset-throws)
            (state/update-round-counter)
            (state/add-dice (dice/init-dice))
            (state/set-action :in-game))]
    (state/save-game-state! game-state new-game-state)))

(defn reset-game! [game-state]
  (let [new-game-state
        (-> state/initial-game-state
            (state/add-dice (dice/init-dice)))]
    (state/save-game-state! game-state new-game-state)))

(defn play-turn! [game-state]
  (let [new-game-state
        (-> @game-state
            (state/process-hand)
            (state/check-hand))]
    (state/save-game-state! game-state new-game-state)))

(defn looser-alert-component [game-state]
  [:div
   {:class "bg-secondary/50 border-b border-secondary/40 text-amber-700 text-sm p-4 flex justify-between"}
   [:div
    [:div.flex.items-center
     [:span.pr-4 "💥"]
     [:p
      (str (:name (state/get-current-player @game-state)) " has been shot too many times!")]]]])

(defn winner-alert-component [game-state]
  [:div
   {:class "bg-primary/50 border-b border-primary/40 text-lime-700 text-sm p-4 flex justify-between"}
   [:div
    [:div.flex.items-center
     [:span.pr-4 "🧟‍♂️"]
     [:p
      (str (:name (state/get-current-player @game-state)) " has won!")]]]])

(defn score-board-table
  "Render a table of players in the game with their accumulated brain
  consumption tally"
  [game-state]
  (let [players (state/get-players-sorted @game-state)
        current-player (state/get-current-player @game-state)]
    (if (< 0 (count players))
      [:table {:class "w-full caption-bottom text-sm"}
       [:thead {:class "[&_tr]:border-b"}
        [:tr {:class "border-b transition-colors hover:bg-muted/50 data-[state=selected]:bg-muted"}
         [:th {:class "h-10 px-2 text-left align-middle font-medium text-muted-foreground w-[100px]"}
          "Name"]
         [:th {:class "h-10 px-2 align-middle font-medium text-muted-foreground text-right"}
          "Position"]
         [:th {:class "h-10 px-2 align-middle font-medium text-muted-foreground text-right"}
          "Rank"]
         [:th {:class "h-10 px-2 align-middle font-medium text-muted-foreground text-right"}
          "Brains"]]]
       [:tbody {:class "[&_tr:last-child]:border-0"}
        (for [{:keys [name position brains]} players]
          (let [is-current-player? (= name (:name current-player))
                tr-class (if is-current-player? "border-b bg-primary/10" "border-b")
                player-name (if is-current-player? (str name " 🎲") name)]
            [:tr {:key name :class tr-class}
             [:td {:class "p-2 align-middle font-medium"} player-name]
             [:td {:class "p-2 align-middle text-right"} position]
             [:td {:class "p-2 align-middle text-right"} 0]
             [:td {:class "p-2 align-middle text-right"} brains]]))]]
      [:table {:class "w-full caption-bottom text-sm"}
       [:tbody {:class "[&_tr:last-child]:border-0"}
        [:tr {:class "border-b"}
         [:td "No players - add between 2 and 5 to start"]]]])))

(defn current-round-stats-table [game-state]
  (let [throws (state/get-throws game-state)
        throw-totals (state/get-throw-totals @game-state)]
    [:table {:class "w-full caption-bottom text-sm"}
     [:thead {:class "[&_tr]:border-b"}
      [:tr {:class "border-b transition-colors hover:bg-muted/50 data-[state=selected]:bg-muted"}
       [:th {:class "h-10 px-2 text-left align-middle font-medium text-muted-foreground w-[100px]"}
        "Throw"]
       [:th {:class "h-10 px-2 align-middle font-medium text-muted-foreground text-right"}
        "Feet"]
       [:th {:class "h-10 px-2 align-middle font-medium text-muted-foreground text-right"}
        "Shotguns"]
       [:th {:class "h-10 px-2 align-middle font-medium text-muted-foreground text-right"}
        "Brains"]]]
     [:tbody {:class "[&_tr:last-child]:border-0"}
      (for [{:keys [throw feet shots brains]} throws]
        ^{:key (random-uuid)} [:tr {:class "border-b transition-colors hover:bg-muted/50 data-[state=selected]:bg-muted"}
                               [:td {:class "p-2 align-middle font-medium"} throw]
                               [:td {:class "p-2 align-middle text-right"} feet]
                               [:td {:class "p-2 align-middle text-right"} shots]
                               [:td {:class "p-2 align-middle text-right"} brains]])]
     [:tfoot {:class "border-t bg-primary/10 font-medium [&>tr]:last:border-b-0"}
      [:tr {:class "border-b"}
       [:td {:class "p-2 align-middle"} "Total"]
       [:td {:class "p-2 align-middle text-right"} (:feet throw-totals)]
       [:td {:class "p-2 align-middle text-right"} (:shots throw-totals)]
       [:td {:class "p-2 align-middle text-right"} (:brains throw-totals)]]]]))

(defn update-players [game-state new-player]
  (let [new-game-state
        (state/add-player @game-state @new-player)]
    (state/save-game-state! game-state new-game-state)))

(defn add-player-component
  [game-state]
  (let [name (r/atom "")]
    (fn []
      (when (= (:action @game-state) :adding-players)
        [:<>
         [components/divider-horizontal]
         [:div
          {:class "grid grid-flow-col grid-rows-1 gap-4"}
          [components/input {:placeholder "Player name"
                             :value @name
                             :on-change #(reset! name (-> % .-target .-value))
                             :on-key-press
                             (fn [e]
                               (when (= (.-key e) "Enter")
                                 (update-players game-state name)
                                 (reset! name "")))}]
          [components/button {:label "Add"
                              :variant :primary
                              :on-click
                              (fn []
                                (update-players game-state name)
                                (reset! name ""))}]]]))))

(defn start-game-component
  [game-state]
  [components/button {:label (if (= (:action @game-state) :adding-players) "Start game" "Restart game")
                      :full-width true
                      :disabled (< (count (state/get-players @game-state)) 2)
                      :on-click #(state/start-game! game-state)}])

(defn stats-component
  [game-state]
  (let [round (state/get-round @game-state)
        players-count (count (state/get-players @game-state))
        current-player (state/get-current-player @game-state)
        player-position (:position current-player)
        remaining-dice (count (:remaining-dice @game-state))]
    [:div
     {:class "grid grid-cols-2 grid-rows-2 gap-0"}
     [:div
      {:class "relative z-30 flex flex-1 flex-col justify-center gap-1 border-t rounded-tl-lg border-primary text-left even:border sm:border sm:px-4 sm:py-2"}
      [:span
       {:class "text-xs text-gray-600"} "Round"]
      [:span
       {:class "text-lg font-bold leading-none sm:text-lg text-center"} round]]
     [:div
      {:class "relative z-30 flex flex-1 flex-col justify-center gap-1 border-t rounded-tr-lg border-primary text-left sm:border sm:border-l-0 sm:px-4 sm:py-2"}
      [:span
       {:class "text-xs text-gray-600"} "Turn"]
      [:span
       {:class "text-lg font-bold leading-none sm:text-lg text-center"} (str player-position " of " players-count)]]
     [:div
      {:class "relative z-30 flex flex-1 flex-col justify-center gap-1 border-t rounded-bl-lg border-primary text-left sm:border sm:border-t-0 sm:px-4 sm:py-2"}
      [:span
       {:class "text-xs text-gray-600"} "Throw"]
      [:span
       {:class "text-lg font-bold leading-none sm:text-lg text-center"} (count (:throws @game-state))]]
     [:div
      {:class "relative z-30 flex flex-1 flex-col justify-center gap-1 border-t rounded-br-lg border-primary text-left sm:border sm:border-t-0 sm:border-l-0 sm:px-4 sm:py-2"}
      [:span
       {:class "text-xs text-gray-600"} "Dice remaining"]
      [:span
       {:class "text-lg font-bold leading-none sm:text-lg text-center"} remaining-dice]]]))

(defn current-dice-component [game-state]
  (let [dices (state/get-current-dice @game-state)]
    [:div {:class "flex justify-center gap-4 text-4xl"}
     (for [dice dices]
       ^{:key (random-uuid)} [:img {:src (str "/images/dice-" (name (:color dice)) "-" (name (:face dice)) ".png")
                                    :alt "Descriptive text"
                                    :style {:width "80px"
                                            :height "auto"}}])]))

(defn game-ui-component [game-state]
  (when (not= (state/get-action game-state) :adding-players)
    [:<>
     [stats-component game-state]

     (let [player-name (:name (state/get-current-player @game-state) "n/a")]
       [components/card
        [:<>
         [components/section-title (str "Current zombie - " player-name)]
         [current-round-stats-table game-state]]])

     [components/card
      [:<>
       [components/section-title "Dice thrown"]
       [current-dice-component game-state]]]

     (when (= (state/get-action game-state) :turn-over)
       [looser-alert-component game-state])

     (when (= (state/get-action game-state) :game-over)
       [winner-alert-component game-state])

     [:div {:class "flex flex-col sm:flex-row gap-2 justify-around"}
      [components/button {:label "Roll dice"
                          :variant :primary
                          :disabled (or (= (:action @game-state) :turn-over) (<= (count (:remaining-dice @game-state)) 0) (= (state/get-action game-state) :game-over))
                          :on-click #(play-turn! game-state)}]
      [components/button {:label "Yield turn"
                          :variant :outline
                          :disabled (= (state/get-action game-state) :game-over)
                          :on-click #(yield-turn! game-state)}]]]))

(defn players-ui-component [game-state]
  [components/card
   [:<>
    [components/section-title "Score board"]
    [components/section-subtitle "The first to eat at least 13 brains wins the game"]
    [score-board-table game-state]
    [add-player-component game-state]]])

(defn zombie-dice-ui [game-state]
  [:div {:class "p-4 space-y-4 max-w-md mx-auto"}
   [:h1 {:class "text-xl font-bold text-center"} "Zombie Dice"]
   [players-ui-component game-state]
   [start-game-component game-state]
   [game-ui-component game-state]])

(defn mount-root []
  (let [root (rc/create-root (.getElementById js/document "root"))]
    (rc/render root (zombie-dice-ui state/game-state))))

;; start is called by init and after code reloading finishes
(defn ^:dev/after-load start []
  (prn "Zombie Dice starting")
  (reset-game! state/game-state)
  (mount-root))

(defn init []
  ;; init is called ONCE when the page loads
  ;; this is called in the index.html and must be exported
  ;; so it is available even in :advanced release builds
  (start))

;; this is called before any code is reloaded
(defn ^:dev/before-load stop []
  (prn "stop"))
