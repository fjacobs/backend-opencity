package com.dynacore.livemap.traveltime.repo;

import com.dynacore.livemap.core.PubDateSizeResponse;
import com.dynacore.livemap.core.repository.GeometryEntity;
import com.dynacore.livemap.core.repository.TrafficRepository;
import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.bool.BooleanUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

import static org.springframework.data.r2dbc.query.Criteria.where;

@Profile("traveltime")
@Repository("travelTimeRepository")
public class TravelTimeRepo implements TrafficRepository<TravelTimeEntityImpl> {

    private final DatabaseClient r2dbcClient;
    private static final Logger logger = LoggerFactory.getLogger(TravelTimeRepo.class);

    public TravelTimeRepo(DatabaseClient databaseClient) {
        this.r2dbcClient = databaseClient;
    }

    @Override
    public Mono<Boolean> isNew(TravelTimeEntityImpl entity) {

        return r2dbcClient
                .select()
                .from(TravelTimeEntityImpl.class)
                .matching(where("id").is(entity.getId()).and("pubDate").is(entity.getPubDate()))
                .fetch()
                .first()
                .hasElement()
                .as(BooleanUtils::not);
    }

    @Override
    public Mono<Void> save(TravelTimeEntityImpl entity) {

        return Mono.just(entity)
                .flatMap(x-> isNew(x).filter(y->y).map(j->entity))
                .map(
                        newEntity ->
                                r2dbcClient
                                        .insert()
                                        .into(TravelTimeEntityImpl.class)
                                        .using(newEntity)
                                        .fetch()
                                        .rowsUpdated())
                .then();
    }

    public Mono<Void> saveGeometry(GeometryEntity geometryEntity) {
        return r2dbcClient
                .execute("UPDATE geometries(id, geo_type, data_type, geom) VALUES( '" + geometryEntity.id() + "', '" + geometryEntity.geo_type() + "', '" + geometryEntity.data_type() + "', ST_GeomFromText('" + geometryEntity.geom().toString() + "',4326));")
                .fetch()
                .rowsUpdated()
                .then();
    }
    public record IdLoc(String id, Geometry geom){};
    public Flux<IdLoc> getLocationsWithin(double yMin, double xMin, double yMax, double xMax) {
           return r2dbcClient.execute("SELECT id, geom FROM geometries where geom && ST_MakeEnvelope(" + yMin + ", "+ xMin +"," + yMax + "," + xMax + ", 4326)")
                    .map((row, rowMetadata) -> new IdLoc((String) row.get(0), (Geometry) row.get(1)) )
            .all();
    }

    @Override
    public Mono<TravelTimeEntityImpl> getLatest(String entityId) {
        return r2dbcClient
                .execute(
                        "     SELECT id, name, pub_date, our_retrieval, type, length, travel_time, velocity \n"
                                + "     FROM public.travel_time_entity\n"
                                + "\t   WHERE pub_date=(\n"
                                + "                SELECT MAX(pub_date) FROM public.travel_time_entity WHERE id='"
                                + entityId
                                + "');")
                .as(TravelTimeEntityImpl.class)
                .fetch()
                .first();
    }

    @Override
    public Flux<TravelTimeEntityImpl> getAllAscending() {
        return r2dbcClient
                .execute("SELECT * FROM public.travel_time_entity ORDER BY pub_date ASC")
                .as(TravelTimeEntityImpl.class)
                .fetch()
                .all();
    }

    @Override
    public Flux<PubDateSizeResponse> getReplayMetaData() {
        return r2dbcClient
                .execute(
                        " SELECT pub_date, COUNT (pub_date) FROM public.travel_time_entity GROUP BY travel_time_entity.pub_date ORDER BY pub_date ASC;")
                .as(PubDateSizeResponse.class)
                .fetch()
                .all();
    }

    @Override
    public Flux<PubDateSizeResponse> getReplayMetaData(OffsetDateTime start, OffsetDateTime end) {
        return r2dbcClient
                .execute(
                        "    SELECT pub_date, COUNT (pub_date)\n"
                                + "    FROM\n"
                                + "             public.travel_time_entity\n"
                                + "    WHERE\n"
                                + "    pub_date >= '"
                                + start
                                + "'\n"
                                + "    AND    pub_date <='"
                                + end
                                + "'\n"
                                + "\n"
                                + "    GROUP BY travel_time_entity.pub_date ORDER BY pub_date ASC;")
                .as(PubDateSizeResponse.class)
                .fetch()
                .all();
    }

    @Override
    public Flux<TravelTimeEntityImpl> getFeatureDateRange(OffsetDateTime start, OffsetDateTime end) {
        return r2dbcClient
                .execute(
                        "    SELECT pub_date, COUNT (pub_date)\n"
                                + "    FROM\n"
                                + "             public.travel_time_entity\n"
                                + "    WHERE\n"
                                + "    pub_date >= '"
                                + start
                                + "'\n"
                                + "    AND    pub_date <= '"
                                + end
                                + "'\n"
                                + "\n"
                                + "    GROUP BY travel_time_entity.pub_date ORDER BY pub_date DESC;")
                .as(TravelTimeEntityImpl.class)
                .fetch()
                .all();
    }
}
