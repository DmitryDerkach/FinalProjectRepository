-- 2026: the database of the pharmacy. In 2021 the tables were created by hand in a local PostgreSQL and never
-- committed; they are restored here from the SQL in MedicineDao and UserDao (column names, types the DAO reads).
-- fix 2026: the table was called "medecines" (a typo, also in MedicineDao) - now "medicines".
-- The script can be run again at any time: ./run.sh reset (and web, runner, test) recreates everything.
set client_min_messages to warning;

drop table if exists medicines cascade;
drop table if exists users cascade;

-- MedicineDao: select * / insert (name, price, country_of_production) / delete ... where id = ?
create table medicines
(
    id                    serial primary key,
    name                  varchar(128) not null,
    -- whole dollars: Medicine.price is an int
    price                 integer      not null check (price >= 0),
    country_of_production varchar(128) not null
);

-- UserDao: insert (name, email, password, role) / select ... where email = ? and password = ?
create table users
(
    id       serial primary key,
    name     varchar(128) not null,
    -- one account per email: the login looks a user up by email and password
    email    varchar(128) not null unique,
    -- stored as plain text, as in the course (see README "Known issues")
    password varchar(128) not null,
    -- the names of the Java enum Role
    role     varchar(32)  not null check (role in ('USER', 'ADMIN'))
);
