package nextstep.subway.section.domain;

import nextstep.subway.core.domain.BaseEntity;
import nextstep.subway.line.domain.Line;
import nextstep.subway.station.domain.Distance;
import nextstep.subway.station.domain.Station;
import javax.persistence.*;

import static java.util.Objects.requireNonNull;

@Entity
public class Section extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long lineId;

    @ManyToOne
    private Station upStation;

    @ManyToOne
    private Station downStation;

    @Embedded
    private Distance distance;

    public Section(Station upStation, Station downStation, Long distance) {
        this.upStation = requireNonNull(upStation, "상행 지하철역이 비었습니다");
        this.downStation = requireNonNull(downStation, "하행 지하철역이 비었습니다");
        this.distance = new Distance(distance);
    }

    protected Section() {
    }

    public void bindLine(Long lineId) {
        this.lineId = lineId;
    }

    public boolean isConnectable(Section other) {
        boolean upConnected = isConnectable(this.upStation, other);
        boolean downConnected = isConnectable(this.downStation, other);
        return !((upConnected && downConnected) || (!upConnected && !downConnected));
    }

    private boolean isConnectable(Station station, Section otherSection) {
        return station.equals(otherSection.getUpStation()) || station.equals(otherSection.getDownStation());
    }

    public void relocate(Section section) {
        if (this.upStation.equals(section.upStation) && !this.downStation.equals(section.downStation)) {
            relocateUpStation(section);
            return;
        }
        if (this.downStation.equals(section.downStation) && !this.upStation.equals(section.upStation)) {
            relocateDownStation(section);
        }
    }

    private void relocateUpStation(Section section) {
        distance.minus(section.distance);
        Station tempStation = this.downStation;
        this.downStation = section.downStation;
        section.upStation = this.downStation;
        section.downStation = tempStation;
    }

    private void relocateDownStation(Section section) {
        distance.minus(section.distance);
        this.downStation = section.upStation;
    }

    public boolean matchesUpStation(Station other) {
        return this.upStation.equals(other);
    }

    public boolean matchesDownStation(Station other) {
        return this.downStation.equals(other);
    }

    public void merge(Section section) {
        this.downStation = section.downStation;
        this.distance.plus(section.distance);
    }

    public Station getUpStation() {
        return upStation;
    }

    public Station getDownStation() {
        return downStation;
    }

    public Distance getDistance() {
        return distance;
    }

    public Long getLineId() {
        return lineId;
    }
}
