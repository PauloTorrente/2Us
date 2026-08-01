package com.coupleapp.service.impl;

import com.coupleapp.dto.SuggestionDTOs.*;
import com.coupleapp.entity.SuggestionPreferences;
import com.coupleapp.entity.User;
import com.coupleapp.exception.NotFoundException;
import com.coupleapp.repository.SuggestionPreferencesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SuggestionService {

    private final SuggestionPreferencesRepository preferencesRepository;
    private final RestTemplate restTemplate;
    private final MetricsService metricsService;

    @Value("${app.google.places.api-key}")
    private String googleApiKey;

    @Value("${app.tripadvisor.api-key}")
    private String tripAdvisorApiKey;

    // --- Preferences Management ---

    public PreferencesResponse getPreferences(User user) {
        SuggestionPreferences prefs = preferencesRepository.findByCoupleId(user.getCouple().getId())
                .orElseGet(() -> createDefaultPreferences(user));
        return mapToPreferencesResponse(prefs);
    }

    @Transactional
    public PreferencesResponse updatePreferences(User user, UpdatePreferencesRequest request) {
        SuggestionPreferences prefs = preferencesRepository.findByCoupleId(user.getCouple().getId())
                .orElseGet(() -> createDefaultPreferences(user));

        if (request.getMaxDistanceMeters() != null) {
            prefs.setMaxDistanceMeters(request.getMaxDistanceMeters());
        }
        if (request.getCuisineTypes() != null) {
            prefs.setCuisineTypesFromSet(request.getCuisineTypes());
        }
        if (request.getBusinessTypes() != null) {
            prefs.setBusinessTypesFromSet(request.getBusinessTypes());
        }
        if (request.getMinRating() != null) {
            prefs.setMinRating(request.getMinRating());
        }
        if (request.getPriceLevel() != null) {
            prefs.setPriceLevel(request.getPriceLevel());
        }
        if (request.getPreferLocal() != null) {
            prefs.setPreferLocal(request.getPreferLocal());
        }

        return mapToPreferencesResponse(preferencesRepository.save(prefs));
    }

    @Transactional
    private SuggestionPreferences createDefaultPreferences(User user) {
        SuggestionPreferences prefs = SuggestionPreferences.builder()
                .couple(user.getCouple())
                .maxDistanceMeters(5000)
                .minRating(3.5)
                .preferLocal(true)
                .build();
        return preferencesRepository.save(prefs);
    }

    // --- Place Suggestions ---

    public List<PlaceSuggestionResponse> getSuggestions(User user, PlaceSuggestionRequest request) {
        // Get couple's preferences
        SuggestionPreferences prefs = preferencesRepository.findByCoupleId(user.getCouple().getId())
                .orElseGet(() -> createDefaultPreferences(user));

        // Determine search radius (use request override if provided, else preferences)
        int radius = request.getMaxDistanceMeters() != null 
            ? request.getMaxDistanceMeters() 
            : prefs.getMaxDistanceMeters();

        // Build Google Places API request
        String type = request.getBusinessType() != null ? request.getBusinessType() : "restaurant";
        String keyword = request.getCuisine() != null ? request.getCuisine() : null;

        // Call Google Places Nearby Search
        List<GooglePlaceResult> googleResults = searchGooglePlaces(
            request.getLatitude(), 
            request.getLongitude(), 
            radius, 
            type, 
            keyword
        );

        // Filter by minimum rating
        List<GooglePlaceResult> filtered = googleResults.stream()
                .filter(place -> place.getRating() != null && place.getRating() >= prefs.getMinRating())
                .toList();

        // Convert to response DTOs and enrich with TripAdvisor data
        List<PlaceSuggestionResponse> suggestions = filtered.stream()
                .map(googlePlace -> enrichWithTripAdvisor(
                    googlePlace, 
                    request.getLatitude(), 
                    request.getLongitude(),
                    prefs.getPreferLocal()  // Pass preferLocal flag
                ))
                .collect(Collectors.toList());

        // Sort by combined score (descending)
        suggestions.sort(Comparator.comparingDouble(PlaceSuggestionResponse::getCombinedScore).reversed());

        // Record metrics
        String businessType = request.getBusinessType() != null ? request.getBusinessType() : "restaurant";
        metricsService.recordSuggestionSearch(businessType, suggestions.size());

        return suggestions;
    }

    // --- Google Places Integration ---

    @Cacheable(value = "googlePlacesCache", key = "#latitude + ':' + #longitude + ':' + #radius + ':' + #type + ':' + #keyword")
    private List<GooglePlaceResult> searchGooglePlaces(
            Double latitude, 
            Double longitude, 
            Integer radius, 
            String type, 
            String keyword) {

        String url = String.format(
            "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=%f,%f&radius=%d&type=%s&key=%s",
            latitude, longitude, radius, type, googleApiKey
        );

        if (keyword != null && !keyword.isBlank()) {
            url += "&keyword=" + keyword;
        }

        long startTime = System.currentTimeMillis();
        boolean success = false;

        try {
            log.info("Calling Google Places API: {}", url.replace(googleApiKey, "***"));
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                success = true;
                List<Map<String, Object>> results = (List<Map<String, Object>>) response.getBody().get("results");
                
                return results.stream()
                        .map(this::parseGooglePlaceResult)
                        .filter(Objects::nonNull)
                        .toList();
            }
        } catch (Exception e) {
            log.error("Error calling Google Places API", e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            metricsService.recordExternalApiCall("google_places", duration, success);
        }

        return Collections.emptyList();
    }

    private GooglePlaceResult parseGooglePlaceResult(Map<String, Object> data) {
        try {
            GooglePlaceResult result = new GooglePlaceResult();
            result.setPlaceId((String) data.get("place_id"));
            result.setName((String) data.get("name"));
            result.setVicinity((String) data.get("vicinity"));
            
            Map<String, Object> geometry = (Map<String, Object>) data.get("geometry");
            if (geometry != null) {
                Map<String, Object> location = (Map<String, Object>) geometry.get("location");
                GooglePlaceResult.GoogleGeometry geom = new GooglePlaceResult.GoogleGeometry();
                GooglePlaceResult.GoogleLocation loc = new GooglePlaceResult.GoogleLocation();
                loc.setLat((Double) location.get("lat"));
                loc.setLng((Double) location.get("lng"));
                geom.setLocation(loc);
                result.setGeometry(geom);
            }

            Object ratingObj = data.get("rating");
            if (ratingObj != null) {
                result.setRating(((Number) ratingObj).doubleValue());
            }

            Object totalObj = data.get("user_ratings_total");
            if (totalObj != null) {
                result.setUserRatingsTotal(((Number) totalObj).intValue());
            }

            Object priceLevelObj = data.get("price_level");
            if (priceLevelObj != null) {
                result.setPriceLevel(((Number) priceLevelObj).intValue());
            }

            result.setTypes((List<String>) data.get("types"));

            Map<String, Object> openingHours = (Map<String, Object>) data.get("opening_hours");
            if (openingHours != null) {
                GooglePlaceResult.GoogleOpeningHours hours = new GooglePlaceResult.GoogleOpeningHours();
                hours.setOpenNow((Boolean) openingHours.get("open_now"));
                result.setOpeningHours(hours);
            }

            List<Map<String, Object>> photos = (List<Map<String, Object>>) data.get("photos");
            if (photos != null && !photos.isEmpty()) {
                List<GooglePlaceResult.GooglePhoto> photoList = new ArrayList<>();
                for (Map<String, Object> photo : photos) {
                    GooglePlaceResult.GooglePhoto p = new GooglePlaceResult.GooglePhoto();
                    p.setPhotoReference((String) photo.get("photo_reference"));
                    p.setWidth(((Number) photo.get("width")).intValue());
                    p.setHeight(((Number) photo.get("height")).intValue());
                    photoList.add(p);
                }
                result.setPhotos(photoList);
            }

            return result;
        } catch (Exception e) {
            log.error("Error parsing Google Place result", e);
            return null;
        }
    }

    // --- TripAdvisor Integration ---

    private PlaceSuggestionResponse enrichWithTripAdvisor(
            GooglePlaceResult googlePlace, 
            Double userLat, 
            Double userLng,
            Boolean preferLocal) {
        PlaceSuggestionResponse response = new PlaceSuggestionResponse();
        
        // Basic Google data
        response.setPlaceId(googlePlace.getPlaceId());
        response.setName(googlePlace.getName());
        response.setAddress(googlePlace.getVicinity());
        
        if (googlePlace.getGeometry() != null && googlePlace.getGeometry().getLocation() != null) {
            response.setLatitude(googlePlace.getGeometry().getLocation().getLat());
            response.setLongitude(googlePlace.getGeometry().getLocation().getLng());
            
            // Calculate distance from user's location
            response.setDistanceMeters(calculateDistance(
                userLat, userLng,
                response.getLatitude(), response.getLongitude()
            ));
        }

        response.setGoogleRating(googlePlace.getRating());
        response.setGoogleReviewCount(googlePlace.getUserRatingsTotal());
        response.setPriceLevel(googlePlace.getPriceLevel());
        response.setTypes(googlePlace.getTypes());

        if (googlePlace.getPhotos() != null && !googlePlace.getPhotos().isEmpty()) {
            response.setPhotoReference(googlePlace.getPhotos().get(0).getPhotoReference());
        }

        if (googlePlace.getOpeningHours() != null) {
            response.setOpenNow(googlePlace.getOpeningHours().getOpenNow());
        }

        // Try to enrich with TripAdvisor data
        TripAdvisorLocation taLocation = searchTripAdvisor(googlePlace.getName(), response.getLatitude(), response.getLongitude());
        if (taLocation != null) {
            response.setTripAdvisorId(taLocation.getLocationId());
            response.setTripAdvisorRating(taLocation.getRating());
            response.setTripAdvisorReviewCount(taLocation.getNumReviews());
            response.setTripAdvisorUrl(taLocation.getWebUrl());
        }

        // Calculate combined score
        response.setCombinedScore(calculateCombinedScore(response, preferLocal));

        return response;
    }

    @Cacheable(value = "tripAdvisorCache", key = "#name + ':' + #lat + ':' + #lng")
    private TripAdvisorLocation searchTripAdvisor(String name, Double lat, Double lng) {
        // TripAdvisor Location Search API
        String url = String.format(
            "https://api.content.tripadvisor.com/api/v1/location/search?searchQuery=%s&latLong=%f,%f&key=%s",
            name.replace(" ", "%20"), lat, lng, tripAdvisorApiKey
        );

        long startTime = System.currentTimeMillis();
        boolean success = false;

        try {
            log.info("Calling TripAdvisor API: {}", url.replace(tripAdvisorApiKey, "***"));
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                success = true;
                List<Map<String, Object>> data = (List<Map<String, Object>>) response.getBody().get("data");
                
                if (data != null && !data.isEmpty()) {
                    Map<String, Object> firstResult = data.get(0);
                    
                    TripAdvisorLocation location = new TripAdvisorLocation();
                    location.setLocationId((String) firstResult.get("location_id"));
                    location.setName((String) firstResult.get("name"));
                    
                    Object ratingObj = firstResult.get("rating");
                    if (ratingObj != null) {
                        location.setRating(Double.parseDouble(ratingObj.toString()));
                    }
                    
                    Object reviewsObj = firstResult.get("num_reviews");
                    if (reviewsObj != null) {
                        location.setNumReviews(Integer.parseInt(reviewsObj.toString()));
                    }
                    
                    location.setWebUrl((String) firstResult.get("web_url"));
                    
                    return location;
                }
            }
        } catch (Exception e) {
            log.warn("TripAdvisor API call failed for '{}': {}", name, e.getMessage());
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            metricsService.recordExternalApiCall("tripadvisor", duration, success);
        }

        return null;
    }

    // --- Helper Methods ---

    private Double calculateCombinedScore(PlaceSuggestionResponse place, Boolean preferLocal) {
        // Weighted average: Google 60%, TripAdvisor 40%
        // If TripAdvisor data is missing, use Google rating only
        
        double googleScore = place.getGoogleRating() != null ? place.getGoogleRating() : 0.0;
        double taScore = place.getTripAdvisorRating() != null ? place.getTripAdvisorRating() : 0.0;
        
        double combined;
        
        if (taScore > 0.0) {
            // Both platforms have data — use weighted average
            combined = (googleScore * 0.6) + (taScore * 0.4);
        } else {
            // Only Google has data — boost by 10% when preferLocal, since small local spots
            // are less likely to be listed on TripAdvisor yet.
            combined = preferLocal != null && preferLocal
                ? Math.min(googleScore * 1.1, 5.0)  // Cap at 5.0
                : googleScore;
        }
        
        return combined;
    }

    private Integer calculateDistance(Double lat1, Double lng1, Double lat2, Double lng2) {
        // Haversine formula to calculate distance in meters
        final int R = 6371000; // Earth's radius in meters

        double latDistance = Math.toRadians(lat2 - lat1);
        double lngDistance = Math.toRadians(lng2 - lng1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return (int) (R * c);
    }

    private PreferencesResponse mapToPreferencesResponse(SuggestionPreferences prefs) {
        return new PreferencesResponse(
            prefs.getId(),
            prefs.getMaxDistanceMeters(),
            prefs.getCuisineTypesSet(),
            prefs.getBusinessTypesSet(),
            prefs.getMinRating(),
            prefs.getPriceLevel(),
            prefs.getPreferLocal(),
            prefs.getUpdatedAt()
        );
    }
}
