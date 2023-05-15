package com.example.springbatch.job.config;

import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
전체 금액이 10000원 이상인 회원들에게 1000원 캐시백을 주는 배치
 */
@Slf4j
@Configuration
@EnableBatchProcessing
public class ExampleJobConfig {

  @Autowired
  public JobBuilderFactory jobBuilderFactory;
  @Autowired
  public StepBuilderFactory stepBuilderFactory;
  @Autowired
  public EntityManagerFactory entityManagerFactory;

  @Bean
  public Job ExampleJob() throws Exception {

    Job exampleJob = jobBuilderFactory.get("exampleJob")
        .start(Step())
        .build();

    return exampleJob;
  }

  @Bean
  @JobScope
  public Step Step() throws Exception {
    return stepBuilderFactory.get("Step")
        .<Member, Memeber>chunk(10)
        .reader(reader())
        .processor(processor())
        .writer(writer())
        .build();
  }

  @Bean
  @StepScope
  public JpaPagingItemReader<Member> reader() throws Exception {
    Map<String, Object> parameterValues = new HashMap<>();
    parameterValues.put("amount", 10000);

    return new JpaPagingItemReaderBuilder<Member>()
        .pageSize(10)
        .parameterValues(parameterValues)
        .queryString("SELECT p FROM Member p WHERE p.amount >= :amount ORDER BY id ASC")
        .entityManagerFactory(entityManagerFactory)
        .name("JpaPagingItemReader")
        .build();
  }

  @Bean
  @StepScope
  public ItemProcessor<Member, Member> processor() {
    return new ItemProcessor<Member, Member>() {
      @Override
      public Member process(Member item) throws Exception {

        //1000원 추가적립
        member.setAmount(memeber.getAmount() + 1000);

        return memeber;
      }
    };
  }

  @Bean
  @StepScope
  public JpaItemWriter<Member> writer() {
    return new JpaItemWriterBuilder<Member>()
        .entityManagerFactory(entityManagerFactory)
        .build();
  }
}
