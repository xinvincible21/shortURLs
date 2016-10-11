CREATE DATABASE links;

use links;

CREATE TABLE short_urls (
             id BIGINT NOT NULL AUTO_INCREMENT,
             url TEXT NOT NULL,
             short_url VARCHAR(255) NOT NULL,
             PRIMARY KEY (id)
);

CREATE INDEX short_url_index ON short_urls (short_url);
CREATE INDEX url_index ON short_urls (url);


