(ns overlap.core
  (:require [quil.core :as q]
            [quil.middleware :as m]))

(defn setup []
  (q/frame-rate 30)

  {:points []})

(defn update-state [state]
  state)

(defn draw-state [state]
  (q/background 0)

  )

(q/defsketch #_:clj-kondo/ignore overlap
  :title "Overlap"
  :size [500 500]
  :setup setup
  :update update-state
  :draw draw-state
  :features [:keep-on-top :no-bind-output]
  :middleware [m/fun-mode])
