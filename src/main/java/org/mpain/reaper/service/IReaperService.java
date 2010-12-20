package org.mpain.reaper.service;

public interface IReaperService {

	public abstract void open(String source);

	public abstract void close();

	public abstract int getPagesCount();

	public abstract String getContent();

}