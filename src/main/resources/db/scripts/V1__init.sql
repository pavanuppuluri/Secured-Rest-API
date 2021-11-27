CREATE TABLE USER (
  user_id int NOT NULL AUTO_INCREMENT,
  username varchar(45) NOT NULL,
  password varchar(200) NOT NULL,
  FIRST_NAME VARCHAR(100),
  LAST_NAME VARCHAR(100),
  EMAIL VARCHAR(250),
  PRIMARY KEY (user_id)
);

CREATE TABLE roles (
  role_id int NOT NULL AUTO_INCREMENT,
  name varchar(50) NOT NULL,
  PRIMARY KEY (role_id)
);

CREATE TABLE user_roles (
  user_id int NOT NULL,
  role_id int NOT NULL,
  KEY user_id_key (user_id),
  KEY role_id_key (role_id),
  CONSTRAINT role_fk FOREIGN KEY (role_id) REFERENCES roles (role_id),
  CONSTRAINT user_fk FOREIGN KEY (user_id) REFERENCES user (user_id)
);

CREATE TABLE token_store (
  id int NOT NULL AUTO_INCREMENT,
  token varchar(5000) NOT NULL,
  username varchar(45) NOT NULL,
  PRIMARY KEY (id)
);

INSERT INTO USER
(
username,
password,
first_name,
last_name,
email
)
VALUES
(
'tzadmin',
'$2a$10$pucWsSNzuh1.2LgddoCHkeZhT19R5Ku3b2GK5Aw6XDMwd665vsrRi',
"Pavan",
"Uppuluri",
"pavanu.techtalks@gmail.com"
);


INSERT INTO roles (name) VALUES ('ROLE_USER');
INSERT INTO roles (name) VALUES ('ROLE_ADMIN');

INSERT INTO USER_ROLES (USER_ID,ROLE_ID) VALUES (1,2);





