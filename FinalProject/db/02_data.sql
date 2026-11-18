-- 2026: demo data, so the pharmacy can be tried right away. The people are fictional.
-- Accounts (email / password): admin@example.com / admin (ADMIN), anna@example.com / anna and
-- oleg@example.com / oleg (USER).
insert into medicines (name, price, country_of_production)
values ('Aspirin', 5, 'Germany'),
       ('Ibuprofen', 7, 'USA'),
       ('Paracetamol', 3, 'Belarus'),
       ('Активированный уголь', 2, 'Беларусь'),
       ('Nurofen', 9, 'United Kingdom'),
       ('Validol', 4, 'Russia');

insert into users (name, email, password, role)
values ('Admin', 'admin@example.com', 'admin', 'ADMIN'),
       ('Анна Смирнова', 'anna@example.com', 'anna', 'USER'),
       ('Oleg Kovalev', 'oleg@example.com', 'oleg', 'USER');
