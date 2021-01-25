package com.andrulban.sandbox.stripe.service.impl;

import com.andrulban.sandbox.stripe.service.ChargeService;
import com.google.common.collect.ImmutableMap;
import com.stripe.exception.*;
import com.stripe.model.Charge;
import org.springframework.stereotype.Service;

@Service
public class ChargeServiceImpl implements ChargeService {

  @Override
  public Charge charge(ImmutableMap<String, Object> params)
      throws CardException, APIException, AuthenticationException, InvalidRequestException,
          APIConnectionException {
    return Charge.create(params);
  }
}
