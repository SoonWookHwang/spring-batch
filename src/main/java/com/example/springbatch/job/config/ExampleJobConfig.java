package com.example.springbatch.job.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
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
        .start(Step())
        .build();

    return exampleJob;
  }

  @Bean
  public Step Step() {
    return stepBuilderFactory.get("step")
        .tasklet((contribution, chunkContext) -> {
          log.info("Step!");
          return RepeatStatus.FINISHED;
        })
        .build();
  }
  /*
  Job Example 2 - 다중 스텝 구성하기
  */

  @Bean
  public Job ExampleJob2() {
    Job exampleJob2 = jobBuilderFactory.get("exampleJob")
        .start(startStep())
        .next(nextStep())
        .next(lastStep())
        .build();

    return exampleJob2;
  }

  @Bean
  public Step startStep() {
    return stepBuilderFactory.get("startStep")
        .tasklet((contribution, chunkContext) -> {
          log.info("start step");
          return RepeatStatus.FINISHED;
        })
        .build();
  }

  @Bean
  public Step nextStep() {
    return stepBuilderFactory.get("nextStep")
        .tasklet((contribution, chunkContext) -> {
          log.info("Next Step!");
          return RepeatStatus.FINISHED;
        })
        .build();
  }
  @Bean
  public Step lastStep() {
    return stepBuilderFactory.get("lastStep")
        .tasklet((contribution, chunkContext) -> {
          log.info("Last Step!");
          return RepeatStatus.FINISHED;
        })
        .build();
  }

}
