package nextstep.subway.section.domain;

import nextstep.subway.section.exception.CanNotConnectSectionException;
import nextstep.subway.station.domain.Station;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Embeddable
public class Sections {

    @OneToMany(cascade =  CascadeType.ALL)
    @JoinColumn(name = "lineId")
    private List<Section> sections = new ArrayList<>();

    public Sections(List<Section> sections) {
        this.sections = requireNonNull(sections, "sections");
    }

    public Sections() {
    }

    public void add(Section other) {
        requireNonNull(other, "section");
        if (!sections.isEmpty()) {
            Section section = getConnectableSection(other);
            section.relocate(other);
        }
        sections.add(other);
    }

    private Section getConnectableSection(Section section) {
        return sections.stream()
                       .filter(s -> s.isConnectable(section))
                       .findFirst()
                       .orElseThrow(CanNotConnectSectionException::new);
    }

    public List<Station> getStations() {
        return getSectionsInOrder();
    }

    private List<Station> getSectionsInOrder() {
        Map<Station, Station> map = sections.stream()
                                            .collect(Collectors.toMap(Section::getUpStation, Section::getDownStation));
        Station station = findUpStationOfHeadSection();
        List<Station> stations = new ArrayList<>();
        while (map.get(station) != null) {
            stations.add(station);
            station = map.get(station);
        }
        stations.add(station);
        return stations;
    }

    private Station findUpStationOfHeadSection() {
        Set<Station> upStations = sections.stream()
                                          .map(Section::getUpStation)
                                          .collect(Collectors.toSet());
        Set<Station> downStations = sections.stream()
                                            .map(Section::getDownStation)
                                            .collect(Collectors.toSet());
        return upStations.stream()
                         .filter(upStation -> !downStations.contains(upStation))
                         .findFirst()
                         .orElseThrow(IllegalArgumentException::new);
    }
}
