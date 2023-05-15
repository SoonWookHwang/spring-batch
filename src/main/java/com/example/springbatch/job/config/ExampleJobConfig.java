package com.example.springbatch.job.config;

import java.sql.SQLException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Slf4j
@Configuration
@EnableBatchProcessing
public class ExampleJobConfig {

  @Autowired
  public JobBuilderFactory jobBuilderFactory;
  @Autowired
  public StepBuilderFactory stepBuilderFactory;

  /*
  Job Example 1 - 단일 스텝 구성하기
  */
  @Bean
  public Job ExampleJob() {
    Job exampleJob = jobBuilderFactory.get("exampleJob")
        .start(startStep())
        .on("FAILED")     // startStep의 ExistStatus가 FAILED일 경우
        .to(failOverStep())       //  failOver Step을 실행시킨다
        .on("*")           //  failOver Step의 결과와 상관없이
        .to(writeStep())          //   writeStep을 실행시킨다
        .on("*")           //  writeStep의 결과와 상과없이
        .end()                    // Flow를 종료시킨다.

        .from(startStep())          // startStep이 FAILED가 아니고
        .on("COMPLETED")   // COMPLETE일 경우
        .to(processStep())        //  process Step을 실행시키다
        .on("*")           // process Step의 결과와 상과없이
        .to(writeStep())          //  writeStep을 실행시킨다
        .on("*")          //  writeStep의 결과와 상관없이
        .end()//Flow를 종료시킨다

        .from(startStep())          //  startStep의 결과가 FAILED, COMPLETE가 아닌
        .on("*")          //  모든 경우
        .to(writeStep())          //  writeStep을 실행시킨다
        .on("*")          //  writeStep의 결과와 상관없이
        .end()                    //  Flow를 종료시킨다.
        .end()
        .build();

    return exampleJob;
  }

  @Bean
  public Step startStep() {
    return stepBuilderFactory.get("startStep")
        .tasklet((contribution, chunkContext) -> {
          log.info("start step!");

          String result = "COMPLETE";
          //String result = "FAIL";
          //String result = "UNKNOWN";

          //Flow에서 on은 ReapeatStatus가 아닌 ExitStatus를 바라본다.

          if (result.equals("COMPLETE")) {
            contribution.setExitStatus(ExitStatus.COMPLETED);
          } else if (result.equals("FAIL")) {
            contribution.setExitStatus(ExitStatus.FAILED);
          } else if (result.equals("UNKNOWN")) {
            contribution.setExitStatus(ExitStatus.UNKNOWN);
          }

          return RepeatStatus.FINISHED;
        })
        .build();
  }

  @Bean
  public Step failOverStep() {
    return stepBuilderFactory.get("nextStep")
        .tasklet((contribution, chunkContext) -> {
          log.info("FailOver Step!");
          return RepeatStatus.FINISHED;
        })
        .build();
  }

  @Bean
  public Step processStep() {
    return stepBuilderFactory.get("processStep")
        .tasklet((contribution, chunkContext) -> {
          log.info("Process Step!");
          return RepeatStatus.FINISHED;
        })
        .build();
  }
  @Bean
  public Step writeStep() {
    return stepBuilderFactory.get("writeStep")
        .tasklet((contribution, chunkContext) -> {
          log.info("Write Step!");
          return RepeatStatus.FINISHED;
        })
        .build();
  }

  @Bean
  @JobScope
  public Step Step1() throws Exception {
    return stepBuilderFactory.get("Step")
        .startLimit(3)
        .<Member,Member>chunk(10)
        .reader(reader(null))
        .processor(processor(null))
        .writer(writer(null))
        .build();
  }

  @Bean
  @JobScope
  public Step Step2() throws Exception {
    return stepBuilderFactory.get("Step")
        .<Member,Member>chunk(10)
        .reader(reader(null))
        .processor(processor(null))
        .writer(writer(null))
        .faultTolerant()
        .skipLimit(1) // skip 허용 횟수, 해당 횟수 초과시 Error 발생, Skip 사용시 필수 설정
        .skip(NullPointerException.class)// NullPointerException에 대해선 Skip
        .noSkip(SQLException.class) // SQLException에 대해선 noSkip
        //.skipPolicy(new CustomSkipPolilcy) // 사용자가 커스텀하며 Skip Policy 설정 가능
        .build();
  }

  @Bean
  @JobScope
  public Step Step3() throws Exception {
    return stepBuilderFactory.get("Step")
        .<Member,Member>chunk(10)
        .reader(reader(null))
        .processor(processor(null))
        .writer(writer(null))
        .faultTolerant()
        .retryLimit(1) //retry 횟수, retry 사용시 필수 설정, 해당 Retry 이후 Exception시 Fail 처리
        .retry(SQLException.class) // SQLException에 대해선 Retry 수행
        .noRetry(NullPointerException.class) // NullPointerException에 no Retry
        //.retryPolicy(new CustomRetryPolilcy) // 사용자가 커스텀하며 Retry Policy 설정 가능
        .build();
  }

}
