package com.zealous.expense;

import android.content.Context;
import android.support.annotation.NonNull;

import com.zealous.utils.Config;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by yaaminu on 4/8/17.
 */

@Module
@Singleton
public class BaseExpenditureProvider {

    @Provides
    public Realm getExpenditureRealm(@NonNull RealmConfiguration configuration) {
        return Realm.getInstance(configuration);
    }

    @Provides
    @Singleton
    public RealmConfiguration getConfiguration() {
        return new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .name("expenditure.realm")
                .modules(new ExpenditureRepo())
                .directory(Config.getApplicationContext().getDir("expenditures.data", Context.MODE_PRIVATE)).build();
    }

    @Provides
    public ExpenditureDataSource createDataSource(Realm realm) {
        return new ExpenditureDataSource(realm);
    }
}
