package com.andrulban.sandbox.stripe.service;

public interface EmailService {

  void sendPasswordRecoveryMail(String recipientEmail, String title, String resetToken);
}
