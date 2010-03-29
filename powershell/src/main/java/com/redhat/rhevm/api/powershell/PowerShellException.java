package com.redhat.rhevm.api.powershell;

public class PowerShellException extends RuntimeException
{
	private static final long serialVersionUID = -2950168057048256815L;

	public PowerShellException(String message, Throwable cause) {
		super(message, cause);
	}

	public PowerShellException(String message) {
		super(message);
	}
}
