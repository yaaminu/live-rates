package com.zealous.ui

import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.content.ContextCompat
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.zealous.R
import com.zealous.equity.EQUITY
import com.zealous.stock.Equity
import io.realm.Realm


/**
 * Created by yaaminu on 4/11/18.
 */
class StockListService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory = StockListFactory(applicationContext)

}

class StockListFactory(val context: Context) : RemoteViewsService.RemoteViewsFactory {
    private var stocks: List<Equity> = emptyList()

    override fun onCreate() {

    }

    override fun getLoadingView(): RemoteViews? {
        return null
    }

    override fun getItemId(p0: Int): Long {
        return -1L
    }

    override fun onDataSetChanged() {
        Realm.getDefaultInstance()
                .use {
                    stocks = it.where(Equity::class.java).equalTo("isFavorite", true)
                            .findAllSorted("symbol")
                    stocks = it.copyFromRealm(stocks)
                }
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun getViewAt(position: Int): RemoteViews {
        val rv = RemoteViews(context.packageName, R.layout.home_screen_equity_list_item)
        val data = stocks[position]
        rv.setTextViewText(R.id.tv_symbol, data.symbol)
        rv.setTextViewText(R.id.tv_price, "GH₵${data.price}")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            val trend = when {
                data.change.startsWith("-") -> R.drawable.arrow_down_24dp
                data.change.startsWith("+") -> R.drawable.arrow_up_24dp
                else -> 0
            }
            rv.setTextViewCompoundDrawables(R.id.tv_price, trend, 0, 0, 0)
        }

        val color = ContextCompat.getColor(context, when {
            data.change.startsWith("-") -> R.color.bbc_color
            data.change.startsWith("+") -> R.color.green_dark
            else -> R.color.black
        })

        rv.setTextColor(R.id.tv_price, color)
        val fillInIntent = Intent()
        fillInIntent.putExtra(EQUITY, data)
        rv.setOnClickFillInIntent(R.id.home_stock_list_item_root, fillInIntent)

        return rv
    }

    override fun getCount(): Int {
        return stocks.size
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun onDestroy() {
    }

}