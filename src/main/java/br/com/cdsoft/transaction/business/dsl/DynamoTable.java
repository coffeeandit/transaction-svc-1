package br.com.cdsoft.transaction.business.dsl;

import com.amazonaws.services.dynamodbv2.document.Table;

public interface DynamoTable<T extends Table> {


    T getTable();
}
