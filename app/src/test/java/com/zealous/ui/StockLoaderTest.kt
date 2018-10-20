package com.zealous.ui

import org.junit.Test
import rx.Emitter
import rx.Observable
import rx.Observable.range
import rx.functions.Action1
import rx.schedulers.Schedulers
import rx.subjects.PublishSubject
import rx.subjects.ReplaySubject
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by yaaminu on 5/5/18.
 */
class StockLoaderTest {
    @Test
    fun doLoad() {
        val atomicInt = AtomicInteger(0)
        val observable = Observable.fromEmitter(Action1<Emitter<Any>> { subject ->
            println("subscribed")
            subject.onNext("hello")
            subject.onError(Exception("${atomicInt.incrementAndGet()}"))
            subject.onNext("passed")
            subject.onCompleted()
        }, Emitter.BackpressureMode.BUFFER)


        observable.repeatWhen {
            println("retryWhen")
            it.flatMap {
                Observable.timer(5L, TimeUnit.SECONDS, Schedulers.immediate())
            }.take(3)
        }.subscribe(::println, ::println) {
            println("completed")
        }

    }

    @Test
    fun testZip() {
        val subject = ReplaySubject.create<String>()
        val subject2 = PublishSubject.create<String>()

        subject2.asObservable().concatMap {
            Observable.from(it.split("i"))
        }
                .observeOn(Schedulers.io())
                .subscribe(::println, ::println) {
                    println("completed")
                }

        subject.onNext("first")
        subject2.onNext("first2")
        subject.onNext("second")
        subject2.onNext("second2")
        subject.onNext("third")
//        subject2.onError(Throwable(""))
        subject2.onNext("third2")
        subject2.onNext("fourth2")
        subject.onNext("fourth")
        subject2.onCompleted()
        subject.onCompleted()
    }

    @Test
    fun test2() {
        val subject = PublishSubject.create<String>()
        subject.concatWith(range(1, 3).map { "$it" })
                .subscribe({
                    println(it)
                }, ::println) {
                    println("completed")
                }

        subject.onNext("first")
        subject.onNext("second")
        subject.onNext("third")
        subject.onNext("fourth")
        subject.onCompleted()
    }

}