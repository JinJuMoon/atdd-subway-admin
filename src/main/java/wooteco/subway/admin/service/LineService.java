package wooteco.subway.admin.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import wooteco.subway.admin.domain.Line;
import wooteco.subway.admin.domain.LineStation;
import wooteco.subway.admin.domain.Station;
import wooteco.subway.admin.dto.LineRequest;
import wooteco.subway.admin.dto.LineResponse;
import wooteco.subway.admin.dto.LineStationCreateRequest;
import wooteco.subway.admin.repository.LineRepository;
import wooteco.subway.admin.repository.StationRepository;

@Service
public class LineService {
	private LineRepository lineRepository;
	private StationRepository stationRepository;

	public LineService(LineRepository lineRepository, StationRepository stationRepository) {
		this.lineRepository = lineRepository;
		this.stationRepository = stationRepository;
	}

	public List<LineResponse> showLines() {
		List<Line> persistLines = lineRepository.findAll();

		Map<Line, Set<Station>> lineWithStations = persistLines.stream()
			.collect(Collectors.toMap(
				Function.identity(),
				persistLine -> toStations(persistLine.getLineStationsId())
			));

		return LineResponse.listOf(lineWithStations);
	}

	public LineResponse showLine(Long id) {
		Line persistLine = lineRepository.findById(id).orElseThrow(RuntimeException::new);

		Set<Station> stations = toStations(persistLine.getLineStationsId());

		return LineResponse.of(persistLine, stations);
	}

	public LineResponse updateLine(Long id, LineRequest lineRequest) {
		Line persistLine = lineRepository.findById(id).orElseThrow(RuntimeException::new);
		persistLine.update(lineRequest.toLine());
		Line updatedLine = lineRepository.save(persistLine);

		Set<Station> stations = toStations(updatedLine.getLineStationsId());

		return LineResponse.of(updatedLine, stations);
	}

	public void deleteLineById(Long id) {
		lineRepository.deleteById(id);
	}

	public void addLineStation(Long lineId, LineStationCreateRequest lineStationCreateRequest) {
		Line line = lineRepository.findById(lineId).orElseThrow(RuntimeException::new);
		LineStation lineStation = lineStationCreateRequest.toLineStation();
		line.addLineStation(lineStation);
		lineRepository.save(line);
	}

	public void removeLineStation(Long lineId, Long stationId) {
		Line line = lineRepository.findById(lineId).orElseThrow(RuntimeException::new);
		line.removeLineStationById(stationId);
		lineRepository.save(line);
	}

	public Set<Station> toStations(List<Long> lineStationsId) {
		Set<Station> stations = new LinkedHashSet<>();
		for (Long id : lineStationsId) {
			Station station = stationRepository.findById(id)
				.orElseThrow(RuntimeException::new);
			stations.add(station);
		}
		return stations;
	}

	public LineResponse createLine(LineRequest lineRequest) {
		Line persistLine = lineRepository.save(lineRequest.toLine());
		Set<Station> stations = toStations(persistLine.getLineStationsId());

		return LineResponse.of(persistLine, stations);
	}
}
