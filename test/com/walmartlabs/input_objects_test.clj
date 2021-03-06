; Copyright (c) 2017-present Walmart, Inc.
;
; Licensed under the Apache License, Version 2.0 (the "License")
; you may not use this file except in compliance with the License.
; You may obtain a copy of the License at
;
;     http://www.apache.org/licenses/LICENSE-2.0
;
; Unless required by applicable law or agreed to in writing, software
; distributed under the License is distributed on an "AS IS" BASIS,
; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
; See the License for the specific language governing permissions and
; limitations under the License.

(ns com.walmartlabs.input-objects-test
  (:require
    [clojure.test :refer [deftest is]]
    [com.walmartlabs.test-utils :refer [compile-schema execute]]
    ))

(deftest null-checks-within-nullable-field
  (let [schema (compile-schema "nested-non-nullable-fields-schema.edn"
                               {:mutation/create-game (fn [_ args _]
                                                        (pr-str args))})]
    (is (= {:data {:create_game "{:game_data {:id 50, :name \"Whist\"}}"}}
           (execute schema "mutation { create_game (game_data: {id: 50, name: \"Whist\"}) }")))

    ;; It's OK to omit the game_data argument entirely
    (is (= {:data {:create_game "nil"}}
           (execute schema "mutation { create_game }")))

    (is (= {:errors [{:argument :game_data
                      :field :create_game
                      :locations [{:column 12
                                   :line 1}]
                      :message "Exception applying arguments to field `create_game': For argument `game_data', no value provided for non-nullable key `id' of input object `game_template'."
                      :missing-key :id
                      :query-path []
                      :required-keys [:id]
                      :schema-type :game_template}]}
           (execute schema "mutation { create_game (game_data: { name: \"Hearts\" }) }")))

    ;; TODO: Missing some needed context from above

    (is (= {:errors [{:argument :game_data
                      :field :create_game
                      :locations [{:column 32
                                   :line 1}]
                      :message "No value provided for non-nullable key `id' of input object `game_template'."
                      :missing-key :id
                      :query-path []
                      :required-keys [:id]
                      :schema-type :game_template}]}
           (execute schema
                    "mutation($g : game_template) { create_game(game_data: $g) }"
                    {:g {:name "Backgammon"}}
                    nil)))))
