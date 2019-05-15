package de.sscholz.util

object LongTexts {
    val credits = """
        For my awesome friend Martin :)
        You are the best friend anybody could ever ask for!

         - Swante Scholz, 2019

        ======================

        Used libraries:
          - libGDX/KTX
        Music/Sounds:
          - "Boom Kick" by Goup_1
            (https://freesound.org/people/Goup_1/
            sounds/195396/)
        Additional art:
          - hidden, by Guigui
            (https://www.contextfreeart.org/gallery2/
            #design/2016)
          - dreaming, by mountain
            (https://www.contextfreeart.org/gallery2/
            #design/2508)

        To all my friends and family who playtested this game: Thank you!
    """.trimIndent()
    val howToPlay = """
        The idea of this app is to play a couple of classical board games while strolling
        through your neighborhood (or perhaps the countryside).
        You can only make a move on a square where you physically walked to (based on your
        GPS position).

        E.g.: In order to make a move on a square in the top-right corner, you might have
        to physically walk about 100 meter north and 100 meter east.

        In Player VS Player mode, two players can discover the world together. The player
        whose turn it is deciding on which direction to go.

        The next level, however, can only be unlocked in Player VS AI mode.


        === Controls ===

        Tap on a square to make a move there.

        In normal mode, you must physically stand on that square (based on your GPS position),
        in order to make the move. In training mode, you can make a move independent of your
        location.

        However, you won't be able to unlock new levels while in training mode!

        You can switch between training and normal (GPS) mode in the Settings. There you can
        also mute/unmute the game, and change the distance in meter that a square represents.

        When playing a game, there are the following buttons at the top, from left to right:

        Info: See the game rules and winning conditions.

        Recenter: Resets you location to the square of your last move. (Useful when you are out of bounds.)

        Rotate: Rotates the board by 90 degrees. This way you're e.g. able to reach the bottom
        squares while physically walking north (after rotating twice). Note though, that
        rotating also resets your location to the square of your last move. Thus you should usually
        first plan out which rotation you want to use for your next move, before starting to walk there.

        New Game: Starts a new game.


        Have fun exploring!
    """.trimIndent().rearrangeNewlines()
}