package br.com.cdsoft.transaction.observer;

public interface TransactionObserver<E> {

    void doObserver(E item);
}
