(ns my-exercise.home
  (:require [hiccup.page :refer [html5]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [my-exercise.us-state :as us-state])
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]))

(defn header [_]
  [:head
   [:meta {:charset "UTF-8"}]
   [:meta {:name "viewport"
           :content "width=device-width, initial-scale=1.0, maximum-scale=1.0"}]
   [:title "Find my next election"]
   [:link {:rel "stylesheet" :href "default.css"}]])

(defn getting-started [_]
  [:div {:class "getting-started"}
   [:h1 "Getting started"]
   [:p "Thank you for applying to work at Democracy Works! "
    "This coding exercise is designed to show off your ability to program web applications in Clojure. "
    "You should spend no more than 2 hours on it and then turn it in to us "
    "by running the command " [:code "lein submit"] " and following the instructions it prints out. "
    "While we will be evaluating how much of the project you complete, we know that 2 hours isn't enough time to do a "
    "thorough and complete job on all of it, and we're not expecting you to. We just want to see what you get working "
    "in that amount of time."]
   [:p "It is a server-side web application written in Clojure and using the "
    [:a {:href "https://github.com/ring-clojure/ring"} "Ring"] ", "
    [:a {:href "https://github.com/weavejester/compojure"} "Compojure"] ", and "
    [:a {:href "https://github.com/weavejester/hiccup"} "Hiccup"] " libraries."
    "You should feel free to use other libraries as you see fit."]
   [:p "Right now the form below submits to a missing route in the app. To complete the exercise, do the following:"]
   [:ul
    [:li "Create the missing /search route"]
    [:li "Ingest the incoming form parameters"]
    [:li "Derive a basic set of OCD-IDs from the address (see below for further explanation)"]
    [:li "Retrieve upcoming elections from the Democracy Works election API using those OCD-IDs"]
    [:li "Display any matching elections to the user"]]
   [:p "You will get bonus points for:"
    [:ul
     [:li "Documenting your code"]
     [:li "Adding tests for your code"]
     [:li "Standardizing and/or augmenting the address data to derive more OCD division IDs (e.g. county and "
      "legislative districts)"]
     [:li "Noting additional features or other improvements you would make if you had more time"]]]])

(defn ocd-id-explainer [_]
  [:div {:class "ocd-id-explainer"}
   [:h2 "All about OCD-IDs"]
   [:ul
    [:li "OCD-IDs are "
     [:a {:href "http://opencivicdata.readthedocs.io/en/latest/data/datatypes.html"}
      "Open Civic Data division identifiers"]
     " and they look like this (for the state of New Jersey): "
     [:code "ocd-division/country:us/state:nj"]]
    [:li "A given address can be broken down into several OCD-IDs. "
     "For example an address in Newark, New Jersey would be associated with the following OCD-IDs:"]
    [:ul
     [:li [:code "ocd-division/country:us"]]
     [:li [:code "ocd-division/country:us/state:nj"]]
     [:li [:code "ocd-division/country:us/state:nj/county:essex"]]
     [:li [:code "ocd-division/country:us/state:nj/place:newark"]]]
    [:li "Not all of those are derivable from just an address (without "
     "running it through a standardization and augmentation service). "
     "For example, just having a random address in Newark doesn't tell us "
     "what county it is in. But we can derive a basic set of state and place "
     "(i.e. city) OCD-IDs that will be a good starting point for this project. "
     "This entails... "
     [:ul
      [:li "lower-casing the state abbreviation and appending it to "
       [:code "ocd-division/country:us/state:"]]
      [:li "creating a copy of the state OCD-ID"]
      [:li "appending " [:code "/place:"] " to it"]
      [:li "lower-casing the city value, replacing all spaces with underscores, and appending it to that."]]
     "Then you should supply " [:em "both"] " OCD-IDs to your election API "
     "request, separated by a comma as shown in the curl example below."]
    [:li "Elections can be retrieved from the Democracy Works elections API for a set of district divisions like so:"]
    [:ul
     [:li [:code "curl 'https://api.turbovote.org/elections/upcoming?district-divisions=ocd-division/country:us/state:nj,ocd-division/country:us/state:nj/place:newark'"]]
     [:li "The response will be in the "
      [:a {:href "https://github.com/edn-format/edn"}
       "EDN format"]
      " (commonly used in Clojure) by default, but you can request JSON by setting your request's Accept header to 'application/json' if you prefer"]]]])

