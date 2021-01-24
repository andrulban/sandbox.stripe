package com.andrulban.sandbox.stripe.exception;

public class ApiException extends RuntimeException {

  private ExceptionType type;

  public ApiException() {}

  public ApiException(Throwable cause) {
    super(cause);
  }

  public ApiException(ExceptionType type) {
    this.type = type;
  }

  public ApiException(String message, ExceptionType type) {
    super(message);
    this.type = type;
  }

  public ApiException(String message, Throwable cause, ExceptionType type) {
    super(message, cause);
    this.type = type;
  }

  public ApiException(Throwable cause, ExceptionType type) {
    super(cause);
    this.type = type;
  }

  public ExceptionType getStatus() {
    return type;
  }
}
