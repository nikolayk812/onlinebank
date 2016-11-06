DROP TABLE IF EXISTS accounts;
DROP SEQUENCE IF EXISTS global_seq;

CREATE SEQUENCE global_seq START WITH 100;

CREATE TABLE accounts (
  id      INTEGER DEFAULT global_seq.nextval PRIMARY KEY,
  name    VARCHAR         UNIQUE NOT NULL,
  balance DECIMAL(24, 10) NOT NULL
)