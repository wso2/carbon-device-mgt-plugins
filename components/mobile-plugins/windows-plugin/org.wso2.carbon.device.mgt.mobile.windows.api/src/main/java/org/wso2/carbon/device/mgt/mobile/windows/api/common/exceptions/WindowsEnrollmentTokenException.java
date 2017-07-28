package org.wso2.carbon.device.mgt.mobile.windows.api.common.exceptions;


public class WindowsEnrollmentTokenException extends Exception {
    private static final long serialVersionUID = -2297311387874900305L;
    private String errorMessage;

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public WindowsEnrollmentTokenException(String msg, Exception nestedEx) {
        super(msg, nestedEx);
        setErrorMessage(msg);
    }

    public WindowsEnrollmentTokenException(String message, Throwable cause) {
        super(message, cause);
        setErrorMessage(message);
    }

    public WindowsEnrollmentTokenException(String msg) {
        super(msg);
        setErrorMessage(msg);
    }

    public WindowsEnrollmentTokenException() {
        super();
    }

    public WindowsEnrollmentTokenException(Throwable cause) {
        super(cause);
    }
}
