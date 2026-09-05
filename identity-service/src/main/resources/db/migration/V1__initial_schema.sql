-- ============================================================
-- IDENTITY SERVICE
-- V1 - INITIAL SCHEMA
--
-- Entities:
--   Role
--   User
--   RefreshToken
--
-- Additional JPA table:
--   user_roles
-- ============================================================


-- ============================================================
-- ROLES
--
-- Role.name:
--   @Enumerated(EnumType.STRING)
--
-- Supported values:
--   ADMIN
--   STOCK_MANAGER
--   PRODUCTION_USER
--   SALES_USER
-- ============================================================

CREATE TABLE roles
(
    id      UUID        NOT NULL,
    name    VARCHAR(50) NOT NULL,

    CONSTRAINT pk_roles
        PRIMARY KEY (id),

    CONSTRAINT uk_roles_name
        UNIQUE (name),

    CONSTRAINT chk_roles_name
        CHECK (
            name IN (
                'ADMIN',
                'STOCK_MANAGER',
                'PRODUCTION_USER',
                'SALES_USER'
            )
        )
);


-- ============================================================
-- USERS
--
-- created_at / updated_at Java tarafında LocalDateTime.
-- Bu nedenle PostgreSQL tarafında TIMESTAMP kullanıyoruz.
-- ============================================================

CREATE TABLE users
(
    id              UUID          NOT NULL,

    first_name      VARCHAR(100)  NOT NULL,
    last_name       VARCHAR(100)  NOT NULL,

    email           VARCHAR(255)  NOT NULL,

    password_hash   VARCHAR(255)  NOT NULL,

    enabled         BOOLEAN       NOT NULL,
    account_locked  BOOLEAN       NOT NULL,

    created_at      TIMESTAMP     NOT NULL,
    updated_at      TIMESTAMP     NOT NULL,

    CONSTRAINT pk_users
        PRIMARY KEY (id),

    CONSTRAINT uk_users_email
        UNIQUE (email)
);


-- ============================================================
-- USER <-> ROLE MANY-TO-MANY JOIN TABLE
--
-- User entity:
--
-- @ManyToMany
-- @JoinTable(
--     name = "user_roles",
--     joinColumns = @JoinColumn(name = "user_id"),
--     inverseJoinColumns = @JoinColumn(name = "role_id")
-- )
-- ============================================================

CREATE TABLE user_roles
(
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,

    CONSTRAINT pk_user_roles
        PRIMARY KEY (
            user_id,
            role_id
        ),

    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id)
        REFERENCES users (id),

    CONSTRAINT fk_user_roles_role
        FOREIGN KEY (role_id)
        REFERENCES roles (id)
);


-- ============================================================
-- REFRESH TOKENS
--
-- expires_at / created_at Java tarafında Instant.
-- PostgreSQL tarafında TIMESTAMPTZ kullanıyoruz.
--
-- replaced_by_token_id:
-- Bir refresh token başka bir refresh token'a referans verebilir.
-- Self-referencing foreign key.
-- ============================================================

CREATE TABLE refresh_tokens
(
    id                      UUID          NOT NULL,

    user_id                 UUID          NOT NULL,

    token_hash              VARCHAR(255)  NOT NULL,

    expires_at              TIMESTAMPTZ   NOT NULL,

    revoked                 BOOLEAN       NOT NULL,

    created_at              TIMESTAMPTZ   NOT NULL,

    replaced_by_token_id    UUID,

    CONSTRAINT pk_refresh_tokens
        PRIMARY KEY (id),

    CONSTRAINT uk_refresh_tokens_token_hash
        UNIQUE (token_hash),

    CONSTRAINT fk_refresh_tokens_user
        FOREIGN KEY (user_id)
        REFERENCES users (id),

    CONSTRAINT fk_refresh_tokens_replaced_by
        FOREIGN KEY (replaced_by_token_id)
        REFERENCES refresh_tokens (id)
);