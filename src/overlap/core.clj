(ns overlap.core
  (:require [quil.core :as q]
            [quil.middleware :as m]))

; [x] draw points on click
; [x] reset on :r keypress
; [x] draw lines between sucessive points
; [x] when click near initial point, close the shape and switch into 'fill' (?) mode
; [] pixel-by-pixel, colour according to whether 'internal' or 'external' to shape
; ...


(def init-state {:points [] ; vector of [x y] points
                 :mode :draw ; options are :draw and :fill
                 })
(def p1-radius 20)


;;; helpers

(defn- get-line-points [points]
  (map #(vector %1 %2) points (rest points)))

(defn- click-near-p1? [x y [x1 y1]]
  (when x1
    (let [[xd yd] [(- x x1) (- y y1)]]
      (>= p1-radius (Math/sqrt (+ (* xd xd)
                                  (* yd yd)))))))

;;; quil functions

(defn setup []
  (q/frame-rate 30)
  (q/stroke 255)
  (q/fill nil)

  init-state)

(defn update-state [state]
  state)

(defn mouse-pressed [{:keys [points mode] :as state} {:keys [x y button]}]
  (if (= :left button)
    (if (click-near-p1? x y (first points))
      (assoc state :mode :fill)
      (if (= :draw mode)
        (update state :points conj [x y])
        state))
    state))

(defn key-pressed [state {:keys [key]}]
  (if (= :r key)
    init-state
    state))

(defn draw-state [{:keys [points mode] :as state}]
  (q/background 0)

  ;; draw points
  (doseq [[x y] points]
    (q/ellipse x y 2 2))
  ;; draw circle around p1
  (when-let [[x1 y1] (first points)]
    (q/ellipse x1 y1 (* p1-radius 2) (* p1-radius 2)))
  ;; draw lines
  (doseq [[p1 p2] (get-line-points points)]
    (q/line p1 p2))
  ;; draw final line
  (when (= :fill mode)
    (q/line (peek points) (first points)))
  )

(q/defsketch #_:clj-kondo/ignore overlap
  :title "Overlap"
  :size [500 500]
  :setup setup
  :update update-state
  :mouse-pressed mouse-pressed
  :key-pressed key-pressed
  :draw draw-state
  :features [:keep-on-top :no-bind-output]
  :middleware [m/fun-mode])
