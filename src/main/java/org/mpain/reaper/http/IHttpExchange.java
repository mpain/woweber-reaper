package org.mpain.reaper.http;

public interface IHttpExchange {

	public abstract void executeGet(String address) throws HttpExchangeException;

	public abstract void executePost(String address, String xmlData) throws HttpExchangeException;

	public abstract HttpExchangeData getExchangeData();

	public void close();
}