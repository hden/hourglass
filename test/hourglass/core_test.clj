(ns hourglass.core-test
  (:refer-clojure :exclude [time])
  (:require [clojure.test :refer :all]
            [hourglass.core :as hg]
            [inflections.core :as inflections])
  (:import [java.time ZoneId Clock Instant LocalDate LocalTime LocalDateTime]
           [java.time.temporal TemporalAmount]))

(deftest interval
  (testing "constructor / multi-methods."
    (are [x] (let [coll [(keyword x)
                         (keyword (inflections/plural x))
                         x
                         (inflections/plural x)]]
               (every? #(instance? TemporalAmount (hg/interval 1 %))
                       coll))
      "microsecond"
      "millisecond"
      "second"
      "minute"
      "hour"
      "day"
      "week"
      "month"
      "year")))

(deftest date
  (testing "constructor"
    (let [instant (Instant/parse "2007-12-03T10:15:30.00Z")
          clock (Clock/fixed instant (ZoneId/of "UTC"))]
      (are [args] (let [t (apply hg/date args)]
                    (= (LocalDate/of 2007 12 3) t))
        [clock]
        [instant "UTC"]
        [2007 12 3])))

  (testing "extract"
    (let [t (LocalDate/of 2007 12 3)]
      (are [part expected] (= expected (hg/extract t part))
        hg/year 2007
        hg/month 12
        hg/day 3)))

  (testing "add"
    (let [t (LocalDate/of 2007 12 3)]
      (are [x unit expected] (= expected (hg/add t (hg/interval x unit)))
        1 :year (LocalDate/of 2008 12 3)
        1 :month (LocalDate/of 2008 1 3)
        1 :day (LocalDate/of 2007 12 4))))

  (testing "sub"
    (let [t (LocalDate/of 2007 12 3)]
      (are [x unit expected] (= expected (hg/sub t (hg/interval x unit)))
        1 :year (LocalDate/of 2006 12 3)
        1 :month (LocalDate/of 2007 11 3)
        1 :day (LocalDate/of 2007 12 2))))

  (testing "diff"
    (let [x (LocalDate/of 2007 12 3)
          y (LocalDate/of 2006 11 2)]
      (are [unit expected] (= expected (hg/diff x y unit))
        hg/year 1
        hg/month 13
        hg/day 396)))

  (testing "trunc"
    (let [x (LocalDate/of 2007 12 3)]
      (are [unit expected] (= expected (hg/trunc x unit))
        hg/year (LocalDate/of 2007 1 1)
        hg/month (LocalDate/of 2007 12 1))))

  (testing "format"
    (let [x (LocalDate/of 2007 12 3)]
      (is (= "2007-12-03" (hg/format x "yyyy-MM-dd"))))))

(deftest time
  (testing "constructor"
    (let [instant (Instant/parse "2007-12-03T10:15:30.00Z")
          clock (Clock/fixed instant (ZoneId/of "UTC"))]
      (are [args] (let [t (apply hg/time args)]
                    (= (LocalTime/of 10 15 30) t))
        [clock]
        [instant "UTC"]
        [10 15 30])))

  (testing "extract"
    (let [t (LocalTime/of 10 15 30)]
      (are [part expected] (= expected (hg/extract t part))
        hg/hour 10
        hg/minute 15
        hg/second 30
        hg/millisecond 0
        hg/microsecond 0)))

  (testing "add"
    (let [t (LocalTime/of 10 15 30)]
      (are [x unit expected] (= expected (hg/add t (hg/interval x unit)))
        1 :hour (LocalTime/of 11 15 30)
        1 :minute (LocalTime/of 10 16 30)
        1 :second (LocalTime/of 10 15 31))))

  (testing "sub"
    (let [t (LocalTime/of 10 15 30)]
      (are [x unit expected] (= expected (hg/sub t (hg/interval x unit)))
        1 :hour (LocalTime/of 9 15 30)
        1 :minute (LocalTime/of 10 14 30)
        1 :second (LocalTime/of 10 15 29))))

  (testing "diff"
    (let [x (LocalTime/of 10 15 30)
          y (LocalTime/of 9 14 29)]
      (are [unit expected] (= expected (hg/diff x y unit))
        hg/hour 1
        hg/minute 61
        hg/second 3661)))

  (testing "trunc"
    (let [x (LocalTime/of 10 15 30)]
      (are [unit expected] (= expected (hg/trunc x unit))
        hg/hour (LocalTime/of 10 0 0)
        hg/minute (LocalTime/of 10 15 0)
        hg/second (LocalTime/of 10 15 30)
        hg/millisecond (LocalTime/of 10 15 30)
        hg/microsecond (LocalTime/of 10 15 30))))

  (testing "format"
    (let [x (LocalTime/of 10 15 30)]
      (is (= "10:15:30" (hg/format x "HH:mm:ss"))))))

