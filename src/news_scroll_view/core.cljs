(ns news-scroll-view.core
  (:require [reagent.core :as r]))

(enable-console-print!)

;; TODO window resize

(def scroll-speed 0.6)

(def no-news-message "no news")

(def app-state (r/atom {:current-relative-x 0
                        :current-news-index 0
                        :current-news-msg ""
                        :news-list ["[category 1] news1"
                                    "[category 2] news2"
                                    "[category 3] news3"]
                        :width 0
                        :box-width 0}))

(defn- update-offset-width [key this]
  (let [offset-width (.-offsetWidth (r/dom-node this))]
                              (swap! app-state #(assoc-in % [key] offset-width))))

(defn -news-item []
  (fn []
    (let [msg (get (:news-list @app-state) (:current-news-index @app-state) no-news-message)]
      [:div {:style {:whiteSpace "nowrap"
                     :overflow "visible"
                     :position "relative"
                     :left (:current-relative-x @app-state)
                     :font-size 60}}
       [:strong msg]])))

(def news-item
  (with-meta -news-item
    {:component-did-mount (partial update-offset-width :width)
     :component-did-update (partial update-offset-width :width)}))

(defn -news-view []
  (fn []
    (let []
      [:div {:style {:display "flex"
                     :justify-content "flex-start"
                     :align-items "center"
                     :width "100%"
                     :background-color "#EAECEE"
                     :overflow-x "hidden"}}
       [news-item]])))

(def news-view
  (with-meta -news-view
    {:component-did-mount (partial update-offset-width :box-width)
     :component-did-update (partial update-offset-width :box-width)}))

(defn first-component []
  [:div
   [:h3 "<news-scroll-view>"]
   [news-view]])

(defn update-state [{:keys [current-news-index current-relative-x news-list width box-width] :as app-state}]
  (when (and width box-width)
    (if (and (neg? current-relative-x)
             (< current-relative-x (* -1 width)))
      (-> app-state
           (assoc-in [:current-relative-x] box-width)
           (assoc-in [:current-news-index] (if (>= current-news-index (dec (count news-list)))
                                             0
                                             (inc current-news-index))))
      (update-in app-state [:current-relative-x] #(- % scroll-speed)))))

(defn main-loop []
  (swap! app-state update-state)
  (js/window.requestAnimationFrame main-loop))

(defn on-js-reload []
  (r/render [first-component] (js/document.getElementById "app"))
  (js/window.requestAnimationFrame main-loop))

(r/render [first-component] (js/document.getElementById "app"))
(js/window.requestAnimationFrame main-loop)
