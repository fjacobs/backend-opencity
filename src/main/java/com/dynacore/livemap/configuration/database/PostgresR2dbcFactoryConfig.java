package com.dynacore.livemap.configuration.database;

import com.dynacore.livemap.configuration.database.postgiscodec.PostGisCodecRegistrar;
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.postgresql.extension.Extension;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

import static io.r2dbc.spi.ConnectionFactoryOptions.*;

@Profile("postgres")
@Configuration
@EnableR2dbcRepositories
public class PostgresR2dbcFactoryConfig extends AbstractR2dbcConfiguration {

  private PostgresConfiguration config;

  public PostgresR2dbcFactoryConfig(PostgresConfiguration config) {
    this.config = config;
  }

  @Bean
  public ConnectionFactory connectionFactory() {
//    return new PostgresqlConnectionFactory(
//                    PostgresqlConnectionConfiguration.builder()
//                            .host(config.getHost())
//                            .username( config.getUser())
//                            .port(config.getPort())
//                            .password(config.getPassword())
//                            .database(config.getDatabase())
//                            .codecRegistrar(new PostGisCodecRegistrar())
//                            .build());

    return ConnectionFactories.get(
            builder()
                    .option(DRIVER, "pool")
                    .option(PROTOCOL, "postgresql")
                    .option(HOST, config.getHost())
                    .option(USER, config.getUser())
                    .option(PORT, config.getPort())
                    .option(PASSWORD, config.getPassword())
                    .option(DATABASE, config.getDatabase())
                    .build());


  }

}
