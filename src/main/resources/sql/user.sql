-- TODO: Create INSERT or UPDATE logic instead of counting on big ID and split delete statements to another SQL file
DELETE FROM PUBLIC.PAYMENT_TRANSACTION;
DELETE FROM PUBLIC.USER;

-- Hashed password is 'password'
INSERT INTO PUBLIC.USER (id,
                  creation_date,
                  update_date,
                  email,
                  first_name,
                  last_name,
                  incorrect_login_attempts,
                  is_blocked,
                  password,
                  phone_number,
                  user_role,
                  reset_token)
VALUES (1000000, now(), now(), 'fake.customer@gmail.com', 'F', 'L', 0, FALSE,
 '$2a$10$e6IBCI16/mS0K3eueTMg1O0GFhi/jKQViyXgylveT4/jL4g8Dke6S', '733111333', 'CUSTOMER',
 '869cc21d-f04f-4210-a1c7-1139687c3c5d');