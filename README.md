lReddit
==========
Simple reddit client that removes most user input. It's intended for users who just like to lurk on text-heavy subs.

### Purpose ###
The purpose of publishing this is mostly didactic, for myself and others. Coming from a heavy js/coffee frontend background I wanted to try Clojurescript and Om to see how different the experience would be in writing a SPA. I found many blog posts, forum threads, hacker news threads, and reddit posts that helped me along the way; hopefully this helps people who want to see another small example of how everything works together.

### Takeaway ###
It was fun to learn clojure and formally introduce myself to concepts like functional programming, channels, and immutable data structures. I'll probably be bringing some of the things I've learned to my js development, and hope to use clj and cljs for future projects, especially the latter as it continues to grow.

### Future of this project ###
I don't feel this project is complete until more effort is put into the UI, it's pretty barebones right now. If you have any tips or ideas in mind, or want to contribute, let me know.

#### For a live version of this, please visit ####
Visit http://sheltered-earth-1060.herokuapp.com

#### For development: ####
    lein ring server-headless
    lein cljsbuild auto
or

#### For local build:####
    lein run 8000
    lein cljsbuild once
And then go to localhost:8000 on your web browser.