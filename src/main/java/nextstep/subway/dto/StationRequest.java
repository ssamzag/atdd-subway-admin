package nextstep.subway.dto;

import nextstep.subway.domain.Station;

public class StationRequest {
    private String name;

    public String getName() {
        return name;
    }

    private StationRequest() {
    }

    public Station toStation() {
        return new Station(name);
    }
}
