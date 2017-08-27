package org.projectbass.bass.model


data class LocationPoint(
    var label: String,
    var bandwidth: String,
    var signal: String,
    var loc: Loc
)

data class Loc(
    var lat: Double,
    var lng: Double
)
