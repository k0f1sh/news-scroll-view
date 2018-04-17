(ns news-scroll-view.core
  (:require [reagent.core :as r]))

(enable-console-print!)

;; TODO window resize

(def scroll-speed (* 100 (/ 1 60)))

(def no-news-message "no news")

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

(defn news-view [news-list]
  (let [requested-animation (atom nil)
        app-state (r/atom {:current-relative-x 0
                           :current-news-index 0
                           :current-news-msg ""
                           :news-list news-list
                           :width 0
                           :box-width 0})]
    (r/create-class
     {:reagent-render (fn [news-list]
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
                                (swap! app-state #(assoc % :box-width offset-width))))
      :component-will-receive-props (fn [this [_ new-news-list]]
                                      (let [box-width (:box-width @app-state)]
                                        (swap! app-state #(assoc % :news-list new-news-list))
                                        (swap! app-state #(assoc % :current-news-index 0))
                                        (swap! app-state #(assoc % :current-relative-x box-width))))})))

(defn first-component []
  (let [news-list (r/atom [])]
    (fn []
      [:div
       [:h3 "<news-scroll-view>"]
       [:button {:on-click (fn [_]
                             (reset! news-list ["news1" "news2" "news3"]))}
        "news-set 1"]
       [:button {:on-click (fn [_]
                             (reset! news-list ["news4" "news5" "news6"]))}
        "news-set 2"]
       [news-view @news-list]])))

(defn on-js-reload []
  (r/render [first-component] (js/document.getElementById "app")))

(on-js-reload)
