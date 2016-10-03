
CREATE TABLE short_urls (
             id BIGINT NOT NULL AUTO_INCREMENT,
             url VARCHAR(1024) NOT NULL,
             short_url VARCHAR(1024) NOT NULL,
             PRIMARY KEY (id)
);
