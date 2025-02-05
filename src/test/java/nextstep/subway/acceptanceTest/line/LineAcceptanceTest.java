package nextstep.subway.acceptanceTest.line;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.dto.SectionResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("지하철 노선 관련기능 인수 테스트")
public class LineAcceptanceTest extends LineAcceptance {

    /**
     * Given 지하철역을 생성하고
     * When 지하철 노선을 생성하면
     * Then 지하철 노선이 생성된다
     * Then 지하철 노선 목록 조회 시 생성한 노선을 찾을 수 있다
     */
    @Test
    @DisplayName("지하철 노선을 생성한다")
    void createLine() {
        //when
        ExtractableResponse<Response> response =
                createLine("신분당선", "bg-red-600", "1", "2", "10");

        //Then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(getLines("name")).containsAnyOf("신분당선");
    }

    /**
     * Given 2개의 지하철 노선을 생성하고
     * When 지하철 노선 목록을 조회하면
     * Then 지하철 노선 목록 조회 시 2개의 노선을 조회할 수 있다.
     */
    @Test
    @DisplayName("지하철 노선을 두 개 생성하고 목록을 조회한다")
    void findAll() {
        //given
        createLine("신분당선", "bg-red-600", "1", "2", "10");
        createLine("분당선", "bg-green-600", "2", "3", "5");

        // when
        List<String> lines = getLines("name");

        // then
        assertThat(lines).hasSize(3).contains("2호선", "신분당선", "분당선");
    }

    /**
     * Given 지하철 노선을 생성하고
     * When 생성한 지하철 노선을 조회하면
     * Then 생성한 지하철 노선의 정보를 응답받을 수 있다.
     */
    @Test
    @DisplayName("생성한 지하철 노선을 조회한다")
    void findById() {
        //given
        createLine("신분당선", "bg-red-600", "1", "2", "10");

        //when
        List<String> lines = getLines("name");

        //then
        assertThat(lines).contains("신분당선");
    }

