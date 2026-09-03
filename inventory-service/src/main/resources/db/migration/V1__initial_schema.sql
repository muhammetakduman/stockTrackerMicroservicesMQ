-- ============================================================
-- INVENTORY SERVICE
-- V1 - INITIAL SCHEMA
-- ============================================================


-- ============================================================
-- STOCK ITEMS
-- ============================================================

CREATE TABLE stock_items
(
    id                UUID          NOT NULL,
    name              VARCHAR(150)  NOT NULL,
    sku               VARCHAR(50)   NOT NULL,
    description       VARCHAR(500),

    item_type         VARCHAR(30)   NOT NULL,
    unit              VARCHAR(30)   NOT NULL,
    packaging_kind    VARCHAR(20),

    active            BOOLEAN       NOT NULL,

    created_at        TIMESTAMPTZ   NOT NULL,
    updated_at        TIMESTAMPTZ   NOT NULL,

    CONSTRAINT pk_stock_items
        PRIMARY KEY (id),

    CONSTRAINT uk_stock_items_sku
        UNIQUE (sku),

    CONSTRAINT chk_stock_items_item_type
        CHECK (
            item_type IN (
                'ESSENCE',
                'FINISHED_PRODUCT',
                'PACKAGING'
            )
        ),

    CONSTRAINT chk_stock_items_unit
        CHECK (
            unit IN (
                'GRAM',
                'MILLILITER',
                'PIECE'
            )
        ),

    CONSTRAINT chk_stock_items_packaging_kind
        CHECK (
            packaging_kind IS NULL
            OR packaging_kind IN (
                'BOTTLE',
                'MALE_SET',
                'FEMALE_SET',
                'UNISEX_SET'
            )
        )
);


-- ============================================================
-- STOCK BALANCES
-- ============================================================

CREATE TABLE stock_balances
(
    id                  UUID           NOT NULL,
    stock_item_id       UUID           NOT NULL,

    on_hand_quantity    NUMERIC(19,3)  NOT NULL,
    reserved_quantity   NUMERIC(19,3)  NOT NULL,

    version             BIGINT,

    updated_at          TIMESTAMPTZ    NOT NULL,

    CONSTRAINT pk_stock_balances
        PRIMARY KEY (id),

    CONSTRAINT uk_stock_balances_stock_item_id
        UNIQUE (stock_item_id),

    CONSTRAINT fk_stock_balance_item
        FOREIGN KEY (stock_item_id)
        REFERENCES stock_items (id)
);


-- ============================================================
-- STOCK MOVEMENTS
-- ============================================================

CREATE TABLE stock_movements
(
    id                          UUID           NOT NULL,

    source_event_id             UUID           NOT NULL,

    stock_item_id               UUID           NOT NULL,

    movement_type               VARCHAR(40)    NOT NULL,

    quantity_change             NUMERIC(19,3)  NOT NULL,

    previous_on_hand_quantity   NUMERIC(19,3)  NOT NULL,
    new_on_hand_quantity        NUMERIC(19,3)  NOT NULL,

    reference_type              VARCHAR(50)    NOT NULL,
    reference_id                VARCHAR(100)   NOT NULL,

    reason_code                 VARCHAR(50),
    note                        VARCHAR(500),

    source_occurred_at          TIMESTAMPTZ    NOT NULL,
    created_at                  TIMESTAMPTZ    NOT NULL,

    CONSTRAINT pk_stock_movements
        PRIMARY KEY (id),

    CONSTRAINT uk_stock_movements_source_event_id
        UNIQUE (source_event_id),

    CONSTRAINT fk_stock_movement_item
        FOREIGN KEY (stock_item_id)
        REFERENCES stock_items (id),

    CONSTRAINT chk_stock_movements_movement_type
        CHECK (
            movement_type IN (
                'PURCHASE_RECEIPT',
                'SALE',
                'ADJUSTMENT',
                'PRODUCTION_CONSUMPTION',
                'PRODUCTION_OUTPUT'
            )
        )
);

CREATE INDEX idx_stock_movements_stock_item_id
    ON stock_movements (stock_item_id);

CREATE INDEX idx_stock_movements_movement_type
    ON stock_movements (movement_type);

CREATE INDEX idx_stock_movements_created_at
    ON stock_movements (created_at);

CREATE INDEX idx_stock_movements_reference
    ON stock_movements (
        reference_type,
        reference_id
    );


-- ============================================================
-- PRODUCTION RECIPES
-- ============================================================