(defn current-elections-link [_]
  [:div {:class "current-elections-link"}
   [:h2 "Current elections"]
   [:p "Depending on the time of year and whether it's an odd or even-numbered "
    "year, the number of elections in the system can vary wildly. "
    "We maintain an up-to-date "
    [:a {:href "https://github.com/democracyworks/dw-code-exercise-lein-template/wiki/Current-elections"}
     "list of OCD-IDs that should return an election"]
    " until the dates they are listed under. Please refer to that for example "
    "OCD-IDs that will return an election to your app."]])

(defn instructions [request]
  [:div {:class "instructions"}
   (getting-started request)
   (ocd-id-explainer request)
   (current-elections-link request)])

(defn address-form [_]
  [:div {:class "address-form" :id "address-form"}
   [:h1 "Find my next election"]
   [:form {:action "/search#address-form" :method "post"}
    (anti-forgery-field)
    [:p "Enter the address where you are registered to vote"]
    [:div
     [:label {:for "street-field"} "Street:"]
     [:input {:id "street-field"
              :type "text"
              :name "street"}]]
    [:div
     [:label {:for "street-2-field"} "Street 2:"]
     [:input {:id "street-2-field"
              :type "text"
              :name "street-2"}]]
    [:div
     [:label {:for "city-field"} "City:"]
     [:input {:id "city-field"
              :type "text"
              :name "city"
              :required true}]
     [:label {:for "state-field"} "State:"]
     [:select {:id "state-field"
               :name "state"
               :required true}
      [:option ""]
      (for [state us-state/postal-abbreviations]
        [:option {:value state} state])]
     [:label {:for "zip-field"} "ZIP:"]
     [:input {:id "zip-field"
              :type "text"
              :name "zip"
              :size "10"}]]
    [:div.button
     [:button {:type "submit"} "Search"]]]])

(defn search-results [request]
  
  ;; Generate OCD query
  (def params (:form-params request))
  (def ocd_country "ocd-division/country:us")
  (def ocd_state (str "/state:" (get params "state")))
  (def ocd_city (clojure.string/replace (str "/place:" (get params "city")) #" " "_"))
  (def ocd_url (clojure.string/lower-case (str "https://api.turbovote.org/elections/upcoming?district-divisions="
                                               ocd_country
                                               "," ocd_country ocd_state
                                               "," ocd_country ocd_state ocd_city)))
  ;; TODO: Add county and district lookup.
  ;; TODO: Stronger validation, maybe through an address lookup service.
  
  (def election_data (edn/read-string (slurp ocd_url)))
  (def desc_seq (map :description election_data))
  (def url_seq (map :polling-place-url-shortened election_data))
  (def date_seq (map :date election_data))

  [:div
   
    [:h2 "Your Upcoming Elections"]
    
    ;; Returning the user to the search form makes it easier to look up multiple addresses.
    ;; TODO: Fill out other election information. Improve formatting.
    
    ;; If no results came back, tell the user. If they did, loop through the data.
    (if (some? (first desc_seq))
      (let [x 0]
        [:div
         [:h3 (nth desc_seq x)]
         [:p
          (str "This election will be on " 
               (.format (java.text.SimpleDateFormat. "MM/dd/yyyy") (first date_seq)) 
	            ". Visit ")
          [:a {:href (nth url_seq x) :target "_blank"} "this page"]
          (str  " to find your polling place.")]])
      [:div 
       [:p "You have no upcoming elections."]])
		  
		]
  )
  ;; Thanks for the opportunity to apply!

(defn page [request]
  (html5
   (header request)
   (instructions request)
   (address-form request)
   (if (seq (:form-params request)) ;; Don't show result information unless there are form params passed in.
    (search-results request))))
