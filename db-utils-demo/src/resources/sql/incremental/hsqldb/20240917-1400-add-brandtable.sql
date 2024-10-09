CREATE TABLE Brand (
   	id varchar(32) PRIMARY KEY,
    brandName varchar(255)
)

INSERT INTO Brand VALUES ('brand1', 'DC')

INSERT INTO Brand VALUES ('brand2', 'Marvel')

ALTER TABLE Hero ADD
    brand_id varchar(32)
   
UPDATE Hero SET brand_id = 'brand1' WHERE id in ('hero1', 'hero2')

UPDATE Hero SET brand_id = 'brand2' WHERE id in ('hero3')
