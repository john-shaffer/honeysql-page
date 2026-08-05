#!/usr/bin/env bash

set -euxo pipefail

npm ci
clojure -M:shadow-cljs watch :main
