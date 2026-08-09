package com.muhammet.inventory_service.outbox.exception;

import java.util.UUID;

public class OutboxSerializationException
        extends RuntimeException {

    public OutboxSerializationException(
            UUID eventId,
            Throwable cause
    ) {
        super(
                "Outbox event could not be serialized. eventId="
                        + eventId,
                cause
        );
    }
}