(ns app.main
  (:require
    [ajax.core :refer [GET]]
    [reagent.core :as reagent]
    ["d3-dsv" :refer (tsvParse csvParse)]
    ["d3-format" :refer (format)]
    ["d3-scale" :refer (scaleTime)]
    ["d3-time" :refer (utcDay)]
    ["d3-time-format" :refer (timeParse)]
    ["react-stockcharts" :refer (Chart ChartCanvas ZoomButtons)]
    ["react-stockcharts/lib/axes" :refer (XAxis YAxis)]
    ["react-stockcharts/lib/coordinates" :refer (CrossHairCursor
                                                 EdgeIndicator
                                                 CurrentCoordinate
                                                 MouseCoordinateX
                                                 MouseCoordinateY)]
    ["react-stockcharts/lib/scale" :refer (discontinuousTimeScaleProvider)]
    ["react-stockcharts/lib/series" :refer (CandlestickSeries BarSeries)]
    ["react-stockcharts/lib/tooltip" :refer (OHLCTooltip)]
    ["react-stockcharts/lib/utils" :refer (last timeIntervalBarWidth)]))

(def bar-series (reagent/adapt-react-class BarSeries))
(def candlestick-series (reagent/adapt-react-class CandlestickSeries))
(def chart (reagent/adapt-react-class Chart))
(def chart-canvas (reagent/adapt-react-class ChartCanvas))
(def cross-hair-cursor (reagent/adapt-react-class CrossHairCursor))
(def current-coordinate (reagent/adapt-react-class CurrentCoordinate))
(def edge-indicator (reagent/adapt-react-class EdgeIndicator))
(def mouse-coordiate-x (reagent/adapt-react-class MouseCoordinateX))
(def mouse-coordiate-y (reagent/adapt-react-class MouseCoordinateY))
(def ohlc-tooltip (reagent/adapt-react-class OHLCTooltip))
(def xaxis (reagent/adapt-react-class XAxis))
(def yaxis (reagent/adapt-react-class YAxis))
(def zoom-buttons (reagent/adapt-react-class ZoomButtons))

(defn stockchart
  [arg-map]
  (let [suffix (reagent/atom 1)
        initial-data (:data arg-map)
        x-scale-provider (.inputDateAccessor discontinuousTimeScaleProvider #(.-date %))
        r (js->clj (x-scale-provider initial-data))
        data (clj->js (get r "data"))
        x-scale (get r "xScale")
        x-accessor (get r "xAccessor")
        display-x-accessor (get r "displayXAccessor")
        x-extents [(x-accessor (last data))
                   (x-accessor (nth data (- (.-length data) 150)))]
        margin {:left 70 :right 70 :top 20 :bottom 30}
        height 400
        width 800
        handle-reset (fn [_] (swap! suffix inc))]
    (reagent/create-class
     {:reagent-render
      (fn
        []
        [chart-canvas
         {:ratio 1
          :width width
          :height height
          :margin {:left 70
                   :right 70
                   :top 10
                   :bottom 30}
          :type "hybrid"
          :seriesName (str "MSFT_" @suffix)
          :data data
          :xAccessor x-accessor
          :displayXAccessor display-x-accessor
          :xScale x-scale
          :xExtents x-extents}
         [chart {:id 1
                 :yExtents #(clj->js [(.-high %) (.-low %)])}
          [xaxis {:axisAt "bottom" :orient "bottom"}]
          [yaxis {:axisAt "right" :orient "right" :ticks 5}]
          [mouse-coordiate-x {:at "bottom"
                              :orient "bottom"
                              :displayFormat (timeParse "%Y-%m-%d")}]
          [mouse-coordiate-y {:at "left"
                              :orient "left"
                              :displayFormat (format ".4s")}]
          [candlestick-series #_{:width (timeIntervalBarWidth utcDay)}]
          [ohlc-tooltip {:forChart 1
                         :origin [-40, 0]}]
          [zoom-buttons {:onReset #(handle-reset "arg_not_used")}]]
         [chart {:id 2
                 :yExtents #(clj->js (.-volume %))
                 :height 150
                 :origin #(clj->js [0 (- %2 150)])}
          [yaxis {:axisAt "left" :orient "left" :ticks 5 :tickFormat (format ".2s")}]
          [mouse-coordiate-y {:at "left"
                              :orient "left"
                              :displayFormat (format ".4s")}]
          [bar-series {:yAccessor #(.-volume %)
                       :fill #(if (> (.-close %) (.-open %)) "#6BA583" "#FF0000")}]
          [current-coordinate {:yAccessor #(.-volume %) :fill "#9B0A47"}]
          [edge-indicator {:itemType "last" :orient="right" :edgeAt "right"
                           :yAccessor #(.-volue %)
                           :displayFormat (format ".4s")
                           :fill "#0F0F0F"}]
          [candlestick-series #_{:width (timeIntervalBarWidth utcDay)}]]
         [cross-hair-cursor]])})))

(defn parse-data
  [time-parser]
  (fn [d]
    (clj->js
     (as-> (js->clj d) _d
       (update _d "date" time-parser)
       (update _d "open" js/parseFloat)
       (update _d "high" js/parseFloat)
       (update _d "low" js/parseFloat)
       (update _d "close" js/parseFloat)
       (update _d "volume" js/parseFloat)))))

(GET "https://cdn.rawgit.com/rrag/react-stockcharts/master/docs/data/MSFT_INTRA_DAY.tsv"
    {:params {}
     :handler #(let [data (tsvParse % (parse-data (fn [d] (js/Date. (js/Number. d)))))]
                 ;;(js/console.log (str "data: " (take 5 data)))
                 (reagent/render [:div [stockchart {:data data}]]
                                 (.getElementById js/document "chart")))
     :error-handler #(js/console.log (str "ERROR: " %))})

