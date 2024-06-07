package com.example.logisticapp.domain.models

import java.io.Serializable

class Point : Serializable {
    var lat: Double = 0.0
    var lon: Double = 0.0

    constructor()

    constructor(lat: Double, lon: Double) {
        this.lat = lat
        this.lon = lon
    }
}