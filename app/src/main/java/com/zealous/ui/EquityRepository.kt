package com.zealous.ui

import rx.Completable

/**
 * Responsible for loading stocks into the the database
 *
 * Created by yaaminu on 4/7/18.
 */

class EquityRepository {

    fun loadStocks(): Completable {
        return StockLoader().doLoad()
    }

}