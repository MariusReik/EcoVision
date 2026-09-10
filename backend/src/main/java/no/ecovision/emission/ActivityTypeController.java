package no.ecovision.emission;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/activity-types")
public class ActivityTypeController {

    private final ActivityTypeRepository activityTypeRepository;

    public ActivityTypeController(ActivityTypeRepository activityTypeRepository) {
        this.activityTypeRepository = activityTypeRepository;
    }

    @GetMapping
    public List<ActivityTypeResponse> list() {
        return activityTypeRepository.findByActiveTrueOrderBySortOrderAscCodeAsc().stream()
                .map(ActivityTypeResponse::from)
                .toList();
    }
}
