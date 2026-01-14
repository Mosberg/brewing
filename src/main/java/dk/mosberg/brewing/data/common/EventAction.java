package dk.mosberg.brewing.data.common;

import java.util.List;
import java.util.Map;

/**
 * Data-driven event action from common-schema.json.
 *
 * This is intentionally permissive: individual action types define their own params/conditions.
 */
public record EventAction(String type, List<Map<String, Object>> conditions,
        Map<String, Object> params) {
}
