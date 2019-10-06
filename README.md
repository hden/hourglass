# hourglass [![CircleCI](https://circleci.com/gh/hden/hourglass/tree/master.svg?style=svg)](https://circleci.com/gh/hden/hourglass/tree/master) [![codecov](https://codecov.io/gh/hden/hourglass/branch/master/graph/badge.svg)](https://codecov.io/gh/hden/hourglass)

An opinionated wrapper for the Java 8 Date-Time API.

## Rationale

Main goals:

* Provide a consistent API (inspired by BigQuery) for common operations.
* Returns native data types.
* Avoid reflective calls.

## Usage

This library supports the following classes:

* java.time.LocalDate
* java.time.LocalTime
* java.time.LocalDateTime
* java.time.Instant

```clj
(ns example
  (:require [hourglass.core :as hg]))

(def date (hg/date 2019 10 6))
;; #object[java.time.LocalDate 0x1750d7e3 "2019-10-06"]
(hg/add date (hg/interval 1 :month)) ;; works for [singular or plural] [string or keyword]
;; #object[java.time.LocalDate 0x5c8da4ce "2019-11-06"]
(hg/sub date (hg/interval 1 :week))
;; #object[java.time.LocalDate 0x21ca6e37 "2019-09-29"]
(hg/diff date (hg/date 2019 10 1) hg/day)
;; 5
(hg/trunc date hg/month)
;; #object[java.time.LocalDate 0x5b02ff88 "2019-10-01"]
(hg/format date "yyyy-MM-dd")
;; "2019-10-06"
```

## License

Copyright © 2019 Haokang Den <haokang.den@gmail.com>

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.
