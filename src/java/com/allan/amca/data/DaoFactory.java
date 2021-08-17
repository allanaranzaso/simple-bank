package com.allan.amca.data;

import com.allan.amca.account.AccountDaoImpl;
import com.allan.amca.enums.DaoType;
import com.allan.amca.transaction.TransactionDaoImpl;
import com.allan.amca.user.UserDaoImpl;

public class DaoFactory<T, N> {
// generic method for creating DAO
    public static Dao createDao(final DaoType type) {
        final Dao factory;

        switch (type) {
            case USER -> factory = new UserDaoImpl();
            case ACCOUNT -> factory = new AccountDaoImpl();
            case TRANSACTION -> factory = new TransactionDaoImpl();
            default -> throw new IllegalStateException("DAO does not exist");
        }
        return factory;
    }
}
