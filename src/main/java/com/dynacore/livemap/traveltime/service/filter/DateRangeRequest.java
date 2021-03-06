package com.dynacore.livemap.traveltime.service.filter;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 *  Request class for filtering a stream of features with an interval between each new pubDate.
 *
 */
@Data
@NoArgsConstructor
public class DateRangeRequest {

    private OffsetDateTime startDate;
    private OffsetDateTime endDate;
}
