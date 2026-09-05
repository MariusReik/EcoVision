package no.ecovision.activity;

import java.util.List;

public record ActivityPageResponse(List<ActivityResponse> items, String nextCursor) {
}
