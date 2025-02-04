package me.darragh.lwjgl;

import lombok.Getter;

public class EventQueue {
    /**
     * -- GETTER --
     * The maximum number of events the queue can hold.
     */
    @Getter
    private final int maxEvents;

    /**
     * -- GETTER --
     * The current number of events in the queue.
     */
    @Getter
    private int eventCount = 0;
    private int readPos = 0;
    private int writePos = 1;
    private long lastDroppedMessageTime = 0;

    /**
     * Constructs an EventQueue with a specified maximum number of events.
     *
     * @param maxEvents The maximum number of events the queue can hold.
     */
    public EventQueue(int maxEvents) {
        this.maxEvents = maxEvents;
    }

    /**
     * Adds an event to the queue. If the queue is full, the oldest event is discarded.
     */
    public synchronized void add() {
        if (eventCount >= maxEvents) {
            eventCount = maxEvents; // Cap the event count
            long currentTime = System.currentTimeMillis();

            if (currentTime - lastDroppedMessageTime > 1000) {
                lastDroppedMessageTime = currentTime;
                System.out.println("Dropping LWJGL input events due to insufficient polling frequency.");
            }
        } else {
            eventCount++; // Increase the event count
        }

        writePos = (writePos + 1) % maxEvents; // Move to the next write position

        if (writePos == readPos) { // If the queue is full, discard the oldest event
            readPos = (readPos + 1) % maxEvents;
            eventCount--;
        }
    }

    /**
     * Moves to the next event in the queue.
     *
     * @return {@code true} if an event is available, otherwise {@code false}.
     */
    public synchronized boolean next() {
        if (eventCount == 0) return false;

        eventCount--;
        readPos = (readPos + 1) % maxEvents;

        return true;
    }

    public int getCurrentPos() {
        return readPos;
    }

    public int getLastWrittenPos() {
        return (writePos + maxEvents - 1) % maxEvents;
    }

    public int getNextPos() {
        return writePos;
    }
}
