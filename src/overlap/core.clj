(ns overlap.core
  (:require [quil.core :as q]
            [quil.middleware :as m]))

; [x] draw points on click
; [] reset on :r keypress
; [] draw lines between sucessive points
; [] when click near initial point, switch into 'fill' (?) mode
; [] pixel-by-pixel, colour according to whether 'internal' or 'external' to shape
; ...

(defn setup []
  (q/frame-rate 30)
  (q/stroke 255)

  {:points []})

(defn update-state [state]
  state)

(defn mouse-pressed [state {:keys [x y button]}]
  (if (= :left button)
    (update state :points conj [x y])
    state))

(defn draw-state [{:keys [points] :as state}]
  (q/background 0)

  (doseq [[x y] points]
    (q/ellipse x y 2 2))
  )

(q/defsketch #_:clj-kondo/ignore overlap
  :title "Overlap"
  :size [500 500]
  :setup setup
  :update update-state
  :mouse-pressed mouse-pressed
  :draw draw-state
  :features [:keep-on-top :no-bind-output]
  :middleware [m/fun-mode])
