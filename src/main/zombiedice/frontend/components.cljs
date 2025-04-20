(ns zombiedice.frontend.components)

(defn card [content]
  [:div
   {:class "rounded-lg border border-primary shadow-sm bg-white space-y-2"}
   [:div
    {:class "flex flex-col space-y-1.5 p-4"} content]])

(defn section-title [title]
  [:h2 {:class "font-semibold leading-none tracking-tight"} title])

(defn section-subtitle [subtitle]
  [:div
   {:class "text-sm text-gray-400"} subtitle])

(def variant-classes
  {:primary    "bg-primary text-primary-foreground hover:bg-primary/90"
   :secondary  "bg-secondary text-secondary-foreground hover:bg-secondary/80"
   :outline    "border border-input bg-background hover:bg-accent hover:text-accent-foreground"
   :destructive "bg-destructive text-destructive-foreground hover:bg-destructive/90"})

(def base-button-classes
  "inline-flex items-center justify-center rounded-md text-sm font-medium transition-colors
   focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-primary/60
   focus-visible:ring-offset-2 disabled:opacity-50 disabled:pointer-events-none
   h-9 px-4 py-2")

(defn button
  [{:keys [label on-click variant full-width]
    :or {variant :primary full-width true}}]
  (let [variant-class (get variant-classes variant (:primary variant-classes))]
    [:button
     {:type "button"
      :on-click on-click
      :class (str base-button-classes " " variant-class " " (if full-width "w-full" ""))}
     label]))

(defn divider-horizontal []
  [:div
   {:class "shrink-0 bg-gray-200 h-[1px] w-full my-4"}])