(deftest datetime
  (testing "constructor"
    (let [instant (Instant/parse "2007-12-03T10:15:30.00Z")
          clock (Clock/fixed instant (ZoneId/of "UTC"))]
      (are [args] (let [t (apply hg/datetime args)]
                    (= (LocalDateTime/of 2007 12 3 10 15 30) t))
        [clock]
        [instant "UTC"])))

  (testing "extract"
    (let [t (LocalDateTime/of 2007 12 3 10 15 30)]
      (are [part expected] (= expected (hg/extract t part))
        hg/year 2007
        hg/month 12
        hg/day 3
        hg/hour 10
        hg/minute 15
        hg/second 30
        hg/millisecond 0
        hg/microsecond 0)))

  (testing "add"
    (let [t (LocalDateTime/of 2007 12 3 10 15 30)]
      (are [x unit expected] (= expected (hg/add t (hg/interval x unit)))
        1 :year (LocalDateTime/of 2008 12 3 10 15 30)
        1 :month (LocalDateTime/of 2008 1 3 10 15 30)
        1 :day (LocalDateTime/of 2007 12 4 10 15 30)
        1 :hour (LocalDateTime/of 2007 12 3 11 15 30)
        1 :minute (LocalDateTime/of 2007 12 3 10 16 30)
        1 :second (LocalDateTime/of 2007 12 3 10 15 31))))

  (testing "sub"
    (let [t (LocalDateTime/of 2007 12 3 10 15 30)]
      (are [x unit expected] (= expected (hg/sub t (hg/interval x unit)))
        1 :year (LocalDateTime/of 2006 12 3 10 15 30)
        1 :month (LocalDateTime/of 2007 11 3 10 15 30)
        1 :day (LocalDateTime/of 2007 12 2 10 15 30)
        1 :hour (LocalDateTime/of 2007 12 3 9 15 30)
        1 :minute (LocalDateTime/of 2007 12 3 10 14 30)
        1 :second (LocalDateTime/of 2007 12 3 10 15 29))))

  (testing "diff"
    (let [x (LocalDateTime/of 2007 12 3 10 15 30)
          y (LocalDateTime/of 2006 11 2 9 14 29)]
      (are [unit expected] (= expected (hg/diff x y unit))
        hg/year 1
        hg/month 13
        hg/day 396
        hg/hour 9505
        hg/minute 570301
        hg/second 34218061)))

  (testing "trunc"
    (let [x (LocalDateTime/of 2007 12 3 10 15 30)]
      (are [unit expected] (= expected (hg/trunc x unit))
        hg/year (LocalDateTime/of 2007 1 1 0 0 0)
        hg/month (LocalDateTime/of 2007 12 1 0 0 0)
        hg/day (LocalDateTime/of 2007 12 3 0 0 0)
        hg/hour (LocalDateTime/of 2007 12 3 10 0 0)
        hg/minute (LocalDateTime/of 2007 12 3 10 15 0)
        hg/second (LocalDateTime/of 2007 12 3 10 15 30)
        hg/millisecond (LocalDateTime/of 2007 12 3 10 15 30)
        hg/microsecond (LocalDateTime/of 2007 12 3 10 15 30))))

  (testing "format"
    (let [x (LocalDateTime/of 2007 12 3 10 15 30)]
      (is (= "2007-12-03 10:15:30" (hg/format x "yyyy-MM-dd HH:mm:ss"))))))

