CREATE TABLE Brand (
   	id varchar(32) PRIMARY KEY,
    brandName varchar(255)
)

INSERT INTO Brand VALUES ('brand-1', 'DC')

INSERT INTO Brand VALUES ('brand-2', 'Marvel')

ALTER TABLE Hero ADD
    brand_id varchar(32)
   
UPDATE Hero SET brand_id = 'brand-1' WHERE id in ('hero-1', 'hero-2')

UPDATE Hero SET brand_id = 'brand-2' WHERE id in ('hero-3')