CREATE TABLE production_recipes
(
    id                              UUID           NOT NULL,

    name                            VARCHAR(150)   NOT NULL,
    description                     VARCHAR(500),

    essence_stock_item_id           UUID           NOT NULL,
    bottle_stock_item_id            UUID           NOT NULL,
    packaging_set_stock_item_id     UUID           NOT NULL,
    output_stock_item_id            UUID           NOT NULL,

    essence_quantity_per_unit       NUMERIC(19,3)  NOT NULL,
    bottle_quantity_per_unit        NUMERIC(19,3)  NOT NULL,
    packaging_quantity_per_unit     NUMERIC(19,3)  NOT NULL,

    active                          BOOLEAN        NOT NULL,

    created_at                      TIMESTAMPTZ    NOT NULL,
    updated_at                      TIMESTAMPTZ    NOT NULL,

    CONSTRAINT pk_production_recipes
        PRIMARY KEY (id),

    CONSTRAINT fk_recipe_essence_item
        FOREIGN KEY (essence_stock_item_id)
        REFERENCES stock_items (id),

    CONSTRAINT fk_recipe_bottle_item
        FOREIGN KEY (bottle_stock_item_id)
        REFERENCES stock_items (id),

    CONSTRAINT fk_recipe_packaging_set_item
        FOREIGN KEY (packaging_set_stock_item_id)
        REFERENCES stock_items (id),

    CONSTRAINT fk_recipe_output_item
        FOREIGN KEY (output_stock_item_id)
        REFERENCES stock_items (id)
);


-- ============================================================
-- PRODUCTION BATCHES
-- ============================================================

CREATE TABLE production_batches
(
    id                              UUID           NOT NULL,

    operation_id                    UUID           NOT NULL,

    recipe_id                       UUID           NOT NULL,

    output_quantity                 NUMERIC(19,3)  NOT NULL,

    essence_quantity_consumed       NUMERIC(19,3)  NOT NULL,
    bottle_quantity_consumed        NUMERIC(19,3)  NOT NULL,
    packaging_quantity_consumed     NUMERIC(19,3)  NOT NULL,

    essence_stock_item_id           UUID           NOT NULL,
    bottle_stock_item_id            UUID           NOT NULL,
    packaging_set_stock_item_id     UUID           NOT NULL,
    output_stock_item_id            UUID           NOT NULL,

    produced_by_user_id             UUID           NOT NULL,

    produced_at                     TIMESTAMPTZ    NOT NULL,

    note                            VARCHAR(500),

    created_at                      TIMESTAMPTZ    NOT NULL,

    CONSTRAINT pk_production_batches
        PRIMARY KEY (id),

    CONSTRAINT uk_production_batches_operation_id
        UNIQUE (operation_id),

    CONSTRAINT fk_batch_recipe
        FOREIGN KEY (recipe_id)
        REFERENCES production_recipes (id)
);


-- ============================================================
-- PROCESSED EVENTS / INBOX
-- ============================================================

CREATE TABLE processed_events
(
    id               UUID          NOT NULL,

    event_id         UUID          NOT NULL,

    event_type       VARCHAR(100)  NOT NULL,

    consumer_name    VARCHAR(150)  NOT NULL,

    processed_at     TIMESTAMPTZ   NOT NULL,

    CONSTRAINT pk_processed_events
        PRIMARY KEY (id),

    CONSTRAINT uk_processed_event_consumer
        UNIQUE (
            event_id,
            consumer_name
        )
);

CREATE INDEX idx_processed_events_processed_at
    ON processed_events (processed_at);


-- ============================================================
-- OUTBOX EVENTS
-- ============================================================

CREATE TABLE outbox_events
(
    id               UUID          NOT NULL,

    event_id         UUID          NOT NULL,

    aggregate_type   VARCHAR(50)   NOT NULL,
    aggregate_id     VARCHAR(100)  NOT NULL,

    event_type       VARCHAR(100)  NOT NULL,
    event_version    INTEGER       NOT NULL,

    exchange_name    VARCHAR(150)  NOT NULL,
    routing_key      VARCHAR(150)  NOT NULL,

    payload          TEXT          NOT NULL,

    status           VARCHAR(30)   NOT NULL,

    attempt_count    INTEGER       NOT NULL,

    available_at     TIMESTAMPTZ   NOT NULL,

    created_at       TIMESTAMPTZ   NOT NULL,
    published_at     TIMESTAMPTZ,

    last_error       VARCHAR(1000),

    version          BIGINT,

    CONSTRAINT pk_outbox_events
        PRIMARY KEY (id),

    CONSTRAINT uk_outbox_events_event_id
        UNIQUE (event_id),

    CONSTRAINT chk_outbox_events_status
        CHECK (
            status IN (
                'PENDING',
                'PUBLISHED',
                'FAILED'
            )
        )
);

CREATE INDEX idx_outbox_events_status_available_at
    ON outbox_events (
        status,
        available_at
    );

CREATE INDEX idx_outbox_events_aggregate
    ON outbox_events (
        aggregate_type,
        aggregate_id
    );

CREATE INDEX idx_outbox_events_created_at
    ON outbox_events (created_at);