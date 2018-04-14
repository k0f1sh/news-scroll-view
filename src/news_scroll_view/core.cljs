(ns news-scroll-view.core
  (:require [reagent.core :as r]))

(enable-console-print!)

;; TODO window resize

(def scroll-speed (* 40 (/ 1 60)))

(def no-news-message "no news")

(def app-state (r/atom {:current-relative-x 0
                        :current-news-index 0
                        :current-news-msg ""
                        :news-list ["[category 1] news1"
                                    "[category 2] news2"
                                    "[category 3] news3"]
                        :width 0
                        :box-width 0}))

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

(defn- update-offset-width [key this]
  (let [offset-width (.-offsetWidth (r/dom-node this))]
                              (swap! app-state #(assoc-in % [key] offset-width))))

(defn news-item [app-state]
  (r/create-class
   {:reagent-render (fn [app-state]
                      (let [msg (get (:news-list @app-state) (:current-news-index @app-state) no-news-message)]
                        [:div {:style {:overflow "visible"
                                       :position "relative"
                                       :left (:current-relative-x @app-state)
                                       :font-size 60}}
                         [:strong msg]]))
    :component-did-mount (fn [this]
                           (let [offset-width (.-offsetWidth (r/dom-node this))]
                             (swap! app-state #(assoc % :width offset-width))))
    :component-did-update (fn [this]
                            (let [offset-width (.-offsetWidth (r/dom-node this))]
                              (swap! app-state #(assoc % :width offset-width))))}))

(defn news-view []
  (let [requested-animation (atom nil)
        app-state (r/atom {:current-relative-x 0
                           :current-news-index 0
                           :current-news-msg ""
                           :news-list ["[category 1] news1"
                                       "[category 2] news2"
                                       "[category 3] news3"]
                           :width 0
                           :box-width 0})]
    (r/create-class
     {:reagent-render (fn []
                        [:div {:style {:display "flex"
                                       :justify-content "flex-start"
                                       :align-items "center"
                                       :width "100%"
                                       :background-color "#EAECEE"
                                       :overflow-x "hidden"}}
                         [news-item app-state]])
      :component-did-mount (fn [this]
                             (let [dom-node (r/dom-node this)
                                   offset-width (.-offsetWidth dom-node)]
                               (swap! app-state #(assoc % :box-width offset-width))
                               ((fn loop-fn []
                                  (swap! app-state update-state)
                                  (reset! requested-animation (js/window.requestAnimationFrame loop-fn))))))
      :component-did-update (fn [this]
                              (let [offset-width (.-offsetWidth (r/dom-node this))]
                                (swap! app-state #(assoc % :box-width offset-width))))})))

(defn first-component []
  [:div
   [:h3 "<news-scroll-view>"]
   [news-view]])

(defn on-js-reload []
  (r/render [first-component] (js/document.getElementById "app")))

(on-js-reload)
