package br.com.cdsoft.transaction.business.dsl;

import com.amazonaws.services.dynamodbv2.document.Item;

public interface InsertableItem<E> {


    Item putItem(E item);
}
