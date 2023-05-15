package com.example.springbatch.job.service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomService {

  public void businessLogic() {
    //비즈니스 로직
    for (int idx = 0; idx < 10; idx++) {
      log.info("[idx] = " + idx);
    }
  }

}
