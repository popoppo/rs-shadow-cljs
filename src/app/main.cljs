(ns app.main
  (:require
    [ajax.core :refer [GET]]
    [reagent.core :as reagent]
    ["d3-dsv" :refer (tsvParse csvParse)]
    ["d3-scale" :refer (scaleTime)]
    ["d3-time" :refer (utcDay)]
    ["d3-time-format" :refer (timeParse)]
    ["react-stockcharts/lib/axes" :refer (XAxis YAxis)]
    ["react-stockcharts/lib/helper" :refer (fitWidth TypeChooser)]
    ["react-stockcharts/lib/series" :refer (CandlestickSeries)]
    ["react-stockcharts/lib/utils" :refer (last timeIntervalBarWidth)]
    ["react-stockcharts" :refer (ChartCanvas Chart)]))


(def candlestick-series (reagent/adapt-react-class CandlestickSeries))
(def chart (reagent/adapt-react-class Chart))
(def chart-canvas (reagent/adapt-react-class ChartCanvas))
(def type-chooser (reagent/adapt-react-class TypeChooser))
(def xaxis (reagent/adapt-react-class XAxis))
(def yaxis (reagent/adapt-react-class YAxis))


(def candlestick-chart
  (reagent/create-class
   {:reagent-render
    (fn [props]
      (let [{:keys [type width data ratio]} props
            x-accessor #(.-date %)
            x-extents [(x-accessor (last data))
                       (x-accessor (nth data (- (count data) 100)))]]
        [chart-canvas {:height 400
                       :ratio ratio
                       :width width
                       :margin {:left 50
                                :right 50
                                :top 10
                                :bottom 30}
                       :type type
                       :seriesName "MSFT"
                       :data data
                       :xAccessor x-accessor
                       :displayXAccessor x-accessor
                       :xScale (scaleTime)
                       :xExtents x-extents}
         [chart {:id 1
                 :yExtents #(clj->js [(.-high %) (.-low %)])}
          [xaxis {:axisAt "bottom" :orient "bottom" :ticks 6}]
          [yaxis {:axisAt "left" :orient "left" :ticks 5}]
          [candlestick-series {:width (timeIntervalBarWidth utcDay)}]]]))}))


(def fitted-chart (fitWidth candlestick-chart))


(defn chart-component
  [data]
  [type-chooser
   (fn [type]
     (reagent/create-element
      fitted-chart
      #js {:type type
           :width 800
           :data data}))])


(defn render-chart
  [data]
  (reagent/render [chart-component data]
                  (.getElementById js/document "chart")))


(def parseDate (timeParse "%Y-%m-%d"))


(defn parse-data
  [parse]
  (fn [d]
    (clj->js
     (as-> (js->clj d) _d
       (update _d "date" parse)
       (update _d "open" js/parseFloat)
       (update _d "high" js/parseFloat)
       (update _d "low" js/parseFloat)
       (update _d "close" js/parseFloat)
       (update _d "volume" js/parseFloat)))))


(defn main! []
  (GET "http://rrag.github.io/react-stockcharts/data/MSFT.tsv"
    {:params {}
     :handler #(let [data (tsvParse % (parse-data parseDate))]
                 (render-chart data))
     :error-handler #(js/console.log (str "ERROR: " %))}))


#_(defn reload! [] (do something as needed))


(main!)
