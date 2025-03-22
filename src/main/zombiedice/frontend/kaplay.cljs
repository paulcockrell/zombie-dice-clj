(ns zombiedice.frontend.kaplay
  (:require [kaplay :default kaplay]))

(defn init [config]
  (kaplay (clj->js config)))

(defn sprite
  ([kaplay name]
   (.sprite kaplay name))
  ([kaplay name opts]
   (.sprite kaplay name (clj->js opts))))

(defn load-sprite
  ([kaplay name url]
   (.loadSprite kaplay name url))
  ([kaplay name url opts]
   (.loadSprite kaplay name url (clj->js opts))))

(defn set-gravity [kaplay val]
  (.setGravity kaplay val))

(defn add [kaplay component_array]
  (.add kaplay (clj->js component_array)))

(defn on-button-press [kaplay button cb]
  (.onButtonPress kaplay button cb))

(defn play-anim [component anim]
  (.play component anim))

(defn play-sound
  ([kaplay sound]
   (.play kaplay sound))
  ([kaplay sound opts]
   (.play kaplay sound (clj->js opts))))

(defn is-grounded [component]
  (.isGrounded ^js component))

(defn jump
  ([component]
   (.jump ^js component))
  ([component force]
   (.jump ^js component force)))

(defn on-ground [component cb]
  (.onGround ^js component cb))

(defn scale [kaplay val]
  (.scale kaplay val))

(defn area
  ([kaplay]
   (.area kaplay))
  ([kaplay opts]
   (.area kaplay (clj->js opts))))

(defn anchor [kaplay pos]
  (.anchor kaplay pos))

(defn body [kaplay opts]
  (.body kaplay (clj->js opts)))

(defn pos [kaplay x y]
  (.pos kaplay x y))

(defn opacity [kaplay val]
  (.opacity kaplay val))

(defn load-sound [kaplay name url]
  (.loadSound kaplay name url))

(defn load-font [kaplay name url]
  (.loadFont kaplay name url))

(defn on-update [kaplay cb]
  (.onUpdate kaplay cb))

(defn move [component x y]
  (.move ^js component x y))

(defn move-to [component x y]
  (.moveTo ^js component x y))

(defn offscreen [kaplay opts]
  (.offscreen kaplay (clj->js opts)))

(defn z-index [k idx]
  (.z k idx))

(defn vec2
  ([kaplay val] (vec2 kaplay val val))
  ([kaplay val-a val-b] (.vec2 kaplay val-a val-b)))

(defn rect
  ([kaplay w h]
   (.rect kaplay w h))
  ([kaplay x y w h]
   (.rect kaplay (vec2 kaplay x y) w h)))

(defn wait [kaplay time cb]
  (.wait kaplay time cb))

(defn destroy [component cb]
  (.destroy ^js component cb))

(defn on-collide [component tag cb]
  (.onCollide ^js component tag cb))

(defn k-rand [kaplay min max]
  (.rand kaplay min max))

(defn scene [kaplay scene-key scene]
  (.scene kaplay scene-key scene))

(defn go
  ([kaplay scene-key]
   (.go kaplay scene-key kaplay))
  ([kaplay scene-key opts]
   (.go kaplay scene-key kaplay opts)))

(defn text [kaplay str opts]
  (.text kaplay str (clj->js opts)))

(defn center [kaplay]
  (.center kaplay))

(defn color [kaplay r g b]
  (.color kaplay r g b))

(defn get-children [component key]
  (array-seq (.get ^js component key)))

(defn kloop [k time cb]
  (.loop k time cb))