(deftest timestamp
  (testing "constructor"
    (let [instant (Instant/parse "2007-12-03T10:15:30.00Z")
          clock (Clock/fixed instant (ZoneId/of "UTC"))]
      (are [args] (let [t (apply hg/timestamp args)]
                    (= (Instant/parse "2007-12-03T10:15:30.00Z") t))
        [clock])))

  (testing "extract without timezone"
    (let [t (Instant/parse "2007-12-03T10:15:30.00Z")]
      (are [part expected] (= expected (hg/extract t part))
        hg/year 2007
        hg/month 12
        hg/day 3
        hg/hour 10
        hg/minute 15
        hg/second 30
        hg/millisecond 0
        hg/microsecond 0)))

  (testing "add"
    (let [t (Instant/parse "2007-12-03T10:15:30.00Z")]
      (are [x unit expected] (= expected (hg/add t (hg/interval x unit)))
        1 :year (Instant/parse "2008-12-03T10:15:30.00Z")
        1 :month (Instant/parse "2008-01-03T10:15:30.00Z")
        1 :day (Instant/parse "2007-12-04T10:15:30.00Z")
        1 :hour (Instant/parse "2007-12-03T11:15:30Z")
        1 :minute (Instant/parse "2007-12-03T10:16:30.00Z")
        1 :second (Instant/parse "2007-12-03T10:15:31.00Z"))))

  (testing "sub"
    (let [t (Instant/parse "2007-12-03T10:15:30.00Z")]
      (are [x unit expected] (= expected (hg/sub t (hg/interval x unit)))
        1 :year (Instant/parse "2006-12-03T10:15:30.00Z")
        1 :month (Instant/parse "2007-11-03T10:15:30.00Z")
        1 :day (Instant/parse "2007-12-02T10:15:30.00Z")
        1 :hour (Instant/parse "2007-12-03T09:15:30.00Z")
        1 :minute (Instant/parse "2007-12-03T10:14:30.00Z")
        1 :second (Instant/parse "2007-12-03T10:15:29.00Z"))))

  (testing "diff"
    (let [x (Instant/parse "2007-12-03T10:15:30.00Z")
          y (Instant/parse "2006-11-02T09:14:29.00Z")]
      (are [unit expected] (= expected (hg/diff x y unit))
        hg/year 1
        hg/month 13
        hg/day 396
        hg/hour 9505
        hg/minute 570301
        hg/second 34218061)))

  (testing "trunc"
    (let [x (Instant/parse "2007-12-03T10:15:30.00Z")]
      (are [unit expected] (= expected (hg/trunc x unit))
        hg/year (Instant/parse "2007-01-01T00:00:00.00Z")
        hg/month (Instant/parse "2007-12-01T00:00:00.00Z")
        hg/day (Instant/parse "2007-12-03T00:00:00.00Z")
        hg/hour (Instant/parse "2007-12-03T10:00:00.00Z")
        hg/minute (Instant/parse "2007-12-03T10:15:00.00Z")
        hg/second (Instant/parse "2007-12-03T10:15:30.00Z")
        hg/millisecond (Instant/parse "2007-12-03T10:15:30.00Z")
        hg/microsecond (Instant/parse "2007-12-03T10:15:30.00Z"))))

  (testing "format"
    (let [x (Instant/parse "2007-12-03T10:15:30.00Z")]
      (is (= "2007-12-03 10:15:30" (hg/format x "yyyy-MM-dd HH:mm:ss"))))))