    /**
     * Given 지하철 노선을 생성하고
     * When 생성하지 않은 지하철 노선을 조회하면
     * Then 404를 응답받는다.
     */
    @Test
    @DisplayName("생성하지 않은 지하철을 조회한다")
    void notFound() {
        //given
        createLine("신분당선", "bg-red-600", "1", "2", "10");

        //when
        int statusCode = RestAssured.given().log().all()
                .when().get("/lines/2")
                .then().log().all()
                .extract().statusCode();

        //then
        assertThat(statusCode).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    /**
     * Given 지하철 노선을 생성하고
     * When 생성한 지하철 노선을 수정하면
     * Then 해당 지하철 노선 정보는 수정된다
     */
    @Test
    @DisplayName("지하철 노선정보를 수정한다")
    void changeLine() {
        //given
        createLine("신분당선", "bg-red-600", "1", "2", "10");

        //when
        Map<String, String> params = new HashMap<>();
        params.put("name", "천당 위에 분당선");
        params.put("color", "bg-god-7000");

        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .params(params)
                .when().put("/lines/1")
                .then().log().all()
                .extract();

        //then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("생성되지 않은 역으로 지하철 노선을 생성하면 404 에러가 발생한다")
    void createLineIfStationNotExists() {
        //when
        ExtractableResponse<Response> response =
                createLine("신분당선", "bg-red-600", "4", "5", "10");
        //then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    /**
     * When 생성되지 않은 지하철 노선을 수정하면
     * Then 404 에러가 발생한다
     */
    @Test
    @DisplayName("생성하지 않은 지하철 노선정보를 수정하면 404 에러가 발생한다")
    void changeLineException() {
        //when
        Map<String, String> params = new HashMap<>();
        params.put("name", "천당 위에 분당선");
        params.put("color", "bg-god-7000");
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .params(params)
                .when().put("/lines/100")
                .then().log().all()
                .extract();

        //then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    /**
     * Given 지하철 노선을 생성하고
     * When 생성한 지하철 노선을 삭제하면
     * Then 해당 지하철 노선 정보는 삭제된다
     */
    @Test
    @DisplayName("지하철 노선을 제거한다")
    void deleteLine() {
        //given
        ExtractableResponse<Response> line =
                createLine("신분당선", "bg-red-600", "1", "2", "10");
        int id = line.jsonPath().get("id");

        //when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when().delete("/lines/" + id)
                .then().log().all()
                .extract();

        //then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
        assertThat(getLines("name")).doesNotContain("신분당선").hasSize(1);
    }

    /**
     * When 생성되지 않은 지하철 노선을 삭제하면
     * Then 404 에러가 발생한다
     */
    @Test
    @DisplayName("생성되지 않은 지하철 노선을 제거하면 에러가 발생한다")
    void deleteLineIfNotExists() {
        //when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when().delete("/lines/" + 10)
                .then().log().all()
                .extract();

        //then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    /**
     * When 생성된 지하철 노선에 구간 등록을 하면
     * Then 지하철 노선에 구간이 등록된다
     */
    @Test
    @DisplayName("노선에 구간을 등록한다")
    void addSection() {
        //when
        ExtractableResponse<Response> response = addSection(_2호선_lineId, 1, 2, 5);

        //then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }


    /**
     * Given 지하철 노선 1개 구간 2개를 등록하고
     * When 지하철 노선 목록을 조회하면
     * Then 노선정보와 지하철역 3개를 조회할 수 있다
     */
    @Test
    @DisplayName("노선을 등록하고 조회한다")
    void findLineWithSection() {
        //when
        addSection(_2호선_lineId, 1, 2, 9);

        //then
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when().log().all()
                .get("/lines/" + _2호선_lineId)
                .then().log().all()
                .extract();

        List<String> stations = response.jsonPath().getList("stations.name", String.class);
        assertThat(stations.get(0)).isEqualTo("강남역");
        assertThat(stations.get(1)).isEqualTo("력삼역");
        assertThat(stations.get(2)).isEqualTo("선릉역");
    }

    /**
     * Given 지하철 노선 1개 구간 2개를 등록하고
     * When 지하철 노선 목록을 조회하면
     * Then 변경된 노선정보를 조회할 수 있다
     */
    @Test
    @DisplayName("노선을 생성하면 기존 노선의 거리가 조정된다")
    void findSectionByLine() {
        //when
        addSection(_2호선_lineId, 1, 2, 4);

        //then
        List<SectionResponse> sectionResponses = getSectionResponse(_2호선_lineId);

        Optional<SectionResponse> 강남역_력삼역 =
                sectionResponses.stream().filter(s -> s.getUpStation().equals("강남역")).findFirst();
        Optional<SectionResponse> 력삼역_선릉역 =
                sectionResponses.stream().filter(s -> s.getUpStation().equals("력삼역")).findFirst();
        assertThat(강남역_력삼역.get().getDistance()).isEqualTo(4);
        assertThat(력삼역_선릉역.get().getDistance()).isEqualTo(6);
    }

    /**
     * Given 지하철 노선을 추가하고
     * When 역을 삭제하면
     * Then 역이 삭제된다.
     */
    @Test
    @DisplayName("라인에서 역을 삭제한다")
    void deleteStation() {
        //given
        addSection(_2호선_lineId, 1, 2, 5);

        //when
        ExtractableResponse<Response> response = deleteStation(_2호선_lineId,2);

        //then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());

    }

    /**
     * Given 지하철 노선을 추가하고
     * When 중간 역을 삭제하면
     * Then 삭제한 역은 조회되지 않는다.
     * Then 이전 역의 역간 거리는 삭제한 역의 역간 거리만큼 증가한다.
     * Then 이전 역의 다음 역은 삭제한 역의 다음 역으로 업데이트 된다.
     */
    @Test
    @DisplayName("라인에서 역을 삭제하면 이전 역의 역간 거리는 삭제한 역간 거리만큼 늘어나고, 다음 역은 삭제한 다음 역으로 업데이트 된다")
    void deleteStation2() {
        //given
        addSection(_2호선_lineId, 1, 2, 5);

        //when
        deleteStation(_2호선_lineId, 2);

        //then
        List<SectionResponse> sectionResponse = getSectionResponse(_2호선_lineId);
        assertThat(sectionResponse.stream().filter(s -> s.getUpStation().equals("력삼역"))).hasSize(0);
        Optional<SectionResponse> 강남역 = sectionResponse.stream().filter(s -> s.getUpStation().equals("강남역")).findFirst();
        assertThat(강남역.get().getDistance()).isEqualTo(10);
        assertThat(강남역.get().getDownStation()).isEqualTo("선릉역");
    }

}
