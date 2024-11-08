CREATE TABLE subscription
(
    id                     BIGSERIAL    NOT NULL PRIMARY KEY,
    subscriber_id          VARCHAR(255) NOT NULL,
    subscriber_email       VARCHAR(255) NOT NULL,
    subscribed_at_username VARCHAR(255) NOT NULL,
    subscribed_at_id       VARCHAR(255) NOT NULL
);

CREATE TABLE notification
(
    id              BIGSERIAL NOT NULL PRIMARY KEY,
    subscription_id BIGINT    NOT NULL,
    build_id        BIGINT    NOT NULL,
    CONSTRAINT fk_notification_subscription FOREIGN KEY (subscription_id) REFERENCES subscription (id)
);
