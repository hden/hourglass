(ns hourglass.core
  (:refer-clojure :exclude [second time format add])
  (:require [clojure.string :as string]
            [inflections.core :as inflections])
  (:import [java.time Duration Period ZoneId Clock Instant LocalDate LocalTime LocalDateTime ZonedDateTime]
           [java.time.format DateTimeFormatter]
           [java.time.temporal ChronoField ChronoUnit TemporalAdjuster TemporalAdjusters TemporalAmount]))

(defmulti interval (fn [x u]
                     (string/lower-case (inflections/singular (name u)))))

(defmethod interval "microsecond" [x _]
  (Duration/of x ChronoUnit/MICROS))

(defmethod interval "millisecond" [x _]
  (Duration/of x ChronoUnit/MILLIS))

(defmethod interval "second" [x _]
  (Duration/of x ChronoUnit/SECONDS))

(defmethod interval "minute" [x _]
  (Duration/of x ChronoUnit/MINUTES))

(defmethod interval "hour" [x _]
  (Duration/of x ChronoUnit/HOURS))

(defmethod interval "day" [x _]
  (Period/ofDays x))

(defmethod interval "week" [x _]
  (Period/ofWeeks x))

(defmethod interval "month" [x _]
  (Period/ofMonths x))

(defmethod interval "year" [x _]
  (Period/ofYears x))

(def microsecond {:chrono-field ChronoField/MICRO_OF_SECOND
                  :chrono-unit ChronoUnit/MICROS})
(def millisecond {:chrono-field ChronoField/MILLI_OF_SECOND
                  :chrono-unit ChronoUnit/MILLIS})
(def second {:chrono-field ChronoField/SECOND_OF_MINUTE
             :chrono-unit ChronoUnit/SECONDS})
(def minute {:chrono-field ChronoField/MINUTE_OF_HOUR
             :chrono-unit ChronoUnit/MINUTES})
(def hour {:chrono-field ChronoField/HOUR_OF_DAY
           :chrono-unit ChronoUnit/HOURS})
(def day-of-week {:chrono-field ChronoField/DAY_OF_WEEK})
(def day {:chrono-field ChronoField/DAY_OF_MONTH
          :chrono-unit ChronoUnit/DAYS})
(def day-of-year {:chrono-field ChronoField/DAY_OF_YEAR})
(def week {:chrono-field ChronoField/ALIGNED_WEEK_OF_YEAR
           :chrono-unit ChronoUnit/WEEKS})
(def month {:chrono-field ChronoField/MONTH_OF_YEAR
            :chrono-unit ChronoUnit/MONTHS
            :temporal-adjuster (TemporalAdjusters/firstDayOfMonth)})
(def year {:chrono-field ChronoField/YEAR
           :chrono-unit ChronoUnit/YEARS
           :temporal-adjuster (TemporalAdjusters/firstDayOfYear)})

(defprotocol Temporal
  (extract [this m] [this m timezone])
  (add [this interval])
  (sub [this interval])
  (diff [this that m])
  (trunc [this m] [this m timezone])
  (format [this s] [this s timezone]))

(declare datetime)

(defn date
  "A date without a time-zone in the ISO-8601 calendar system, such as 2007-12-03."
  ([]
   (LocalDate/now))
  ([^Clock clock]
   (LocalDate/now clock))
  ([^Instant instant ^String timezone]
   (-> (^LocalDateTime datetime instant timezone)
       (.toLocalDate)))
  ([^Integer x ^Integer y ^Integer z]
   {:pre [(every? pos-int? [x y z])]}
   (LocalDate/of x y z)))

(extend LocalDate
  Temporal
  {:extract (fn ([^LocalDate this m]
                 {:pre [(contains? m :chrono-field)]}
                 (.get this (:chrono-field m)))
                ([this m timezone]
                 (extract this m)))
   :add (fn [^LocalDate this ^TemporalAmount interval]
          (.plus this interval))
   :sub (fn [^LocalDate this ^TemporalAmount interval]
          (.minus this interval))
   :diff (fn [^LocalDate this ^LocalDate that m]
           {:pre [(contains? m :chrono-unit)]}
           (.until that this (:chrono-unit m)))
   :trunc (fn ([^LocalDate this m]
               {:pre [(contains? m :temporal-adjuster)]}
               (.with this ^TemporalAdjuster (:temporal-adjuster m)))
              ([this m timezone]
               (trunc this m)))
   :format (fn ([^LocalDate this s]
                (let [formatter (DateTimeFormatter/ofPattern s)]
                  (.format this formatter)))
               ([this s timezone]
                (format this s)))})

