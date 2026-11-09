package com.zenith.player.domain.model

enum class RepeatMode {
    /** Al terminar la cola, se detiene. */
    Off,

    /** Al terminar la cola, vuelve a empezar. */
    All,

    /** Repite la canción actual. */
    One,
    ;

    /** Siguiente modo al pulsar el botón: desactivado → toda la cola → una canción → desactivado. */
    fun next(): RepeatMode = when (this) {
        Off -> All
        All -> One
        One -> Off
    }
}
