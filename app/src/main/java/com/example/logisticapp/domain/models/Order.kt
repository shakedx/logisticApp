package com.example.logisticapp.domain.models

import java.util.Queue

class Order {
    var start: Point? = null
    var finish: Point? = null

    var nameStart: String? = null
    var nameFinish: String? = null
    var descProduct: String? = null
    var executor: String? = null
    var status: String? = null
    var queue: Int? = null

    constructor() // Пустой конструктор для использования в Firebase

    constructor(
        start: Point?,
        finish: Point?,
        nameStart: String?,
        nameFinish: String?,
        descProduct: String?,
        executor: String?,
        status: String?,
        queue: Int?
    ) {
        this.start = start
        this.finish = finish
        this.nameStart = nameStart
        this.nameFinish = nameFinish
        this.descProduct = descProduct
        this.executor = executor
        this.status = status
        this.queue = queue
    }
}
