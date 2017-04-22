package com.zealous.expense;

/**
 * Created by yaaminu on 4/8/17.
 */


@io.realm.annotations.RealmModule(classes = {
        Expenditure.class, ExpenditureCategory.class, Attachment.class
})
public class ExpenditureRepo {
}
