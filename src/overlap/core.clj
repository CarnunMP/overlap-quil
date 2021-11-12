(ns overlap.core
  (:require [quil.core :as q]
            [quil.middleware :as m]))

; [x] draw points on click
; [x] reset on :r keypress
; [x] draw lines between sucessive points
; [x] when click near initial point, close the shape and switch into 'fill' (?) mode
; [x] pixel-by-pixel, colour according to whether 'internal' or 'external' to shape
; ...


(def init-state {:points [] ; vector of [x y] points
                 :mode :draw ; options are :draw, :fill, :filling, and :done
                 })
(def p1-radius 20)
(def width 500)
(def height 500)


;;; helpers

(defn- get-line-points
  [points mode]
   (cond-> (map #(vector %1 %2) points (rest points))
     (not= :draw mode) (conj [(peek points) (first points)])))

(defn- click-near-p1? [x y [x1 y1]]
  (when x1
    (let [[xd yd] [(- x x1) (- y y1)]]
      (>= p1-radius (Math/sqrt (+ (* xd xd)
                                  (* yd yd)))))))

(defn- lines-intersect?
  "See: https://www.geeksforgeeks.org/check-if-two-given-line-segments-intersect/"
  ;; TODO: understand/demistify/rewrite
  [[p1 q1] [p2 q2]]
  (let [orientation (fn [[x1 y1] [x2 y2] [x3 y3]]
                      (let [o (- (* (- y2 y1) (- x3 x2))
                                 (* (- x2 x1) (- y3 y2)))]
                        (cond
                          (> o 0) :clockwise
                          (< o 0) :counter-clockwise
                          :else :colinear)))
        on-segment? (fn [[x1 y1] [x2 y2] [x3 y3]]
                      (and (<= x2 (max x1 x3))
                           (>= x2 (min x1 x3))
                           (<= y2 (max y1 y3))
                           (>= y2 (min y1 y3))))
        o1 (orientation p1 q1 p2)
        o2 (orientation p1 q1 q2)
        o3 (orientation p2 q2 p1)
        o4 (orientation p2 q2 q1)]
    (or
      (and (not= o1 o2) (not= o3 o4))
      (and (= :colinear o1) (on-segment? p1 p2 q1))
      (and (= :colinear o2) (on-segment? p1 q2 q1))
      (and (= :colinear o3) (on-segment? p2 p1 q2))
      (and (= :colinear o4) (on-segment? p2 q1 q2)))))

(defn- point-inside-shape? [p points]
  (let [ray [p [-1 -1]]]
    (->> (get-line-points points :filling)
         (filter #(lines-intersect? % ray))
         count
         odd?)))

(defn- fill [points]
  (q/fill 255)
  ;(let [gr (q/create-graphics width height)
  ;      px (q/pixels gr)]
  ;  (dotimes [i (length. px)]
  ;    (let [x (mod (inc i) (inc width))
  ;          y (inc (int (/ (inc i) width)))]
  ;      (when (point-inside-shape? [x y] points)
  ;        (aset-int px i )))))
  (doseq [x (range 1 (inc width))
          y (range 1 (inc height))]
    (when (point-inside-shape? [x y] points)
      (q/ellipse x y 1 1)))
  (q/fill nil))

;;; quil functions

(defn setup []
  (q/frame-rate 30)
  (q/stroke 255)
  (q/fill nil)

  init-state)

(defn update-state [{:keys [_points mode] :as state}]
  (cond
    (= :fill mode) (assoc state :mode :filling)
    (= :filling mode) (assoc state :mode :done)
    :else state))

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

(defn draw-state [{:keys [points mode]}]
  (when (not= :done mode)
    (q/background 0)

    (doseq [[x y] points]
      (q/ellipse x y 2 2))
    (when-let [[x1 y1] (first points)]
      (q/ellipse x1 y1 (* p1-radius 2) (* p1-radius 2)))
    (doseq [[p1 p2] (get-line-points points mode)]
      (q/line p1 p2)))

  (when (= :filling mode)
    (fill points)))

(q/defsketch #_:clj-kondo/ignore overlap
  :title "Overlap"
  :size [width height]
  :setup setup
  :update update-state
  :mouse-pressed mouse-pressed
  :key-pressed key-pressed
  :draw draw-state
  :features [:keep-on-top :no-bind-output]
  :middleware [m/fun-mode])
