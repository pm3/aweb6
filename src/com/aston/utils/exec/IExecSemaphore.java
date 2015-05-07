package com.aston.utils.exec;

public interface IExecSemaphore {

	public void acquire() throws InterruptedException;

	public void release();
}
