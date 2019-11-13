package br.com.cdsoft.transaction.config;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableDynamoDBRepositories
public class DynamoDBConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(DynamoDBConfig.class);


    public static final String TRANSACAO = "transacao";
    @Value("${amazon.aws.accesskey}")
    private String amazonAWSAccessKey;

    @Value("${amazon.aws.secretkey}")
    private String amazonAWSSecretKey;

    @Bean
    public AmazonDynamoDB amazonDynamoDB() {
        AmazonDynamoDB amazonDynamoDB = AmazonDynamoDBClientBuilder.standard()
                .withCredentials(amazonAWSCredentials())
                .withRegion(Regions.US_WEST_2)
                .build();


        return amazonDynamoDB;

    }

    @Bean
    public DynamoDB getDatabase(AmazonDynamoDB amazonDynamoDB) {

        return new DynamoDB(amazonDynamoDB);
    }

    @Bean
    public AWSCredentialsProvider amazonAWSCredentials() {

        BasicAWSCredentials awsCreds = new BasicAWSCredentials(amazonAWSAccessKey, amazonAWSSecretKey);
        return new AWSStaticCredentialsProvider(awsCreds);


    }
}
