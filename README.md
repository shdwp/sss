# sss

Sci-Fi roguelike in space written in clojure. In development.
All functions and namespaces are documented (except for some in hard development), additional help on project structure and terms you can find in [docs](https://github.com/ShadowPrince/sss/blob/master/doc/intro.md)

## Usage

Common leiningen project.

    $ lein deps
    $ lein run

There is some helpers for REPL in core namespace:
    
    (s!) starts game
    (!) stops game (interrupt all threads and stop lanterna's screen)

## License

Copyright © 2013 ShadowPrince

Distributed under the Eclipse Public License, the same as Clojure.
