package com.payvia.dto;

import java.util.List;

public class RelaySimulationStepResultDto {
    private List<RelayEventDto> events;
    private int activePacketsRemaining;

    public List<RelayEventDto> getEvents() { return events; }
    public void setEvents(List<RelayEventDto> events) { this.events = events; }

    public int getActivePacketsRemaining() { return activePacketsRemaining; }
    public void setActivePacketsRemaining(int activePacketsRemaining) { this.activePacketsRemaining = activePacketsRemaining; }
}
