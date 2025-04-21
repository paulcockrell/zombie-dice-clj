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

(defn list-current-dice [game-state]
  (let [dices (state/get-current-dice @game-state)]
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
  [:button.button.is-danger {:on-click (fn [] (reset-game! game-state))}
   "Restart"])

(defn yield-turn-btn [game-state]
  [:button.button.is-warning {:on-click
                              (fn []
                                (yield-turn! game-state))
                              :disabled
                              (= 0 (count (state/get-players @game-state)))}
   "Yield turn"])

(defn roll-dice-btn [game-state]
  [:button.button.is-primary {:on-click (fn [] (play-turn! game-state)) :disabled (or
                                                                                   (= 0 (count (state/get-active-players @game-state)))
                                                                                   (= 0 (count (:remaining-dice @game-state))))}
   "Roll dice..."])

(defn navbar-component []
  [:nav
   {:class "navbar py-4"}
   [:div
    {:class "container is-fluid"}
    [:div
     {:class "navbar-brand"}
     [:a
      {:class "navbar-item", :href "#"}
      [:img
       {:class "image",
        :src "https://bulma.io/images/bulma-logo.png",
        :alt "",
        :width "96px"}]]
     [:a
      {:class "navbar-burger",
       :role "button",
       :aria-label "menu",
       :aria-expanded "false"}
      [:span {:aria-hidden "true"}]
      [:span {:aria-hidden "true"}]
      [:span {:aria-hidden "true"}]]]
    [:div
     {:class "navbar-menu"}
     [:div
      {:class "navbar-start"}
      [:a {:class "navbar-item", :href "#"} "About"]
      [:a {:class "navbar-item", :href "#"} "Company"]
      [:a {:class "navbar-item", :href "#"} "Services"]
      [:a {:class "navbar-item", :href "#"} "Testimonials"]]
     [:div
      {:class "navbar-item"}
      [:div
       {:class "field has-addons"}
       [:div
        {:class "control"}
        [:input
         {:class "input",
          :type "search",
          :placeholder "Search",
          :aria-label "Search"}]]
       [:div
        {:class "control"}
        [:button
         {:class "button", :type "submit"}
         [:svg
          {:xmlns "http://www.w3.org/2000/svg",
           :fill "none",
           :viewBox "0 0 24 24",
           :stroke "currentColor",
           :style {:width "24px", :height "24px"}}
          [:path
           {:stroke-linecap "round",
            :stroke-linejoin "round",
            :stroke-width "2",
            :d "M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"}]]]]]]]]])

(defn update-player-list [game-state new-player]
  (let [new-game-state
        (state/add-player @game-state @new-player)]
    (state/save-game-state! game-state new-game-state)))

(defn player-input [value]
  [:input {:type "text"
           :value @value
           :class "input"
           :placeholder "Player name"
           :on-change #(reset! value (-> % .-target .-value))
           :on-key-press
           (fn [e]
             (when (= (.-key e) "Enter")
               (update-player-list state/game-state value)
               (reset! value "")))}])

(defn player-input-save [game-state value]
  [:button.button.is-primary {:on-click
                              (fn []
                                (update-player-list game-state value)
                                (reset! value ""))}
   "Add player"])

;; (defn app []
;;   [:div
;;    [:div.columns.is-multiline]
;;    [navbar-component]
;;    [:section.section
;;     [:div.container
;;      [header-component state/game-state]]
;;     [:div.columns.is-multiline
;;      [current-player-component state/game-state]
;;      [mobile-component]
;;      [list-players-component state/game-state]]]
;;    [footer-component]
;;    [:section.section
;;     [:div.buttons
;;      [roll-dice-btn state/game-state]
;;      [yield-turn-btn state/game-state]
;;      [reset-game-btn state/game-state]]]
;;    [:section.section
;;     [state/show-current-player state/game-state]
;;     [state/show-round-brains state/game-state]
;;     [state/show-round-shots state/game-state]]
;;    [:section.section
;;     [show-current-hand state/game-state]
;;     [list-remaining-dice state/game-state]
;;     [state/list-players state/game-state]]])

;; new design start

(defn score-board-table
  "Render a table of players in the game with their accumulated brain
  consumption tally"
  [game-state]
  (let [players (state/get-players @game-state)]
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
          (let [tr-class (if (= 1 position) "border-b bg-primary/10" "border-b")]
            [:tr {:key name :class tr-class}
             [:td {:class "p-2 align-middle font-medium"} name]
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
  [:div
   {:class "grid grid-cols-2 grid-rows-2 gap-0"}
   [:div
    {:class "relative z-30 flex flex-1 flex-col justify-center gap-1 border-t rounded-tl-lg border-primary text-left even:border sm:border sm:px-4 sm:py-2"}
    [:span
     {:class "text-xs text-gray-600"} "Round"]
    [:span
     {:class "text-lg font-bold leading-none sm:text-lg text-center"} "5"]]
   [:div
    {:class "relative z-30 flex flex-1 flex-col justify-center gap-1 border-t rounded-tr-lg border-primary text-left sm:border sm:border-l-0 sm:px-4 sm:py-2"}
    [:span
     {:class "text-xs text-gray-600"} "Turn"]
    [:span
     {:class "text-lg font-bold leading-none sm:text-lg text-center"} "2 of 3"]]
   [:div
    {:class "relative z-30 flex flex-1 flex-col justify-center gap-1 border-t rounded-bl-lg border-primary text-left sm:border sm:border-t-0 sm:px-4 sm:py-2"}
    [:span
     {:class "text-xs text-gray-600"} "Throw"]
    [:span
     {:class "text-lg font-bold leading-none sm:text-lg text-center"} "1"]]
   [:div
    {:class "relative z-30 flex flex-1 flex-col justify-center gap-1 border-t rounded-br-lg border-primary text-left sm:border sm:border-t-0 sm:border-l-0 sm:px-4 sm:py-2"}
    [:span
     {:class "text-xs text-gray-600"} "Dice remaining"]
    [:span
     {:class "text-lg font-bold leading-none sm:text-lg text-center"} "6"]]])

(defn game-ui-component [game-state]
  (when (not= (state/get-action game-state) :adding-players)
    [:<>
     [stats-component game-state]

     [components/card
      [:<>
       [components/section-title "Current zombie - Moss"]
       [current-round-stats-table game-state]]]

     [components/card
      [:<>
       [components/section-title "Dice thrown"]
       [:div {:class "flex justify-center gap-4 text-4xl"}
        [:img {:src "/images/dice-green-brains.png"
               :alt "Descriptive text"
               :style {:width "80px"
                       :height "auto"}}]
        [:img {:src "/images/dice-yellow-footsteps.png"
               :alt "Descriptive text"
               :style {:width "80px"
                       :height "auto"}}]
        [:img {:src "/images/dice-red-explosion.png"
               :alt "Descriptive text"
               :style {:width "80px"
                       :height "auto"}}]]]]

     [:div {:class "flex flex-col sm:flex-row gap-2 justify-around"}
      [components/button {:label "Roll dice"
                          :variant :primary
                          :disabled (= (:action @game-state) :turn-over)
                          :on-click #(play-turn! game-state)}]
      [components/button {:label "Yield turn"
                          :variant :outline
                          :on-click #(yield-turn! game-state)}]]]))

(defn players-ui-component [game-state]
  [components/card
   [:<>
    [components/section-title "Score board"]
    [components/section-subtitle "The first to eat 13 brains wins the game"]
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
  (js/console.log "stop"))
