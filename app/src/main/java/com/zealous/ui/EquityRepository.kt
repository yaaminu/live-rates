package com.zealous.ui

import rx.Completable
import rx.Observable

/**
 * Responsible for loading stocks into the the database
 *
 * Created by yaaminu on 4/7/18.
 */

class EquityRepository {

    fun loadStocks(): Completable {
        return StockLoader().doLoad()
    }

    fun loadLastNDaysPriceFor(symbol: String, days: Int): Observable<Pair<String, List<Double>>> {
        return StockLoader().doLoadLastNDays(symbol, days)
    }
}