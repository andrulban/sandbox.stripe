package com.andrulban.sandbox.stripe.service;

import com.google.common.collect.ImmutableMap;
import com.stripe.exception.*;
import com.stripe.model.Charge;

public interface ChargeService {

  Charge charge(ImmutableMap<String, Object> params)
      throws CardException, APIException, AuthenticationException, InvalidRequestException,
          APIConnectionException;
}
