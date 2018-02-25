package com.totalnihon.ubs.connect4.infrastructure.exception;

public class ColumnIsFullException extends Exception {
	public ColumnIsFullException(String msg) {
		super(msg);
	}
}
