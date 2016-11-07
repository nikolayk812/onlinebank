DROP TABLE IF EXISTS accounts;
DROP SEQUENCE IF EXISTS global_seq;

CREATE SEQUENCE global_seq START 100;

CREATE TABLE accounts (
  id      INTEGER PRIMARY KEY DEFAULT nextval('global_seq'),
  name    TEXT         UNIQUE NOT NULL,
  balance DECIMAL(24, 10) NOT NULL
);
CREATE UNIQUE INDEX unique_name ON accounts(name);
