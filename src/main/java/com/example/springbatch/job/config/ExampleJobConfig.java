package com.example.springbatch.job.config;

import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

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
  public DataSource dataSource;

  @Bean
  public Job ExampleJob() {
    Job exampleJob = jobBuilderFactory.get("exampleJob")
        .start(Step())
        .build();

    return exampleJob;
  }

  @Bean
  @JobScope
  public Step Step() throws Exception {
    return stepBuilderFactory.get("Step")
        .<Member, Member>chunk(10)
        .reader(reader())
        .processor(processor())
        .writer(writer())
        .build();
  }

  @Bean
  @StepScope
  public JdbcPagingItemReader<Member> reader() throws Exception {

    Map<String, Object> parameterValues = new HashMap<>();
    parameterValues.put("amount", "10000");

    //pageSize와 fetchSize는 동일하게 설정

    return new JdbcPagingItemReaderBuilder<Member>()
        .pageSize(10)
        .fetchSize(10)
        .dataSource(new BeanPropertyRowMapper<>(Member.class))
        .queryProvider(customQueryProvider())
        .parameterValues(parameterValues)
        .name("JdbcPagingItemReader")
        .build();
  }

  @Bean
  @StepScope
  public ItemProcessor<Member, Member> processor() {
    return new ItemProcessor<Member, Member>() {
      @Override
      public Member process(Member member) throws Exception {

        //1000원 추가 적립
        member.setAmount(member.getAmount() + 1000);

        return member;
      }
    };
  }


  @Bean
  @StepScope
  public JdbcBatchItemWriter<Member> writer() {
    return new JdbcBatchItemWriterBuilder<Member>()
        .dataSource(dataSource)
        .sql("UPDATE MEMBER SET AMOUNT = :amount WHERE ID = :id")
        .beanMapped()
        .build();
  }

  public PagingQueryProvider customQueryProvider() throws Exception {
    SqlPagingQueryProviderFactoryBean queryProviderFactoryBean = new SqlPagingQueryProviderFactoryBean();

    queryProviderFactoryBean.setDataSource(dataSource);

    queryProviderFactoryBean.setSelectClause("SELECT ID, NAME, EMAIL, NICK_NAME, STATUS, AMOUNT ");
    queryProviderFactoryBean.setFromClause("FROM MEMBER ");
    queryProviderFactoryBean.setWhereClause("WHERE AMOUNT >= :amount");

    Map<String, Order> sortKey = new HashMap<>();
    sortKey.put("id", Order.ASCENDING);

    queryProviderFactoryBean.setSortKeys(sortKey);

    return queryProviderFactoryBean.getObject();
  }

}
