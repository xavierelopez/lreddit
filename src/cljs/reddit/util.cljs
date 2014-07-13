(ns reddit.util)

(defn unescape-html [html-text]
  (if (nil? html-text)
    ""
    (-> html-text
        (str/replace "&amp;" "&")
        (str/replace "&lt;" "<")
        (str/replace "&gt;" ">")
        (str/replace "&quot;" "\""))))