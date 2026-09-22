package com.payvia.dto;

import java.time.OffsetDateTime;
import java.util.List;

public class TransactionTimelineDto {
    private TransactionDto transaction;
    private List<EventDto> events;
    
    public static class EventDto {
        private String type; // e.g. "RELAY_EVENT", "SETTLEMENT_ATTEMPT", "SETTLEMENT"
        private String description;
        private String status;
        private OffsetDateTime timestamp;
        
        public EventDto(String type, String description, String status, OffsetDateTime timestamp) {
            this.type = type;
            this.description = description;
            this.status = status;
            this.timestamp = timestamp;
        }

        public String getType() { return type; }
        public String getDescription() { return description; }
        public String getStatus() { return status; }
        public OffsetDateTime getTimestamp() { return timestamp; }
    }

    public TransactionDto getTransaction() { return transaction; }
    public void setTransaction(TransactionDto transaction) { this.transaction = transaction; }

    public List<EventDto> getEvents() { return events; }
    public void setEvents(List<EventDto> events) { this.events = events; }
}