(defn time
  "A time without a time-zone in the ISO-8601 calendar system, such as 13:05:30."
  ([]
   (LocalTime/now))
  ([^Clock clock]
   (LocalTime/now clock))
  ([^Instant instant ^String timezone]
   (-> (^LocalDateTime datetime instant timezone)
       (.toLocalTime)))
  ([^Integer x ^Integer y ^Integer z]
   {:pre [(every? nat-int? [x y z])]}
   (LocalTime/of x y z)))

(extend LocalTime
  Temporal
  {:extract (fn ([^LocalTime this m]
                 {:pre [(contains? m :chrono-field)]}
                 (.get this (:chrono-field m)))
                ([this m timezone]
                 (extract this m)))
   :add (fn [^LocalTime this ^TemporalAmount interval]
          (.plus this interval))
   :sub (fn [^LocalTime this ^TemporalAmount interval]
          (.minus this interval))
   :diff (fn [^LocalTime this ^LocalTime that m]
           {:pre [(contains? m :chrono-unit)]}
           (.until that this (:chrono-unit m)))
   :trunc (fn ([^LocalTime this m]
               {:pre [(contains? m :chrono-unit)]}
               (.truncatedTo this (:chrono-unit m)))
              ([this m timezone]
               (trunc this m)))
   :format (fn ([^LocalTime this s]
                (let [formatter (DateTimeFormatter/ofPattern s)]
                  (.format this formatter)))
               ([this s timezone]
                (format this s)))})

(defn datetime
  "A date-time without a time-zone in the ISO-8601 calendar system, such as 2007-12-03T10:15:30."
  ([]
   (LocalDateTime/now))
  ([^Clock clock]
   (LocalDateTime/now clock))
  ([^Instant instant ^String timezone]
   (LocalDateTime/ofInstant instant (ZoneId/of timezone))))

(extend LocalDateTime
  Temporal
  {:extract (fn ([^LocalDateTime this m]
                 {:pre [(contains? m :chrono-field)]}
                 (.get this (:chrono-field m)))
                ([this m timezone]
                 (extract this m)))
   :add (fn [^LocalDateTime this ^TemporalAmount interval]
          (.plus this interval))
   :sub (fn [^LocalDateTime this ^TemporalAmount interval]
          (.minus this interval))
   :diff (fn [^LocalDateTime this ^LocalDateTime that m]
           {:pre [(contains? m :chrono-unit)]}
           (.until that this (:chrono-unit m)))
   :trunc (fn ([^LocalDateTime this m]
               {:pre [(or (contains? m :temporal-adjuster)
                          (contains? m :chrono-unit))]}
               (if (contains? m :temporal-adjuster)
                 (-> (.with this ^TemporalAdjuster (:temporal-adjuster m))
                     (.truncatedTo ChronoUnit/DAYS))
                 (.truncatedTo this (:chrono-unit m))))
              ([this m timezone]
               (trunc this m)))
   :format (fn ([^LocalDateTime this s]
                (let [formatter (DateTimeFormatter/ofPattern s)]
                  (.format this formatter)))
               ([this s timezone]
                (format this s)))})

(defn timestamp
  "An instantaneous point on the time-line."
  ([]
   (Instant/now))
  ([^Clock clock]
   (Instant/now clock)))

(extend Instant
  Temporal
  {:extract (fn ([this m]
                 {:pre [(contains? m :chrono-field)]}
                 (extract this m "UTC"))
                ([this m timezone]
                 (let [t (datetime this timezone)]
                   (extract t m))))
   :add (fn [this interval]
          (let [t (datetime this "UTC")]
            (-> (^LocalDateTime add t interval)
                (.atZone (ZoneId/of "UTC"))
                (.toInstant))))
   :sub (fn [this interval]
          (let [t (datetime this "UTC")]
            (-> (^LocalDateTime sub t interval)
                (.atZone (ZoneId/of "UTC"))
                (.toInstant))))
   :diff (fn [this that m]
           (let [this (datetime this "UTC")
                 that (datetime that "UTC")]
             (diff this that m)))
   :trunc (fn ([this m]
               (trunc this m "UTC"))
              ([this m timezone]
               (let [t (datetime this timezone)]
                 (-> (^LocalDateTime trunc t m)
                     (.atZone (ZoneId/of timezone))
                     (.toInstant)))))
   :format (fn ([this s]
                (format this s "UTC"))
               ([this s timezone]
                (let [t (datetime this timezone)]
                  (format t s))))})
