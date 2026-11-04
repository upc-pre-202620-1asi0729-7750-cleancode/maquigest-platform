-- MaquiGest Rentals. Use on the intended schema before enabling the new endpoint
-- when Hibernate ddl-auto does not create tables (for example in validate mode).
-- Zero existing rental requests are modified.
CREATE TABLE IF NOT EXISTS rental_request_submission_guards (
    id BIGINT NOT NULL AUTO_INCREMENT,
    equipment_id BIGINT NOT NULL,
    construction_user_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_rental_request_submission_guard_pair
        UNIQUE (equipment_id, construction_user_id)
) ENGINE=InnoDB;

-- Verify with:
-- SHOW CREATE TABLE rental_request_submission_guards;
