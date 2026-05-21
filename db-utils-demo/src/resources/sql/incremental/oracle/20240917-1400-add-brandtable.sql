CREATE TABLE Brand (
    id VARCHAR2(32) NOT NULL,
    brandName VARCHAR2(255),
    PRIMARY KEY (id)
)

INSERT INTO Brand (id, brandName)
VALUES ('brand1', 'DC')

INSERT INTO Brand (id, brandName)
VALUES ('brand2', 'Marvel')

ALTER TABLE Hero
ADD (brand_id VARCHAR2(32))

UPDATE Hero
SET brand_id = 'brand1'
WHERE id IN ('hero1','hero2')

UPDATE Hero
SET brand_id = 'brand2'
WHERE id IN ('hero3')