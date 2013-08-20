# Namespaces:
* sss
    * entity - entity system. There`ll be entity logic
        * core - core ns for entity system
        * actor - entity designed for be controlled by player

    * game - game logic
        * core - fn`s to start, stop the game, low-level game logick (that works in each part of the game)
        * game - root game dispatcher. Handles basic rendering and behavior
        * gamestate - bunch of fn`s to handle gamestate object
        * gmap - bunch of helper fn`s usable in gmap part of game
        * console - game logic and view of consoles
            * core - basic console game dispatcher, handles basic interface and behavior
            * cc - command center console, controls over maps and autopilotting
            * direct-control - console for direct controlling of the ship
        * ship - game logic and view of "ship" part of game
            * core - game dispatcher for this

    * gmap - game map (tilemap)
        * core - fn`s to create and work with gmaps

    * graphics - sss`s own project graphics
        * gui - primitive gui
            * core - fn`s to help building gui
            * term - primitive in-game terminal
        * bitmap - working with bitmaps
        * canvas - working with canvases
        * viewport - cropping and positioning bitmaps
        * core - some basic graphics primitives 

    * gui - backends for outputting graphics and inputting keyboard events
        * lanterna-clojure - main backend, using lanterna-clojure
        
    * ship - ship system.
        * core - general functions 
        * autopilot - fn`s to autopilot ships
        * form - currently unused
        * gen - generating ships 
        * geom - helper fn`s to figure out in ship geometry
        * interior - for putting things like doors

    * tile - tiles for gmap (should be merged with gmap)
        * core - general functions
        * ship-material - some usable tiles to build ships

    * universe - generate and work with universe
        * planet - generate and work with planets
            * core - general functions
        * social - social aspect of universe
            * core - general functions - gen races, unions
            * history - generate history of races and apply political state to universe
            * ligvo - for names generating
        * space - generate and work with space 
            * core - general functions
        * system - generate and work with system
            * core - general functions
        * core - general functions and helpers
        * random - tiny library of random
    * core - (main)
