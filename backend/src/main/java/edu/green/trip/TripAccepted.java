package edu.green.trip;

public record TripAccepted(String eventId,long tripId,long userId,long points,String ruleVersion) { }
